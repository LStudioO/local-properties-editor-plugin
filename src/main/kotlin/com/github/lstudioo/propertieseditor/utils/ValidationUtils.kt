package com.github.lstudioo.propertieseditor.utils

import com.github.lstudioo.propertieseditor.model.*

object ValidationUtils {
    fun validatePropertyValue(value: PropertyValue, definition: PropertyDefinition): Boolean {
        return when (definition.type) {
            is PropertyType.Boolean -> value is PropertyValue.BooleanValue
            
            is PropertyType.Array -> {
                if (value !is PropertyValue.ArrayValue) return false
                if (value.values.isEmpty()) return true
                value.values.any { it in definition.type.possibleValues }
            }
            
            is PropertyType.Enum -> {
                if (value !is PropertyValue.StringValue) return false
                value.value in definition.type.possibleValues
            }
            
            is PropertyType.StringValue -> value is PropertyValue.StringValue
        }
    }

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
