package com.github.lstudioo.propertieseditor.toolWindow.viewmodel

import com.github.lstudioo.propertieseditor.LocalizationBundle
import com.github.lstudioo.propertieseditor.model.Property
import com.github.lstudioo.propertieseditor.model.PropertyDefinition
import com.github.lstudioo.propertieseditor.model.SortOption
import com.github.lstudioo.propertieseditor.repository.PropertyRepository
import com.github.lstudioo.propertieseditor.repository.PropertyRepositoryObserver
import com.github.lstudioo.propertieseditor.utils.ValidationUtils.getValidationMessage
import com.github.lstudioo.propertieseditor.utils.ValidationUtils.validatePropertyValue
import com.intellij.openapi.diagnostic.logger

class PropertyEditorViewModel(
    private val repository: PropertyRepository,
) : PropertyRepositoryObserver {
    private val propertyListeners = mutableSetOf<(List<Property>) -> Unit>()
    private val errorListeners = mutableSetOf<(String?) -> Unit>()
    private val logger = logger<PropertyEditorViewModel>()

    private var properties: List<Property> = emptyList()
    private var errorMessage: String? = null
    private var currentPreset: String? = null
    private var currentSearchText: String = ""
    private var currentSortOption: SortOption = SortOption.NATURAL
    private var originalProperties: List<Property> = emptyList()

    init {
        repository.addObserver(this)
    }

    override fun onPropertiesReloaded() {
        notifyPropertyListeners()
    }

    fun getProperties(): List<Property> {
        return properties
    }

    fun addPropertyListener(listener: (List<Property>) -> Unit) {
        propertyListeners.add(listener)
        listener(properties)
    }

    fun addErrorListener(listener: (String?) -> Unit) {
        errorListeners.add(listener)
        listener(errorMessage)
    }

    private fun updateProperties(newProperties: List<Property>) {
        properties = newProperties
        propertyListeners.forEach { it(properties) }
    }

    private fun updateError(error: String?) {
        errorMessage = error
        errorListeners.forEach { it(errorMessage) }
        
        if (error != null) {
            logger.warn(error)
        }
    }

    private fun validateProperty(property: Property): Boolean {
        val definition = getPropertyDefinition(property.key) ?: return true
        return validatePropertyValue(property.value, definition)
    }

    fun loadProperties() {
        try {
            clearError()
            repository.loadConfiguration()
            originalProperties = repository.getProperties()
            updateFilteredAndSortedProperties()
        } catch (e: Exception) {
            logger.warn("Failed to load properties", e)
            updateError(LocalizationBundle.message("ui.error.load", e.message.orEmpty()))
        }
    }

    fun updateProperty(property: Property) {
        try {
            if (!validateProperty(property)) {
                val definition = getPropertyDefinition(property.key)
                val message = definition?.let { getValidationMessage(property.value, it) }
                updateError(message ?: LocalizationBundle.message("ui.error.import",  property.key))
                return
            }
            repository.updateProperty(property)
            clearError()
        } catch (e: Exception) {
            logger.warn("Error updating property", e)
            updateError("Failed to update property: ${e.message}")
        }
    }

    fun getPropertyDefinition(key: String): PropertyDefinition? =
        repository.getPropertyDefinition(key)

    private fun clearError() {
        updateError(null)
    }

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

    fun reloadConfiguration() {
        try {
            properties = emptyList()
            originalProperties = emptyList()
            loadProperties()
        } catch (e: Exception) {
            updateError("Failed to reload configuration. Please check settings: ${e.message}")
        }
    }

    fun getSelectedPresetIndex(): Int? =
        currentPreset?.let { getAvailablePresets().indexOf(it) }

    fun getAvailablePresets(): List<String> =
        repository.getPresets().map { it.name }

    private fun notifyPropertyListeners() {
        propertyListeners.forEach { it(properties) }
    }

    fun resetToDefaults() {
        try {
            repository.resetToDefaults()
            loadProperties()
            clearError()
        } catch (e: Exception) {
            updateError("Failed to reset properties: ${e.message}")
            throw e
        }
    }

    fun filterProperties(searchText: String) {
        currentSearchText = searchText
        updateFilteredAndSortedProperties()
    }

    fun sortProperties(sortOption: SortOption) {
        currentSortOption = sortOption
        updateFilteredAndSortedProperties()
    }

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
}

