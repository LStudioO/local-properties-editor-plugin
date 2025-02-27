package com.github.lstudioo.propertieseditor.model

/**
 * Represents a property with its key, value, and description.
 *
 * @property key The unique identifier of the property.
 * @property value The value of the property, can be boolean, array, or string.
 * @property description A human-readable description of the property.
 * @property source Indicates the source of this property (from file, schema, or both).
 */
data class Property(
    val key: String,
    val value: PropertyValue,
    val description: String,
    val source: PropertySource = PropertySource.BOTH,
)

/**
 * Indicates the source of a property.
 *
 * FILE_ONLY - Property exists in the properties file but not in the schema
 * SCHEMA_ONLY - Property exists in the schema but not in the properties file
 * BOTH - Property exists in both the schema and properties file
 */
enum class PropertySource {
    FILE_ONLY,
    SCHEMA_ONLY,
    BOTH
}
