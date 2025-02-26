package com.github.lstudioo.propertieseditor.model.dto

import com.google.gson.annotations.SerializedName

/**
 * Data transfer object for a preset configuration.
 *
 * @property name The name of the preset.
 * @property description The description of the preset.
 * @property properties The map of property keys to their corresponding property value DTOs.
 */
data class PresetDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("properties")
    val properties: Map<String, PropertyValueDto>,
)

/**
 * Data transfer object for a property value.
 *
 * @property value The value of the property.
 */
data class PropertyValueDto(
    @SerializedName("value")
    val value: Any,
)
