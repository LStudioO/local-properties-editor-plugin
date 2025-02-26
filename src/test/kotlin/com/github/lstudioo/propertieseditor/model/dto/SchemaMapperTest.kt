package com.github.lstudioo.propertieseditor.model.dto

import com.github.lstudioo.propertieseditor.model.*
import io.mockk.junit4.MockKRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class SchemaMapperTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @Test
    fun `mapDefinition should map boolean property correctly`() {
        // Arrange
        val mapper = createSut()
        val dto = PropertyDto(
            key = "test.boolean",
            type = "boolean",
            defaultValue = true,
            description = "Test boolean property",
            values = null
        )
        
        // Act
        val definition = mapper.mapDefinition(dto)
        
        // Assert
        assertEquals("test.boolean", definition.key)
        assertTrue(definition.type is PropertyType.Boolean)
        assertTrue((definition.defaultValue as PropertyValue.BooleanValue).value)
        assertEquals("Test boolean property", definition.description)
    }

    @Test
    fun `mapDefinition should map array property correctly`() {
        // Arrange
        val mapper = createSut()
        val dto = PropertyDto(
            key = "test.array",
            type = "array",
            defaultValue = listOf("value1", "value2"),
            description = "Test array property",
            values = listOf("value1", "value2", "value3")
        )
        
        // Act
        val definition = mapper.mapDefinition(dto)
        
        // Assert
        assertEquals("test.array", definition.key)
        assertTrue(definition.type is PropertyType.Array)
        assertEquals(3, (definition.type as PropertyType.Array).possibleValues.size)
        assertTrue((definition.defaultValue as PropertyValue.ArrayValue).values.contains("value1"))
        assertTrue((definition.defaultValue as PropertyValue.ArrayValue).values.contains("value2"))
        assertEquals("Test array property", definition.description)
    }

    @Test
    fun `mapDefinition should map enum property correctly`() {
        // Arrange
        val mapper = createSut()
        val dto = PropertyDto(
            key = "test.enum",
            type = "enum",
            defaultValue = "value1",
            description = "Test enum property",
            values = listOf("value1", "value2", "value3")
        )
        
        // Act
        val definition = mapper.mapDefinition(dto)
        
        // Assert
        assertEquals("test.enum", definition.key)
        assertTrue(definition.type is PropertyType.Enum)
        assertEquals(3, (definition.type as PropertyType.Enum).possibleValues.size)
        assertEquals("value1", (definition.defaultValue as PropertyValue.StringValue).value)
        assertEquals("Test enum property", definition.description)
    }

    @Test
    fun `mapDefinition should map string property correctly`() {
        // Arrange
        val mapper = createSut()
        val dto = PropertyDto(
            key = "test.string",
            type = "string",
            defaultValue = "default value",
            description = "Test string property",
            values = null
        )
        
        // Act
        val definition = mapper.mapDefinition(dto)
        
        // Assert
        assertEquals("test.string", definition.key)
        assertTrue(definition.type is PropertyType.StringValue)
        assertEquals("default value", (definition.defaultValue as PropertyValue.StringValue).value)
        assertEquals("Test string property", definition.description)
    }

    @Test
    fun `mapPreset should map preset correctly`() {
        // Arrange
        val mapper = createSut()
        val presetDto = PresetDto(
            name = "TestPreset",
            description = "Test preset",
            properties = mapOf(
                "test.boolean" to PropertyValueDto(value = false),
                "test.string" to PropertyValueDto(value = "preset value")
            )
        )
        
        val definitions = listOf(
            PropertyDefinition(
                key = "test.boolean",
                type = PropertyType.Boolean,
                defaultValue = PropertyValue.BooleanValue(true),
                description = "Test boolean property"
            ),
            PropertyDefinition(
                key = "test.string",
                type = PropertyType.StringValue,
                defaultValue = PropertyValue.StringValue("default"),
                description = "Test string property"
            )
        )
        
        // Act
        val preset = mapper.mapPreset(presetDto, definitions)
        
        // Assert
        assertEquals("TestPreset", preset.name)
        assertEquals("Test preset", preset.description)
        assertEquals(2, preset.properties.size)
        
        val booleanProperty = preset.properties.find { it.key == "test.boolean" }
        assertNotNull(booleanProperty)
        assertFalse((booleanProperty!!.value as PropertyValue.BooleanValue).value)
        
        val stringProperty = preset.properties.find { it.key == "test.string" }
        assertNotNull(stringProperty)
        assertEquals("preset value", (stringProperty!!.value as PropertyValue.StringValue).value)
    }
    
    private fun createSut(): SchemaMapper {
        return SchemaMapper()
    }
} 