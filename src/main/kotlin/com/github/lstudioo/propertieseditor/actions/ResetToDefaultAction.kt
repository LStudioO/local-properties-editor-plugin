package com.github.lstudioo.propertieseditor.actions

import com.github.lstudioo.propertieseditor.LocalizationBundle
import com.github.lstudioo.propertieseditor.toolWindow.viewmodel.PropertyEditorViewModel
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

/**
 * Action to reset the property values to their default values
 */
class ResetToDefaultAction(private val viewModel: PropertyEditorViewModel) :
    AnAction(
        LocalizationBundle.message("ui.reset"),
        LocalizationBundle.message("ui.reset.description"),
        AllIcons.Actions.Restart,
    ) {

    override fun actionPerformed(e: AnActionEvent) {
        val result = Messages.showYesNoDialog(
            LocalizationBundle.message("ui.reset.confirm"),
            LocalizationBundle.message("ui.reset.title"),
            Messages.getQuestionIcon()
        )

        if (result == Messages.YES) {
            try {
                viewModel.resetToDefaults()
                Messages.showInfoMessage(
                    LocalizationBundle.message("ui.reset.success"),
                    LocalizationBundle.message("ui.reset.complete")
                )
            } catch (ex: Exception) {
                Messages.showErrorDialog(
                    LocalizationBundle.message("ui.error.reset", ex.message.orEmpty()),
                    "Reset Failed"
                )
            }
        }
    }
}