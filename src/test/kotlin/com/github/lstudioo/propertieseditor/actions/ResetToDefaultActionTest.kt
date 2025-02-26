package com.github.lstudioo.propertieseditor.actions

import com.github.lstudioo.propertieseditor.toolWindow.viewmodel.PropertyEditorViewModel
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import io.mockk.*
import io.mockk.junit4.MockKRule
import org.junit.Rule
import org.junit.Test

class ResetToDefaultActionTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @Test
    fun `actionPerformed should reset to defaults when user confirms`() {
        // Arrange
        val viewModel = mockk<PropertyEditorViewModel>(relaxed = true)
        val action = createSut(viewModel = viewModel)
        val mockEvent = mockk<AnActionEvent>()
        
        // Mock Messages.showYesNoDialog to return YES
        mockkStatic(Messages::class)
        every { Messages.showYesNoDialog(any(), any(), any()) } returns Messages.YES
        every { Messages.getQuestionIcon() } returns AllIcons.Actions.Restart
        every { Messages.showInfoMessage(any(), any()) } just Runs
        
        // Act
        action.actionPerformed(mockEvent)
        
        // Assert
        verify(exactly = 1) { viewModel.resetToDefaults() }
        verify(exactly = 1) { Messages.showInfoMessage(any(), any()) }
    }

    @Test
    fun `actionPerformed should not reset to defaults when user cancels`() {
        // Arrange
        val viewModel = mockk<PropertyEditorViewModel>(relaxed = true)
        val action = createSut(viewModel = viewModel)
        val mockEvent = mockk<AnActionEvent>()
        
        // Mock Messages.showYesNoDialog to return NO
        mockkStatic(Messages::class)
        every { Messages.showYesNoDialog(any(), any(), any()) } returns Messages.NO
        every { Messages.getQuestionIcon() } returns AllIcons.Actions.Restart
        
        // Act
        action.actionPerformed(mockEvent)
        
        // Assert
        verify(exactly = 0) { viewModel.resetToDefaults() }
        verify(exactly = 0) { Messages.showInfoMessage(any(), any()) }
    }

    @Test
    fun `actionPerformed should show error message when reset fails`() {
        // Arrange
        val viewModel = mockk<PropertyEditorViewModel>()
        val action = createSut(viewModel = viewModel)
        val mockEvent = mockk<AnActionEvent>()
        
        // Mock Messages.showYesNoDialog to return YES
        mockkStatic(Messages::class)
        every { Messages.showYesNoDialog(any(), any(), any()) } returns Messages.YES
        every { Messages.getQuestionIcon() } returns AllIcons.Actions.Restart
        every { Messages.showErrorDialog(any<String>(), any()) } just Runs
        
        // Make resetToDefaults throw an exception
        every { viewModel.resetToDefaults() } throws Exception("Test error")
        
        // Act
        action.actionPerformed(mockEvent)
        
        // Assert
        verify(exactly = 1) { viewModel.resetToDefaults() }
        verify(exactly = 1) { Messages.showErrorDialog(any<String>(), any()) }
    }
    
    private fun createSut(
        viewModel: PropertyEditorViewModel = mockk()
    ): ResetToDefaultAction {
        return ResetToDefaultAction(viewModel)
    }
} 