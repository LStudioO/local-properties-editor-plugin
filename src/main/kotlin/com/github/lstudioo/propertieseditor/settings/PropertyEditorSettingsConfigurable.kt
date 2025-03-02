package com.github.lstudioo.propertieseditor.settings

import com.github.lstudioo.propertieseditor.LocalizationBundle
import com.github.lstudioo.propertieseditor.toolWindow.PropertyEditorSettingsListener
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import javax.swing.JComponent

/**
 * Configurable implementation for Property Editor settings.
 * 
 * This class integrates the Property Editor settings into the IntelliJ settings dialog,
 * handling the creation of the UI component and the application of settings changes.
 *
 * @property project The current project
 */
class PropertyEditorSettingsConfigurable(private val project: Project) : Configurable {
    private var settingsComponent: PropertyEditorSettingsComponent? = null

    override fun getDisplayName(): String = LocalizationBundle.message("ui.settings.title")

    override fun createComponent(): JComponent {
        settingsComponent = PropertyEditorSettingsComponent(project)
        return requireNotNull(settingsComponent).panel
    }

    override fun isModified(): Boolean {
        val settings = PropertyEditorSettings.getInstance(project)
        return settingsComponent?.let { component ->
            component.propertiesFilePath != settings.propertiesFilePath ||
                    component.configFilePath != settings.configFilePath
        } == true
    }

    override fun apply() {
        val settings = PropertyEditorSettings.getInstance(project)
        settingsComponent?.let { component ->
            settings.propertiesFilePath = component.propertiesFilePath
            settings.configFilePath = component.configFilePath
            
            // Notify listeners that settings have changed
            project.messageBus.syncPublisher(PropertyEditorSettingsListener.TOPIC).settingsChanged()
        }
    }

    override fun reset() {
        val settings = PropertyEditorSettings.getInstance(project)
        settingsComponent?.let { component ->
            component.propertiesFilePath = settings.propertiesFilePath
            component.configFilePath = settings.configFilePath
        }
    }

    override fun disposeUIResources() {
        settingsComponent = null
    }
}
