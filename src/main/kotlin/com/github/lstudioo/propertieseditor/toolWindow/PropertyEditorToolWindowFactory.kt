package com.github.lstudioo.propertieseditor.toolWindow

import com.github.lstudioo.propertieseditor.actions.CopyToClipboardAction
import com.github.lstudioo.propertieseditor.actions.SettingsAction
import com.github.lstudioo.propertieseditor.actions.RefreshAction
import com.github.lstudioo.propertieseditor.actions.ResetToDefaultAction
import com.github.lstudioo.propertieseditor.repository.PropertyRepository
import com.github.lstudioo.propertieseditor.services.PropertyEditorService
import com.github.lstudioo.propertieseditor.toolWindow.viewmodel.PropertyEditorViewModel
import com.github.lstudioo.propertieseditor.utils.SystemClipboardManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.actionSystem.*
import java.awt.BorderLayout

class PropertyEditorToolWindowFactory : ToolWindowFactory {
    private val logger = thisLogger()

    init {
        logger.info("Initializing Property Editor Tool Window")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val propertyEditorWindow = PropertyEditorWindow(toolWindow)
        val content = ContentFactory.getInstance().createContent(
            propertyEditorWindow.getContent(),
            null,
            false,
        )
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class PropertyEditorWindow(toolWindow: ToolWindow) {
        private val propertyService = toolWindow.project.service<PropertyEditorService>()
        private val viewModel = PropertyEditorViewModel(PropertyRepository(propertyService))
        private val clipboardManager = SystemClipboardManager()

        private fun createToolbar(): ActionToolbar {
            val actionGroup = DefaultActionGroup().apply {
                add(SettingsAction())
                add(RefreshAction(viewModel))
                add(ResetToDefaultAction(viewModel))
                add(CopyToClipboardAction(viewModel, clipboardManager))
            }
            
            return ActionManager.getInstance().createActionToolbar(
                "PropertyEditorToolbar",
                actionGroup,
                true,
            )
        }

        fun getContent(): DialogPanel {
            val toolbar = createToolbar()
            val panel = PropertyEditorPanel(viewModel)
            
            return DialogPanel(BorderLayout()).apply {
                add(toolbar.component, BorderLayout.NORTH)
                add(panel)
            }
        }
    }
}
