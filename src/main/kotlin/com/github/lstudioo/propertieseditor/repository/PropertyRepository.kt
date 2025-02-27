package com.github.lstudioo.propertieseditor.repository

import com.github.lstudioo.propertieseditor.model.*
import com.github.lstudioo.propertieseditor.model.dto.PresetDto
import com.github.lstudioo.propertieseditor.model.dto.SchemaDto
import com.github.lstudioo.propertieseditor.model.dto.SchemaMapper
import com.github.lstudioo.propertieseditor.services.PropertyEditorService
import com.google.gson.Gson
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File
import java.util.*

class PropertyRepository(
    private val propertyEditorService: PropertyEditorService,
) {
    private val mapper = SchemaMapper()
    private val properties = Properties()
    private val gson = Gson()
    private val observers = mutableListOf<PropertyRepositoryObserver>()
    private var propertyDefinitions: List<PropertyDefinition> = emptyList()
    private var presets: List<Preset> = emptyList()

    private val configFile: File
        get() = propertyEditorService.getFileSettings().configFile

    private val propertiesFile: File
        get() = propertyEditorService.getFileSettings().propertiesFile

    fun loadConfiguration() {
        loadSchema()
        loadProperties()
        notifyObservers()
    }

    fun addObserver(observer: PropertyRepositoryObserver) {
        observers.add(observer)
    }

    private fun notifyObservers() {
        observers.forEach { it.onPropertiesReloaded() }
    }

    // Loads and parses the JSON schema
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

    // Loads properties from the local.properties file
    private fun loadProperties() {
        properties.clear()
        propertiesFile.inputStream().use { properties.load(it) }
    }

    // Gets all properties with their current values
    fun getProperties(): List<Property> {
        return buildList {
            addAll(
                propertyDefinitions.mapNotNull { definition ->
                    if (!properties.containsKey(definition.key)) {
                        null
                    } else {
                        Property(
                            key = definition.key,
                            value = getPropertyValue(definition),
                            description = definition.description
                        )
                    }
                }
            )
            addAll(
                properties.filterNot { property -> propertyDefinitions.any { it.key == property.key } }.map { p ->
                    Property(
                        key = p.key.toString(),
                        value = PropertyValue.StringValue(p.value.toString()),
                        description = "Undefined in the schema"
                    )
                }
            )
        }
    }

    fun getPresets(): List<Preset> = presets

    fun getPreset(name: String): Preset? = presets.find { it.name == name }

    fun getPropertyDefinition(key: String): PropertyDefinition? =
        propertyDefinitions.find { it.key == key }

    fun updateProperty(property: Property) {
        properties.setProperty(property.key, property.value.asString())
        saveProperties()
        notifyTheIdeAboutFileUpdates()
    }

    private fun notifyTheIdeAboutFileUpdates() {
        val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(propertiesFile)
        virtualFile?.refresh(false, false)
    }

    private fun saveProperties() {
        propertiesFile.outputStream().use { properties.store(it, null) }
    }

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

    fun resetToDefaults() {
        propertyDefinitions.forEach { definition ->
            properties.setProperty(definition.key, definition.defaultValue?.asString())
        }
        saveProperties()
        notifyObservers()
    }
}
