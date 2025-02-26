package com.github.lstudioo.propertieseditor.actions

import com.github.lstudioo.propertieseditor.LocalizationBundle
import com.github.lstudioo.propertieseditor.model.Property
import com.github.lstudioo.propertieseditor.model.asString
import com.github.lstudioo.propertieseditor.toolWindow.viewmodel.PropertyEditorViewModel
import com.github.lstudioo.propertieseditor.utils.ClipboardManager
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import java.io.StringWriter
import java.util.*

/**
 * Action to copy the property values to clipboard
 */
class CopyToClipboardAction(
    private val viewModel: PropertyEditorViewModel,
    private val clipboardManager: ClipboardManager,
) :
    AnAction(
        LocalizationBundle.message("ui.copy"),
        LocalizationBundle.message("ui.copy.description"),
        AllIcons.Actions.Copy,
    ) {

    override fun actionPerformed(e: AnActionEvent) {
        val properties = mapToProperties(viewModel.getProperties())
        val text = properties.toStringFormat()
        clipboardManager.setClipboardContents(text)
        Messages.showInfoMessage(
            LocalizationBundle.message("ui.copy.success"),
            LocalizationBundle.message("ui.copy.title")
        )
    }

    private fun mapToProperties(properties: List<Property>): Properties {
        return Properties().apply {
            properties.forEach { setProperty(it.key, it.value.asString()) }
        }
    }

    private fun Properties.toStringFormat(): String {
        return StringWriter().use { writer ->
            this.store(writer, null)
            writer.toString()
        }
    }
}