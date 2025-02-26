package com.github.lstudioo.propertieseditor.model.dto

import com.github.lstudioo.propertieseditor.model.*

class SchemaMapper {
    fun mapDefinition(dto: PropertyDto): PropertyDefinition {
        return PropertyDefinition(
            key = dto.key,
            type = when (dto.type.lowercase()) {
                "boolean" -> PropertyType.Boolean
                "array" -> PropertyType.Array(dto.values ?: emptyList())
                "enum" -> PropertyType.Enum(dto.values ?: emptyList())
                else -> PropertyType.StringValue
            },
            defaultValue = mapDefaultValue(dto.type, dto.defaultValue),
            description = dto.description
        )
    }

    private fun mapDefaultValue(type: String, value: Any?): PropertyValue? {
        if (value == null) return null

        return when (type.lowercase()) {
            "boolean" -> PropertyValue.BooleanValue(value as Boolean)
            "array" -> PropertyValue.ArrayValue((value as List<*>).mapNotNull { it?.toString() })
            else -> PropertyValue.StringValue(value.toString())
        }
    }

    fun mapPreset(dto: PresetDto, definitions: List<PropertyDefinition>): Preset {
        val properties = dto.properties.mapNotNull { (key, valueDto) ->
            val definition = definitions.find { it.key == key }
            definition?.let { def ->
                Property(
                    key = key,
                    value = mapPresetValue(def.type, valueDto.value),
                    description = def.description
                )
            }
        }

        return Preset(
            name = dto.name,
            description = dto.description,
            properties = properties
        )
    }

    private fun mapPresetValue(type: PropertyType, value: Any): PropertyValue {
        return when (type) {
            is PropertyType.Boolean -> PropertyValue.BooleanValue(value as Boolean)
            is PropertyType.Array -> PropertyValue.ArrayValue((value as List<*>).mapNotNull { it?.toString() })
            is PropertyType.Enum -> PropertyValue.StringValue(value.toString())
            is PropertyType.StringValue -> PropertyValue.StringValue(value.toString())
        }
    }
}