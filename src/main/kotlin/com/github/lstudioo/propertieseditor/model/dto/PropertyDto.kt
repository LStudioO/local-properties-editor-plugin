package com.github.lstudioo.propertieseditor.model.dto

import com.google.gson.annotations.SerializedName

/**
 * Data transfer object for a property definition.
 *
 * @property key The unique identifier of the property.
 * @property type The type of the property (e.g., "boolean", "array", "enum", "string").
 * @property defaultValue The default value for the property, if any.
 * @property description A human-readable description of the property.
 * @property values The list of possible values for the property, if it is an enum.
 */
data class PropertyDto(
    @SerializedName("key")
    val key: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("defaultValue")
    val defaultValue: Any?,
    @SerializedName("description")
    val description: String,
    @SerializedName("values")
    val values: List<String>?,
)
