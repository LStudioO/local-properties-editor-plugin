package com.github.lstudioo.propertieseditor.repository

import com.github.lstudioo.propertieseditor.model.FileSettings
import com.github.lstudioo.propertieseditor.model.Property
import com.github.lstudioo.propertieseditor.model.PropertySource
import com.github.lstudioo.propertieseditor.model.PropertyValue
import com.github.lstudioo.propertieseditor.services.PropertyEditorService
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import io.mockk.*
import io.mockk.junit4.MockKRule
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.util.Properties

class PropertyRepositoryTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    private val tempPropertiesFile = File.createTempFile("test", ".properties")
    private val tempConfigFile = File.createTempFile("test", ".json")
    private val mockObserver = mockk<PropertyRepositoryObserver>(relaxed = true)
    private val mockVirtualFile = mockk<VirtualFile>(relaxed = true)
    private val mockFileSystem = mockk<LocalFileSystem>()

    @After
    fun tearDown() {
        tempPropertiesFile.delete()
        tempConfigFile.delete()
    }

    @Test
    fun `loadConfiguration should load schema and properties and notify observers`() {
        // Arrange
        writePropertiesToFile(mapOf(
            "test.boolean" to "true",
            "test.string" to "test value"
        ))
        val repository = createSut()
        
        // Act
        repository.loadConfiguration()
        
        // Assert
        verify(exactly = 1) { mockObserver.onPropertiesReloaded() }
        val properties = repository.getProperties()
        assertEquals(2, properties.size)
        
        val booleanProperty = properties.find { it.key == "test.boolean" }
        assertNotNull(booleanProperty)
        assertTrue((booleanProperty!!.value as PropertyValue.BooleanValue).value)
        
        val stringProperty = properties.find { it.key == "test.string" }
        assertNotNull(stringProperty)
        assertEquals("test value", (stringProperty!!.value as PropertyValue.StringValue).value)
    }

    @Test
    fun `getPresets should return loaded presets`() {
        // Arrange
        writePropertiesToFile(mapOf(
            "test.boolean" to "true",
            "test.string" to "test value"
        ))
        val repository = createSut()
        repository.loadConfiguration()
        
        // Act
        val presets = repository.getPresets()
        
        // Assert
        assertEquals(1, presets.size)
        assertEquals("TestPreset", presets[0].name)
        assertEquals("Test preset", presets[0].description)
        assertEquals(2, presets[0].properties.size)
    }

    @Test
    fun `updateProperty should save property to file`() {
        // Arrange
        writePropertiesToFile(mapOf(
            "test.boolean" to "true",
            "test.string" to "test value"
        ))
        val repository = createSut()
        repository.loadConfiguration()
        
        val property = Property(
            key = "test.string",
            value = PropertyValue.StringValue("updated value"),
            description = "Test string property"
        )
        
        // Act
        repository.updateProperty(property)
        
        // Assert
        val props = Properties()
        props.load(tempPropertiesFile.inputStream())
        assertEquals("updated value", props.getProperty("test.string"))
        verify(exactly = 1) { mockVirtualFile.refresh(false, false) }
    }

    @Test
    fun `resetToDefaults should reset properties to default values`() {
        // Arrange
        writePropertiesToFile(mapOf(
            "test.boolean" to "false",
            "test.string" to "test value"
        ))
        val repository = createSut()
        repository.loadConfiguration()
        
        // Act
        repository.resetToDefaults()
        
        // Assert
        verify(exactly = 2) { mockObserver.onPropertiesReloaded() } // Once for loadConfiguration, once for reset
        
        // Read the properties file directly to verify
        val props = Properties()
        props.load(tempPropertiesFile.inputStream())
        assertEquals("true", props.getProperty("test.boolean"))
        assertEquals("default", props.getProperty("test.string"))
    }

    @Test
    fun `getProperties should identify properties from schema not in file`() {
        // Arrange
        // Only add one property to the file but the schema has two
        writePropertiesToFile(mapOf(
            "test.boolean" to "true"
        ))
        val repository = createSut()
        repository.loadConfiguration()
        
        // Act
        val properties = repository.getProperties()
        
        // Assert
        assertEquals(2, properties.size)
        // Property in file and schema
        val fileProperty = properties.find { it.key == "test.boolean" }
        assertNotNull(fileProperty)
        assertEquals(PropertySource.BOTH, fileProperty?.source)
        
        // Property in schema but not in file
        val schemaProperty = properties.find { it.key == "test.string" }
        assertNotNull(schemaProperty)
        assertEquals(PropertySource.SCHEMA_ONLY, schemaProperty?.source)
    }

    @Test
    fun `deleteProperty should remove property from properties file`() {
        // Arrange
        writePropertiesToFile(mapOf(
            "test.boolean" to "true",
            "test.string" to "test value"
        ))
        val repository = createSut()
        repository.loadConfiguration()
        
        // Mock file system interactions
        mockkStatic(LocalFileSystem::class)
        every { LocalFileSystem.getInstance() } returns mockFileSystem
        every { mockFileSystem.findFileByIoFile(any()) } returns mockVirtualFile
        
        // Act
        repository.deleteProperty("test.boolean")
        
        // Assert
        verify(exactly = 1) { mockVirtualFile.refresh(false, false) }
        verify(exactly = 2) { mockObserver.onPropertiesReloaded() } // Once for loadConfiguration, once for deleteProperty
        
        // Verify property was removed
        val properties = Properties()
        properties.load(tempPropertiesFile.inputStream())
        assertFalse(properties.containsKey("test.boolean"))
        assertTrue(properties.containsKey("test.string"))
    }
    
    private fun createSut(
        service: PropertyEditorService = mockk<PropertyEditorService>(),
    ): PropertyRepository {
        mockkStatic(LocalFileSystem::class)
        every { LocalFileSystem.getInstance() } returns mockFileSystem
        every { mockFileSystem.findFileByIoFile(any()) } returns mockVirtualFile
        
        every { service.getFileSettings() } returns FileSettings(
            propertiesFile = tempPropertiesFile,
            configFile = tempConfigFile
        )
        
        // Write test schema to config file
        tempConfigFile.writeText("""
            {
                "properties": [
                    {
                        "key": "test.boolean",
                        "type": "boolean",
                        "defaultValue": true,
                        "description": "Test boolean property"
                    },
                    {
                        "key": "test.string",
                        "type": "string",
                        "defaultValue": "default",
                        "description": "Test string property"
                    }
                ],
                "presets": [
                    {
                        "name": "TestPreset",
                        "description": "Test preset",
                        "properties": {
                            "test.boolean": { "value": false },
                            "test.string": { "value": "preset value" }
                        }
                    }
                ]
            }
        """.trimIndent())
        
        return PropertyRepository(service).apply {
            addObserver(mockObserver)
        }
    }
    
    private fun writePropertiesToFile(props: Map<String, String>) {
        val properties = Properties()
        props.forEach { (key, value) -> properties.setProperty(key, value) }
        properties.store(FileOutputStream(tempPropertiesFile), null)
    }
}
