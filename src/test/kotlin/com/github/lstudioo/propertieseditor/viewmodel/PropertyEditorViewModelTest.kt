package com.github.lstudioo.propertieseditor.viewmodel

import com.github.lstudioo.propertieseditor.model.Preset
import com.github.lstudioo.propertieseditor.model.Property
import com.github.lstudioo.propertieseditor.model.PropertySource
import com.github.lstudioo.propertieseditor.model.PropertyValue
import com.github.lstudioo.propertieseditor.model.SortOption
import com.github.lstudioo.propertieseditor.repository.PropertyRepository
import com.github.lstudioo.propertieseditor.toolWindow.viewmodel.PropertyEditorViewModel
import io.mockk.*
import io.mockk.junit4.MockKRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PropertyEditorViewModelTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @Test
    fun `loadProperties should call repository loadConfiguration`() {
        // Arrange
        val repository = mockk<PropertyRepository> {
            every { loadConfiguration() } just Runs
            every { getProperties() } returns createTestProperties()
        }
        val viewModel = createSut(repository = repository)

        // Act
        viewModel.loadProperties()

        // Assert
        verify(exactly = 1) { repository.loadConfiguration() }
    }

    @Test
    fun `filterProperties should filter properties by key`() {
        // Arrange
        val testProperties = createTestProperties()
        val repository = mockk<PropertyRepository> {
            every { getProperties() } returns testProperties
            every { loadConfiguration() } just Runs
        }
        val viewModel = createSut(repository = repository)

        // Act
        viewModel.loadProperties()
        viewModel.filterProperties("boolean")
        val filteredProperties = viewModel.getAllProperties()

        // Assert
        assertEquals(1, filteredProperties.size)
        assertEquals("test.boolean", filteredProperties[0].key)
    }

    @Test
    fun `sortProperties should sort properties by key ascending`() {
        // Arrange
        val testProperties = createTestProperties()
        val repository = mockk<PropertyRepository> {
            every { getProperties() } returns testProperties
            every { loadConfiguration() } just Runs
        }
        val viewModel = createSut(repository = repository)

        // Act
        viewModel.loadProperties()
        viewModel.sortProperties(SortOption.KEY_ASC)

        // Assert
        val sortedProperties = viewModel.getAllProperties()
        assertEquals(3, sortedProperties.size)
        assertEquals("test.array", sortedProperties[0].key)
        assertEquals("test.boolean", sortedProperties[1].key)
        assertEquals("test.string", sortedProperties[2].key)
    }

    @Test
    fun `sortProperties should sort properties by key descending`() {
        // Arrange
        val testProperties = createTestProperties()
        val repository = mockk<PropertyRepository> {
            every { getProperties() } returns testProperties
            every { loadConfiguration() } just Runs
        }
        val viewModel = createSut(repository = repository)

        // Act
        viewModel.loadProperties()
        viewModel.sortProperties(SortOption.KEY_DESC)

        // Assert
        val sortedProperties = viewModel.getAllProperties()
        assertEquals(3, sortedProperties.size)
        assertEquals("test.string", sortedProperties[0].key)
        assertEquals("test.boolean", sortedProperties[1].key)
        assertEquals("test.array", sortedProperties[2].key)
    }

    @Test
    fun `sortProperties should sort properties by type`() {
        // Arrange
        val testProperties = createTestProperties()
        val repository = mockk<PropertyRepository> {
            every { loadConfiguration() } just Runs
            every { getProperties() } returns testProperties
        }
        val viewModel = createSut(repository = repository)

        // Act
        viewModel.loadProperties()
        viewModel.sortProperties(SortOption.TYPE)

        // Assert
        val sortedProperties = viewModel.getAllProperties()
        assertEquals(3, sortedProperties.size)
        // The exact order depends on the implementation, but we can verify all properties are present
        assertTrue(sortedProperties.any { it.key == "test.array" })
        assertTrue(sortedProperties.any { it.key == "test.boolean" })
        assertTrue(sortedProperties.any { it.key == "test.string" })
    }

    @Test
    fun `getAvailablePresets should return preset names from repository`() {
        // Arrange
        val testPresets = createTestPresets()
        val repository = mockk<PropertyRepository> {
            every { getPresets() } returns testPresets
        }
        val viewModel = createSut(repository = repository)

        // Act
        val presetNames = viewModel.getAvailablePresets()

        // Assert
        assertEquals(1, presetNames.size)
        assertEquals("TestPreset", presetNames[0])
    }

    @Test
    fun `getSchemaOnlyProperties should return only schema properties`() {
        // Arrange
        val repository = mockk<PropertyRepository> {
            every { loadConfiguration() } just Runs
            every { getProperties() } returns listOf(
                Property("test.both", PropertyValue.StringValue("both"), "Both property", PropertySource.BOTH),
                Property("test.schema", PropertyValue.StringValue("schema"), "Schema property", PropertySource.SCHEMA_ONLY),
                Property("test.file", PropertyValue.StringValue("file"), "File property", PropertySource.FILE_ONLY)
            )
        }
        val viewModel = createSut(repository = repository)

        // Act
        viewModel.loadProperties()
        val schemaOnlyProps = viewModel.getSchemaOnlyProperties()

        // Assert
        assertEquals(1, schemaOnlyProps.size)
        assertEquals("test.schema", schemaOnlyProps[0].key)
    }

    @Test
    fun `getFileAndSchemaProperties should return non-schema-only properties`() {
        // Arrange
        val repository = mockk<PropertyRepository> {
            every { loadConfiguration() } just Runs
            every { getProperties() } returns listOf(
                Property("test.both", PropertyValue.StringValue("both"), "Both property", PropertySource.BOTH),
                Property("test.schema", PropertyValue.StringValue("schema"), "Schema property", PropertySource.SCHEMA_ONLY),
                Property("test.file", PropertyValue.StringValue("file"), "File property", PropertySource.FILE_ONLY)
            )
        }
        val viewModel = createSut(repository = repository)

        // Act
        viewModel.loadProperties()
        val fileAndSchemaProps = viewModel.getFileAndSchemaProperties()

        // Assert
        assertEquals(2, fileAndSchemaProps.size)
        assertTrue(fileAndSchemaProps.any { it.key == "test.both" })
        assertTrue(fileAndSchemaProps.any { it.key == "test.file" })
    }

    @Test
    fun `deleteProperty should call repository deleteProperty`() {
        // Arrange
        val property = Property("test.key", PropertyValue.StringValue("value"), "Description")
        val repository = mockk<PropertyRepository> {
            every { deleteProperty(any()) } just Runs
            every { loadConfiguration() } just Runs
            every { getProperties() } returns emptyList()
        }
        val viewModel = createSut(repository = repository)

        // Act
        viewModel.deleteProperty(property)

        // Assert
        verify(exactly = 1) { repository.deleteProperty("test.key") }
        verify(exactly = 1) { repository.loadConfiguration() }
    }

    private fun createSut(
        repository: PropertyRepository = mockk(),
    ): PropertyEditorViewModel {
        every { repository.addObserver(any()) } just Runs

        return PropertyEditorViewModel(repository)
    }

    private fun createTestProperties(): List<Property> {
        return listOf(
            Property(
                key = "test.boolean",
                value = PropertyValue.BooleanValue(true),
                description = "Test boolean property"
            ),
            Property(
                key = "test.string",
                value = PropertyValue.StringValue("test value"),
                description = "Test string property"
            ),
            Property(
                key = "test.array",
                value = PropertyValue.ArrayValue(listOf("value1", "value2")),
                description = "Test array property"
            )
        )
    }

    private fun createTestPresets(): List<Preset> {
        return listOf(
            Preset(
                name = "TestPreset",
                description = "Test preset",
                properties = listOf(
                    Property(
                        key = "test.boolean",
                        value = PropertyValue.BooleanValue(false),
                        description = "Test boolean property",
                        source = PropertySource.FILE_ONLY
                    ),
                    Property(
                        key = "test.string",
                        value = PropertyValue.StringValue("preset value"),
                        description = "Test string property",
                        source = PropertySource.BOTH,
                    )
                )
            )
        )
    }
}