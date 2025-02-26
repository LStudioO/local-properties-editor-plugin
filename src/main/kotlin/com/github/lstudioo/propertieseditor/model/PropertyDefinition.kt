package com.github.lstudioo.propertieseditor.model

/**
 * Defines the schema for a property.
 *
 * @property key The unique identifier of the property.
 * @property type The type of the property (Boolean, Array, Enum, or String).
 * @property defaultValue The default value for the property, if any.
 * @property description A human-readable description of the property.
 */
data class PropertyDefinition(
    val key: String,
    val type: PropertyType,
    val defaultValue: PropertyValue? = null,
    val description: String,
)
