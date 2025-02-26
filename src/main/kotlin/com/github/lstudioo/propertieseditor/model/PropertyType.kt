package com.github.lstudioo.propertieseditor.model

/**
 * Represents the type and constraints for a property.
 * Used to define the valid data types and possible values for configuration properties.
 */
sealed class PropertyType {
    /** Represents a boolean property that can be true or false */
    data object Boolean : PropertyType()

    /** Represents an array property with a predefined list of possible values */
    data class Array(val possibleValues: List<String>) : PropertyType()

    /** Represents an enumeration property with a predefined list of possible values */
    data class Enum(val possibleValues: List<String>) : PropertyType()

    /** Represents a string property that can contain any text value */
    data object StringValue : PropertyType()
}