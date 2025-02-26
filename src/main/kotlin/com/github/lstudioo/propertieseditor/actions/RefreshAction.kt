package com.github.lstudioo.propertieseditor.actions

import com.github.lstudioo.propertieseditor.LocalizationBundle
import com.github.lstudioo.propertieseditor.toolWindow.viewmodel.PropertyEditorViewModel
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Action to refresh the property editor's content
 */
class RefreshAction(private val viewModel: PropertyEditorViewModel) : AnAction(
    LocalizationBundle.message("ui.refresh"),
    LocalizationBundle.message("ui.refresh.description"),
    AllIcons.Actions.Refresh,
) {
    override fun actionPerformed(e: AnActionEvent) {
        viewModel.reloadConfiguration()
    }
}
