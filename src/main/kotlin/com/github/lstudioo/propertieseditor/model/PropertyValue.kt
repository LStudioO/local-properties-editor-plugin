package com.github.lstudioo.propertieseditor.model

/**
 * Sealed class representing the different types of property values.
 * This class provides a way to represent various types of property values in a type-safe manner.
 * It serves as a base class for different types of property values, including boolean, array, and string values.
 */
sealed class PropertyValue {
    /**
     * Represents a boolean property value.
     *
     * @property value The boolean value.
     */
    data class BooleanValue(val value: Boolean) : PropertyValue()

    /**
     * Represents an array property value.
     *
     * @property values The list of string values in the array.
     */
    data class ArrayValue(val values: List<String>) : PropertyValue()

    /**
     * Represents a string property value.
     *
     * @property value The string value.
     */
    data class StringValue(val value: String) : PropertyValue()
}

/**
 * Converts a PropertyValue to its string representation.
 * This is used when saving properties to the properties file.
 * The conversion is done based on the type of PropertyValue, ensuring that the resulting string is in the correct format.
 *
 * @return The string representation of the property value
 */
fun PropertyValue.asString(): String = when (this) {
    is PropertyValue.BooleanValue -> value.toString()
    is PropertyValue.ArrayValue -> values.distinct().sorted().joinToString(",")
    is PropertyValue.StringValue -> value
}
