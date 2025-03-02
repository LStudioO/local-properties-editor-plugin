package com.github.lstudioo.propertieseditor.toolWindow.viewmodel

import com.github.lstudioo.propertieseditor.LocalizationBundle
import com.github.lstudioo.propertieseditor.model.Property
import com.github.lstudioo.propertieseditor.model.PropertyDefinition
import com.github.lstudioo.propertieseditor.model.PropertySource
import com.github.lstudioo.propertieseditor.model.SortOption
import com.github.lstudioo.propertieseditor.repository.PropertyRepository
import com.github.lstudioo.propertieseditor.repository.PropertyRepositoryObserver
import com.github.lstudioo.propertieseditor.utils.ValidationUtils.getValidationMessage
import com.github.lstudioo.propertieseditor.utils.ValidationUtils.validatePropertyValue
import com.intellij.openapi.diagnostic.logger
import java.io.FileNotFoundException

/**
 * ViewModel for the Property Editor UI.
 * Handles loading, filtering, and modifying properties, while keeping the UI state.
 */
class PropertyEditorViewModel(
    private val repository: PropertyRepository,
) : PropertyRepositoryObserver {
    private val propertyListeners = mutableSetOf<(List<Property>) -> Unit>()
    private val errorListeners = mutableSetOf<(String?) -> Unit>()
    private val logger = logger<PropertyEditorViewModel>()

    // Properties state
    private var properties: List<Property> = emptyList()
    private var schemaOnlyProperties: List<Property> = emptyList()
    private var fileProperties: List<Property> = emptyList()
    
    // Original unfiltered properties
    private var originalProperties: List<Property> = emptyList()
    private var originalSchemaOnlyProperties: List<Property> = emptyList()
    
    // UI state
    private var errorMessage: String? = null
    private var currentPreset: String? = null
    private var currentSearchText: String = ""
    private var currentSortOption: SortOption = SortOption.NATURAL

    init {
        repository.addObserver(this)
    }

    override fun onPropertiesReloaded() {
        notifyPropertyListeners()
    }

    /**
     * Returns all properties (both from file and schema-only)
     */
    fun getAllProperties(): List<Property> = properties

    /**
     * Returns only schema-defined properties not present in the file
     */
    fun getSchemaOnlyProperties(): List<Property> = schemaOnlyProperties

    /**
     * Returns properties stored in the file (with or without schema definition)
     */
    fun getFileAndSchemaProperties(): List<Property> = fileProperties

    /**
     * Adds a listener that will be notified when properties change
     */
    fun addPropertyListener(listener: (List<Property>) -> Unit) {
        propertyListeners.add(listener)
        listener(properties)
    }

    /**
     * Adds a listener that will be notified when error state changes
     */
    fun addErrorListener(listener: (String?) -> Unit) {
        errorListeners.add(listener)
        listener(errorMessage)
    }

    /**
     * Loads properties from the repository and updates UI state
     */
    fun loadProperties() {
        try {
            clearError()
            repository.loadConfiguration()
            originalProperties = repository.getProperties()
            originalSchemaOnlyProperties = originalProperties.filter { it.source == PropertySource.SCHEMA_ONLY }
            updateFilteredAndSortedProperties()
        } catch (e: FileNotFoundException) {
            // File not found - this is handled by showing the welcome panel
            logger.info("Property file not found: ${e.message}")
            // Don't show error message as we'll display the welcome panel instead
            originalProperties = emptyList()
            originalSchemaOnlyProperties = emptyList()
            updateFilteredAndSortedProperties()
        } catch (e: Exception) {
            logger.warn("Failed to load properties", e)
            updateError(LocalizationBundle.message("ui.error.load", e.message.orEmpty()))
        }
    }

    /**
     * Updates a property in the repository
     */
    fun updateProperty(property: Property) {
        try {
            if (!validateProperty(property)) {
                val definition = getPropertyDefinition(property.key)
                val message = definition?.let { getValidationMessage(property.value, it) }
                updateError(message ?: LocalizationBundle.message("ui.error.import", property.key))
                return
            }
            repository.updateProperty(property)
            clearError()
        } catch (e: Exception) {
            logger.warn("Error updating property", e)
            updateError("Failed to update property: ${e.message}")
        }
    }

    /**
     * Gets property definition from the repository
     */
    fun getPropertyDefinition(key: String): PropertyDefinition? =
        repository.getPropertyDefinition(key)

    /**
     * Loads a preset of predefined properties
     */
    fun loadPreset(presetName: String) {
        try {
            val preset = repository.getPreset(presetName)
                ?: throw IllegalArgumentException("Unknown preset: $presetName")

            currentPreset = presetName
            preset.properties.forEach { property ->
                repository.updateProperty(property)
            }
            loadProperties()
            notifyPropertyListeners()
            clearError()
        } catch (e: Exception) {
            logger.warn("Failed to load preset: $presetName", e)
            updateError("Failed to load preset: ${e.message}")
        }
    }

    /**
     * Reloads the configuration from files
     */
    fun reloadConfiguration() {
        try {
            properties = emptyList()
            originalProperties = emptyList()
            loadProperties()
        } catch (e: Exception) {
            updateError("Failed to reload configuration. Please check settings: ${e.message}")
        }
    }

    /**
     * Returns the index of the currently selected preset
     */
    fun getSelectedPresetIndex(): Int? =
        currentPreset?.let { getAvailablePresets().indexOf(it) }

    /**
     * Returns all available presets
     */
    fun getAvailablePresets(): List<String> =
        repository.getPresets().map { it.name }

    private fun notifyPropertyListeners() {
        propertyListeners.forEach { it(properties) }
    }

    /**
     * Resets all properties to their default values
     */
    fun resetToDefaults() {
        try {
            repository.resetToDefaults()
            loadProperties()
            clearError()
        } catch (e: Exception) {
            logger.warn("Error resetting properties", e)
            updateError(LocalizationBundle.message("ui.error.reset", e.message.orEmpty()))
            throw e
        }
    }

    /**
     * Deletes a property from the file
     */
    fun deleteProperty(property: Property) {
        try {
            repository.deleteProperty(property.key)
            loadProperties()
            clearError()
        } catch (e: Exception) {
            logger.warn("Error deleting property: ${property.key}", e)
            updateError(LocalizationBundle.message("ui.error.delete", property.key))
            throw e
        }
    }

    /**
     * Filters properties based on search text
     */
    fun filterProperties(searchText: String) {
        currentSearchText = searchText
        updateFilteredAndSortedProperties()
    }

    /**
     * Sorts properties by the given option
     */
    fun sortProperties(sortOption: SortOption) {
        currentSortOption = sortOption
        updateFilteredAndSortedProperties()
    }

    /**
     * Updates the filtered and sorted properties based on current filters and sort options
     */
    private fun updateFilteredAndSortedProperties() {
        var filtered = if (currentSearchText.isEmpty()) {
            originalProperties
        } else {
            originalProperties.filter { property ->
                property.key.contains(currentSearchText, ignoreCase = true) ||
                property.value.toString().contains(currentSearchText, ignoreCase = true) ||
                property.description.contains(currentSearchText, ignoreCase = true)
            }
        }

        filtered = when (currentSortOption) {
            SortOption.NATURAL -> filtered
            SortOption.KEY_ASC -> filtered.sortedBy { it.key }
            SortOption.KEY_DESC -> filtered.sortedByDescending { it.key }
            SortOption.TYPE -> filtered.sortedBy { it.value.javaClass.simpleName }
        }

        updateProperties(filtered)
    }

    private fun updateProperties(newProperties: List<Property>) {
        properties = newProperties
        schemaOnlyProperties = newProperties.filter { it.source == PropertySource.SCHEMA_ONLY }
        fileProperties = newProperties.filter { it.source != PropertySource.SCHEMA_ONLY }
        propertyListeners.forEach { it(properties) }
    }

    private fun updateError(error: String?) {
        errorMessage = error
        errorListeners.forEach { it(errorMessage) }

        if (error != null) {
            logger.warn(error)
        }
    }

    private fun clearError() {
        updateError(null)
    }

    private fun validateProperty(property: Property): Boolean {
        val definition = getPropertyDefinition(property.key) ?: return true
        return validatePropertyValue(property.value, definition)
    }
}
