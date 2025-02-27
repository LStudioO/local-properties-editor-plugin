package com.github.lstudioo.propertieseditor.utils

import com.github.lstudioo.propertieseditor.model.*

/**
 * Utility class for validating property values against their definitions.
 * Provides methods to check if a value matches the expected type and constraints.
 */
object ValidationUtils {
    /**
     * Validates a property value against its definition.
     *
     * @param value The property value to validate
     * @param definition The property definition containing type information
     * @return True if the value is valid according to the definition, false otherwise
     */
    fun validatePropertyValue(value: PropertyValue, definition: PropertyDefinition): Boolean {
        return when (definition.type) {
            is PropertyType.Boolean -> value is PropertyValue.BooleanValue
            
            is PropertyType.Array -> {
                if (value !is PropertyValue.ArrayValue) return false
                // Empty arrays are considered valid
                if (value.values.isEmpty()) return true
                // At least one value must be in the list of possible values
                value.values.any { it in definition.type.possibleValues }
            }
            
            is PropertyType.Enum -> {
                if (value !is PropertyValue.StringValue) return false
                // Value must be one of the possible enum values
                value.value in definition.type.possibleValues
            }
            
            is PropertyType.StringValue -> value is PropertyValue.StringValue
        }
    }

    /**
     * Gets a validation error message based on the property value and definition.
     *
     * @param value The property value to validate
     * @param definition The property definition containing type information
     * @return A validation error message if the value is invalid, null otherwise
     */
    fun getValidationMessage(value: PropertyValue, definition: PropertyDefinition): String? {
        return when {
            !validatePropertyValue(value, definition) -> {
                when (definition.type) {
                    is PropertyType.Array -> "Invalid array values. Allowed values: ${definition.type.possibleValues}"
                    is PropertyType.Enum -> "Invalid enum value. Allowed values: ${definition.type.possibleValues}"
                    is PropertyType.Boolean -> "Value must be boolean"
                    is PropertyType.StringValue -> "Value must be string"
                }
            }
            else -> null
        }
    }
}
