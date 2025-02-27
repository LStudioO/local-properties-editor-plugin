package com.github.lstudioo.propertieseditor.utils

import io.mockk.junit4.MockKRule
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*

class OrderedPropertiesTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    private lateinit var tempPropertiesFile: File

    @Before
    fun setUp() {
        tempPropertiesFile = File.createTempFile("test", ".properties")
    }

    @After
    fun tearDown() {
        tempPropertiesFile.delete()
    }

    @Test
    fun `load and store should preserve property order`() {
        // Arrange
        val dateComment = generateDateComment()
        val propertiesContent = """
            $dateComment
            
            # First section
            first.property=value1
            second.property=value2
            
            # Second section
            third.property=value3
            fourth.property=value4
        """.trimIndent()
        tempPropertiesFile.writeText(propertiesContent)
        val sut = createSut()
        
        // Act
        sut.load(FileInputStream(tempPropertiesFile))
        val outputStream = ByteArrayOutputStream()
        sut.store(outputStream, null)
        val result = outputStream.toString()
        
        // Assert
        val resultLines = result.lines()
        
        // Check if header comment is preserved
        assertTrue("Header date comment should be preserved", result.contains(dateComment))
        
        // Check if section comments are preserved
        assertTrue("First section comment should be preserved", result.contains("# First section"))
        assertTrue("Second section comment should be preserved", result.contains("# Second section"))
        
        // Check property order
        val firstPropertyIndex = resultLines.indexOfFirst { it.startsWith("first.property") }
        val secondPropertyIndex = resultLines.indexOfFirst { it.startsWith("second.property") }
        val thirdPropertyIndex = resultLines.indexOfFirst { it.startsWith("third.property") }
        val fourthPropertyIndex = resultLines.indexOfFirst { it.startsWith("fourth.property") }
        
        assertTrue("first.property should be found in the result", firstPropertyIndex >= 0)
        assertTrue("second.property should be found in the result", secondPropertyIndex >= 0)
        assertTrue("third.property should be found in the result", thirdPropertyIndex >= 0)
        assertTrue("fourth.property should be found in the result", fourthPropertyIndex >= 0)
        
        assertTrue("second.property should come after first.property", secondPropertyIndex > firstPropertyIndex)
        assertTrue("third.property should come after second.property", thirdPropertyIndex > secondPropertyIndex)
        assertTrue("fourth.property should come after third.property", fourthPropertyIndex > thirdPropertyIndex)
    }

    @Test
    fun `load and store should preserve header comment`() {
        // Arrange
        val dateComment = generateDateComment()
        val propertiesContent = """
            $dateComment
            
            property1=value1
            property2=value2
        """.trimIndent()
        val sut = createSut()

        // Act
        sut.load(ByteArrayInputStream(propertiesContent.toByteArray()))
        val outputStream = ByteArrayOutputStream()
        sut.store(outputStream, "This comment should not appear")
        val result = outputStream.toString()
        
        // Assert
        assertTrue("Header date comment should be at the start of the file", result.startsWith(dateComment))
        assertFalse("Provided comment should not appear when header comment exists", result.contains("This comment should not appear"))
    }

    @Test
    fun `load and store should preserve property comments`() {
        // Arrange
        val dateComment = generateDateComment()
        val propertiesContent = """
            $dateComment
            
            # Comment for property1
            property1=value1
            
            # Comment for property2
            # with multiple lines
            property2=value2
        """.trimIndent()
        val sut = createSut()

        // Act
        sut.load(ByteArrayInputStream(propertiesContent.toByteArray()))
        val outputStream = ByteArrayOutputStream()
        sut.store(outputStream, null)
        val result = outputStream.toString()
        
        // Assert
        assertTrue("Comment for property1 should be preserved", result.contains("# Comment for property1"))
        assertTrue("Multi-line comment for property2 should be preserved", result.contains("# Comment for property2\n# with multiple lines"))
    }

    @Test
    fun `setProperty should maintain order of existing properties`() {
        // Arrange
        val dateComment = generateDateComment()
        val propertiesContent = """
            $dateComment
            property1=value1
            property2=value2
            property3=value3
        """.trimIndent()
        val sut = createSut()
        sut.load(ByteArrayInputStream(propertiesContent.toByteArray()))
        
        // Act
        sut.setProperty("property2", "new value")
        val outputStream = ByteArrayOutputStream()
        sut.store(outputStream, null)
        val result = outputStream.toString()
        
        // Assert
        val resultLines = result.lines()
        val property1Index = resultLines.indexOfFirst { it.startsWith("property1") }
        val property2Index = resultLines.indexOfFirst { it.startsWith("property2") }
        val property3Index = resultLines.indexOfFirst { it.startsWith("property3") }
        
        assertTrue("property1 should come before property2", property1Index < property2Index)
        assertTrue("property2 should come before property3", property2Index < property3Index)
        assertTrue("property2 should have the new value", result.contains("property2=new value"))
    }

    @Test
    fun `setProperty should add new properties at the end`() {
        // Arrange
        val dateComment = generateDateComment()
        val propertiesContent = """
            $dateComment
            property1=value1
            property2=value2
        """.trimIndent()
        val sut = createSut()
        sut.load(ByteArrayInputStream(propertiesContent.toByteArray()))
        
        // Act
        sut.setProperty("property3", "value3")
        val outputStream = ByteArrayOutputStream()
        sut.store(outputStream, null)
        val result = outputStream.toString()
        
        // Assert
        val resultLines = result.lines()
        val property1Index = resultLines.indexOfFirst { it.startsWith("property1") }
        val property2Index = resultLines.indexOfFirst { it.startsWith("property2") }
        val property3Index = resultLines.indexOfFirst { it.startsWith("property3") }
        
        assertTrue("property1 should come before property2", property1Index < property2Index)
        assertTrue("property2 should come before property3", property2Index < property3Index)
        assertTrue("property3 should be added to the properties", property3Index >= 0)
    }

    @Test
    fun `remove should remove property and its comment`() {
        // Arrange
        val dateComment = generateDateComment()
        val propertiesContent = """
            $dateComment
            
            # Comment for property1
            property1=value1
            
            # Comment for property2
            property2=value2
        """.trimIndent()
        val sut = createSut()
        sut.load(ByteArrayInputStream(propertiesContent.toByteArray()))
        
        // Act
        sut.remove("property1")
        val outputStream = ByteArrayOutputStream()
        sut.store(outputStream, null)
        val result = outputStream.toString()
        
        // Assert
        assertFalse("property1 should be removed", result.contains("property1="))
        assertFalse("Comment for property1 should be removed", result.contains("# Comment for property1"))
        assertTrue("Comment for property2 should still exist", result.contains("# Comment for property2"))
        assertTrue("property2 should still exist", result.contains("property2=value2"))
    }

    @Test
    fun `clear should remove all properties and comments`() {
        // Arrange
        val dateComment = generateDateComment()
        val propertiesContent = """
            $dateComment
            property1=value1
            property2=value2
        """.trimIndent()
        val sut = createSut()
        sut.load(ByteArrayInputStream(propertiesContent.toByteArray()))
        
        // Act
        sut.clear()
        val outputStream = ByteArrayOutputStream()
        sut.store(outputStream, null)
        val result = outputStream.toString()
        
        // Assert
        assertFalse("property1 should be removed", result.contains("property1="))
        assertFalse("property2 should be removed", result.contains("property2="))
        assertFalse("Header comment should be removed", result.contains(dateComment))
        assertTrue("Result should be empty or contain only minimal content", result.trim().isEmpty() || result.trim().startsWith("#"))
    }
    
    @Test
    fun `only first line date comment should be treated as header`() {
        // Arrange
        val dateComment = generateDateComment()
        val propertiesContent = """
            $dateComment
            
            # This is a regular comment
            # that should be associated with property1
            property1=value1
            
            # Comment for property2
            property2=value2
        """.trimIndent()
        val sut = createSut()

        // Act
        sut.load(ByteArrayInputStream(propertiesContent.toByteArray()))
        val outputStream = ByteArrayOutputStream()
        sut.store(outputStream, null)
        val result = outputStream.toString()
        
        // Assert
        assertTrue("Date comment should be at the top", result.startsWith(dateComment))
        
        // The comment for property1 should be preserved and associated with property1
        val property1Index = result.indexOf("property1=value1")
        val commentIndex = result.indexOf("# This is a regular comment")
        
        assertTrue("Comment for property1 should exist", commentIndex >= 0)
        assertTrue("Comment should be before property1", commentIndex < property1Index)
        assertTrue("Multi-line comment should be preserved", 
            result.contains("# This is a regular comment\n# that should be associated with property1"))
    }
    
    private fun createSut(): OrderedProperties {
        return OrderedProperties()
    }
    
    private fun generateDateComment(): String {
        val date = Date()
        val dayOfWeek = SimpleDateFormat("EEE", Locale.US).format(date)
        val month = SimpleDateFormat("MMM", Locale.US).format(date)
        val day = SimpleDateFormat("dd", Locale.US).format(date)
        val time = SimpleDateFormat("HH:mm:ss", Locale.US).format(date)
        val timezone = SimpleDateFormat("z", Locale.US).format(date)
        val year = SimpleDateFormat("yyyy", Locale.US).format(date)
        
        return "#$dayOfWeek $month $day $time $timezone $year"
    }
}
