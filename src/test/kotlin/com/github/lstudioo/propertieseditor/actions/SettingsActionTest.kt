package com.github.lstudioo.propertieseditor.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import io.mockk.*
import io.mockk.junit4.MockKRule
import org.junit.Rule
import org.junit.Test

class SettingsActionTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @Test
    fun `actionPerformed should open settings dialog when project is available`() {
        // Arrange
        val action = createSut()
        val mockEvent = mockk<AnActionEvent>()
        val mockProject = mockk<Project>()
        
        every { mockEvent.project } returns mockProject
        
        mockkStatic(ShowSettingsUtil::class)
        val mockSettingsUtil = mockk<ShowSettingsUtil>()
        every { ShowSettingsUtil.getInstance() } returns mockSettingsUtil
        every { mockSettingsUtil.showSettingsDialog(any(), any<String>()) } just Runs
        
        // Act
        action.actionPerformed(mockEvent)
        
        // Assert
        verify(exactly = 1) { mockSettingsUtil.showSettingsDialog(mockProject, any<String>()) }
    }

    @Test
    fun `actionPerformed should do nothing when project is null`() {
        // Arrange
        val action = createSut()
        val mockEvent = mockk<AnActionEvent>()
        
        every { mockEvent.project } returns null
        
        mockkStatic(ShowSettingsUtil::class)
        val mockSettingsUtil = mockk<ShowSettingsUtil>()
        every { ShowSettingsUtil.getInstance() } returns mockSettingsUtil
        
        // Act
        action.actionPerformed(mockEvent)
        
        // Assert
        verify(exactly = 0) { mockSettingsUtil.showSettingsDialog(any(), any<String>()) }
    }
    
    private fun createSut(): SettingsAction {
        return SettingsAction()
    }
} 