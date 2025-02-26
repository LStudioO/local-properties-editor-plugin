package com.github.lstudioo.propertieseditor.actions

import com.github.lstudioo.propertieseditor.toolWindow.viewmodel.PropertyEditorViewModel
import com.intellij.openapi.actionSystem.AnActionEvent
import io.mockk.*
import io.mockk.junit4.MockKRule
import org.junit.Rule
import org.junit.Test

class RefreshActionTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @Test
    fun `actionPerformed should call reloadConfiguration on viewModel`() {
        // Arrange
        val viewModel = mockk<PropertyEditorViewModel>(relaxed = true)
        val action = createSut(viewModel = viewModel)
        val mockEvent = mockk<AnActionEvent>()

        // Act
        action.actionPerformed(mockEvent)

        // Assert
        verify(exactly = 1) { viewModel.reloadConfiguration() }
    }

    private fun createSut(
        viewModel: PropertyEditorViewModel = mockk(relaxed = true)
    ): RefreshAction {
        return RefreshAction(viewModel)
    }
} 