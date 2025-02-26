package com.github.lstudioo.propertieseditor.model.dto

import com.google.gson.annotations.SerializedName

/**
 * Data class representing the schema of the properties, including the property definitions and presets.
 *
 * @property properties The list of property definitions.
 * @property presets The list of presets, if any.
 */
data class SchemaDto(
    /**
     * The list of property definitions.
     */
    @SerializedName("properties")
    val properties: List<PropertyDto>,

    /**
     * The list of presets, if any.
     */
    @SerializedName("presets")
    val presets: List<PresetDto>? = emptyList(),
)
