package com.github.lstudioo.propertieseditor.repository

import com.github.lstudioo.propertieseditor.model.*
import com.github.lstudioo.propertieseditor.model.dto.PresetDto
import com.github.lstudioo.propertieseditor.model.dto.SchemaDto
import com.github.lstudioo.propertieseditor.model.dto.SchemaMapper
import com.github.lstudioo.propertieseditor.services.PropertyEditorService
import com.github.lstudioo.propertieseditor.utils.OrderedProperties
import com.google.gson.Gson
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File
import java.util.*

/**
 * Repository for managing properties and their definitions.
 * Handles loading and saving properties to/from files, and provides operations
 * for manipulating properties.
 */
class PropertyRepository(
    private val propertyEditorService: PropertyEditorService,
) {
    private val mapper = SchemaMapper()
    private val properties = OrderedProperties()
    private val gson = Gson()
    private val observers = mutableListOf<PropertyRepositoryObserver>()
    private var propertyDefinitions: List<PropertyDefinition> = emptyList()
    private var presets: List<Preset> = emptyList()

    private val configFile: File
        get() = propertyEditorService.getFileSettings().configFile

    private val propertiesFile: File
        get() = propertyEditorService.getFileSettings().propertiesFile

    /**
     * Loads the schema and properties configuration
     */
    fun loadConfiguration() {
        loadSchema()
        loadProperties()
        notifyObservers()
    }

    /**
     * Registers an observer to be notified of property changes
     */
    fun addObserver(observer: PropertyRepositoryObserver) {
        observers.add(observer)
    }

    private fun notifyObservers() {
        observers.forEach { it.onPropertiesReloaded() }
    }

    /**
     * Loads and parses the JSON schema
     */
    private fun loadSchema() {
        val schemaJson = configFile.readText()
        val schema = gson.fromJson(schemaJson, SchemaDto::class.java)
        propertyDefinitions = schema.properties.map(mapper::mapDefinition)
        presets = schema.presets?.map { presetDto ->
            validatePreset(presetDto)
            mapper.mapPreset(presetDto, propertyDefinitions)
        }.orEmpty()
    }

    private fun validatePreset(presetDto: PresetDto) {
        if (presetDto.name.isBlank()) {
            throw IllegalArgumentException("Preset name cannot be empty")
        }
        presetDto.properties.forEach { (key, _) ->
            if (!propertyDefinitions.any { it.key == key }) {
                throw IllegalArgumentException("Unknown property in preset: $key")
            }
        }
    }

    /**
     * Loads properties from the properties file
     */
    private fun loadProperties() {
        properties.clear()
        propertiesFile.inputStream().use { properties.load(it) }
    }

    /**
     * Gets all properties with their current values, including:
     * 1. Properties defined in schema but not in file (SCHEMA_ONLY)
     * 2. Properties defined in both schema and file (BOTH)
     * 3. Properties defined in file but not in schema (FILE_ONLY)
     */
    fun getProperties(): List<Property> {
        return buildList {
            // Properties in schema
            addAll(
                propertyDefinitions.map { definition ->
                    if (!properties.containsKey(definition.key)) {
                        // Schema-only properties (in schema but not in file)
                        Property(
                            key = definition.key,
                            value = definition.defaultValue ?: getDefaultValueForType(definition.type),
                            description = definition.description,
                            source = PropertySource.SCHEMA_ONLY
                        )
                    } else {
                        // Properties in both file and schema
                        Property(
                            key = definition.key,
                            value = getPropertyValue(definition),
                            description = definition.description,
                            source = PropertySource.BOTH
                        )
                    }
                }
            )
            
            // Properties in file but not in schema
            addAll(
                properties.filterNot { property -> 
                    propertyDefinitions.any { it.key == property.key } 
                }.map { p ->
                    Property(
                        key = p.key.toString(),
                        value = PropertyValue.StringValue(p.value.toString()),
                        description = "Undefined in the schema",
                        source = PropertySource.FILE_ONLY
                    )
                }
            )
        }
    }

    /**
     * Returns available presets
     */
    fun getPresets(): List<Preset> = presets

    /**
     * Returns a preset by name
     */
    fun getPreset(name: String): Preset? = presets.find { it.name == name }

    /**
     * Returns a property definition by key
     */
    fun getPropertyDefinition(key: String): PropertyDefinition? =
        propertyDefinitions.find { it.key == key }

    /**
     * Updates a property in the properties file
     */
    fun updateProperty(property: Property) {
        properties.setProperty(property.key, property.value.asString())
        saveProperties()
        notifyTheIdeAboutFileUpdates()
    }

    /**
     * Deletes a property from the properties file
     */
    fun deleteProperty(key: String) {
        properties.remove(key)
        saveProperties()
        notifyTheIdeAboutFileUpdates()
        notifyObservers()
    }

    private fun notifyTheIdeAboutFileUpdates() {
        val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(propertiesFile)
        virtualFile?.refresh(false, false)
    }

    private fun saveProperties() {
        propertiesFile.outputStream().use { properties.store(it, null) }
    }

    /**
     * Gets the property value for a definition from the properties file
     */
    private fun getPropertyValue(definition: PropertyDefinition): PropertyValue {
        val rawValue = properties.getProperty(definition.key)
        return when (definition.type) {
            is PropertyType.Boolean -> PropertyValue.BooleanValue(
                rawValue?.trim()?.lowercase(Locale.getDefault()) == "true"
            )

            is PropertyType.Array -> {
                val validValues = rawValue?.split(",")
                    ?.asSequence()
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?.filter { value -> definition.type.possibleValues.contains(value) }
                    ?.distinct()
                    ?.sorted()
                    ?.toList()
                    ?: emptyList()
                PropertyValue.ArrayValue(validValues)
            }

            is PropertyType.Enum -> {
                val enumValue = rawValue?.takeIf {
                    definition.type.possibleValues.contains(it)
                } ?: definition.type.possibleValues.firstOrNull() ?: ""
                PropertyValue.StringValue(enumValue)
            }

            PropertyType.StringValue -> PropertyValue.StringValue(rawValue ?: "")
        }
    }

    /**
     * Creates a default value for a property type
     */
    private fun getDefaultValueForType(type: PropertyType): PropertyValue {
        return when (type) {
            is PropertyType.Boolean -> PropertyValue.BooleanValue(false)
            is PropertyType.Array -> PropertyValue.ArrayValue(emptyList())
            is PropertyType.Enum -> PropertyValue.StringValue(
                type.possibleValues.firstOrNull() ?: ""
            )
            PropertyType.StringValue -> PropertyValue.StringValue("")
        }
    }

    /**
     * Resets all properties to their default values
     */
    fun resetToDefaults() {
        propertyDefinitions.forEach { definition ->
            definition.defaultValue?.asString()?.let { defaultValue ->
                properties.setProperty(definition.key, defaultValue)
            }
        }
        saveProperties()
        notifyObservers()
    }
}
