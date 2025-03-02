package com.github.lstudioo.propertieseditor.actions

import com.github.lstudioo.propertieseditor.LocalizationBundle
import com.github.lstudioo.propertieseditor.toolWindow.PropertyEditorSettingsListener
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil

/**
 * Action to open the Property Editor settings dialog
 */
class SettingsAction : AnAction(
    LocalizationBundle.message("ui.settings"),
    LocalizationBundle.message("ui.settings.description"),
    AllIcons.General.Settings,
) {
    override fun actionPerformed(e: AnActionEvent) {
        e.project?.let { project ->
            // Show settings dialog
            ShowSettingsUtil.getInstance().showSettingsDialog(
                project,
                LocalizationBundle.message("ui.settings.title")
            )
            
            // Notify listeners that settings may have changed
            project.messageBus.syncPublisher(PropertyEditorSettingsListener.TOPIC).settingsChanged()
        }
    }
}
