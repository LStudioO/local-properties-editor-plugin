package com.github.lstudioo.propertieseditor.model

/**
 * Sealed class representing the different types of property values.
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

fun PropertyValue.asString(): String = when (this) {
    is PropertyValue.BooleanValue -> value.toString()
    is PropertyValue.ArrayValue -> values.distinct().sorted().joinToString(",")
    is PropertyValue.StringValue -> value
}
