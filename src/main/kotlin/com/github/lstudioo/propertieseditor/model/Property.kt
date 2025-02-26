package com.github.lstudioo.propertieseditor.model

/**
 * Represents a property with its key, value, and description.
 *
 * @property key The unique identifier of the property.
 * @property value The value of the property, can be boolean, array, or string.
 * @property description A human-readable description of the property.
 */
data class Property(
    val key: String,
    val value: PropertyValue,
    val description: String,
)
