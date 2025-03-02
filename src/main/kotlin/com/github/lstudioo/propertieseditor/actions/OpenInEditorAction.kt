package com.github.lstudioo.propertieseditor.actions

import com.github.lstudioo.propertieseditor.LocalizationBundle
import com.github.lstudioo.propertieseditor.services.PropertyEditorService
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.vfs.LocalFileSystem

/**
 * Action to open the property file in the IDE editor
 */
class OpenInEditorAction(private val propertyEditorService: PropertyEditorService) : AnAction(
    LocalizationBundle.message("ui.open.editor"),
    LocalizationBundle.message("ui.open.editor.description"),
    AllIcons.Actions.EditSource,
) {
    override fun actionPerformed(e: AnActionEvent) {
        e.project?.let { project ->
            val propertiesFile = propertyEditorService.getFileSettings().propertiesFile
            
            // Find the virtual file in the local file system
            val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(propertiesFile)
            
            // Open the file in the editor if it exists
            virtualFile?.let {
                OpenFileDescriptor(project, it).navigate(true)
            }
        }
    }
}
