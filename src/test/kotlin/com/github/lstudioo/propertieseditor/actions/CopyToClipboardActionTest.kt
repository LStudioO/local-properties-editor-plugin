package com.github.lstudioo.propertieseditor.actions

import com.github.lstudioo.propertieseditor.model.Property
import com.github.lstudioo.propertieseditor.model.PropertyValue
import com.github.lstudioo.propertieseditor.toolWindow.viewmodel.PropertyEditorViewModel
import com.github.lstudioo.propertieseditor.utils.ClipboardManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import io.mockk.*
import io.mockk.junit4.MockKRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CopyToClipboardActionTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @Test
    fun `actionPerformed should copy properties to clipboard`() {
        // Arrange
        val expectedText = "test.boolean=true\rtest.string=test value\r"
        val testProperties = listOf(
            Property(
                key = "test.boolean",
                value = PropertyValue.BooleanValue(true),
                description = "Test boolean property"
            ),
            Property(
                key = "test.string",
                value = PropertyValue.StringValue("test value"),
                description = "Test string property"
            )
        )
        val viewModel = mockk<PropertyEditorViewModel> {
            every { getProperties() } returns testProperties
        }
        val contentSlot = slot<String>()
        val action = createSut(
            viewModel = viewModel,
            clipboardManager = mockk {
                every { setClipboardContents(capture(contentSlot)) } just Runs
            }
        )
        mockkStatic(Messages::class)
        every { Messages.showInfoMessage(any(), any()) } just Runs

        // Act
        action.actionPerformed(mockk<AnActionEvent>(relaxed = true))

        // Assert
        verify(exactly = 1) { viewModel.getProperties() }
        // The first line is the header with the date
        val actualText = contentSlot.captured.split("\n").drop(1).joinToString("")
        assertEquals(expectedText, actualText)
    }

    private fun createSut(
        viewModel: PropertyEditorViewModel = mockk(),
        clipboardManager: ClipboardManager = mockk(),
    ): CopyToClipboardAction {
        return CopyToClipboardAction(viewModel, clipboardManager)
    }
}