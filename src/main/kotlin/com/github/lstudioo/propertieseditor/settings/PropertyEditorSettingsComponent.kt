package com.github.lstudioo.propertieseditor.settings

import com.github.lstudioo.propertieseditor.LocalizationBundle
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import javax.swing.JPanel

/**
 * UI component for the Property Editor settings.
 * 
 * This class provides the UI for configuring the Property Editor plugin settings,
 * including file path selection controls for properties and schema files.
 *
 * @property project The current project
 */
class PropertyEditorSettingsComponent(private val project: Project) {
    private val propertiesFileChooser = TextFieldWithBrowseButton()
    private val configFileChooser = TextFieldWithBrowseButton()

    /**
     * Path to the properties file.
     */
    var propertiesFilePath: String
        get() = propertiesFileChooser.text
        set(value) {
            propertiesFileChooser.text = value
        }

    /**
     * Path to the schema configuration file.
     */
    var configFilePath: String
        get() = configFileChooser.text
        set(value) {
            configFileChooser.text = value
        }

    /**
     * The settings panel containing the UI components.
     */
    val panel: JPanel

    init {
        // Configure file choosers
        configureFileChooser(
            propertiesFileChooser,
            LocalizationBundle.message("ui.settings.chooser.properties.title"),
            LocalizationBundle.message("ui.settings.chooser.properties.description"),
            project
        )

        configureFileChooser(
            configFileChooser,
            LocalizationBundle.message("ui.settings.chooser.config.title"),
            LocalizationBundle.message("ui.settings.chooser.config.description"),
            project
        )

        // Build the form
        panel = FormBuilder.createFormBuilder()
            .addLabeledComponent(
                JBLabel(LocalizationBundle.message("ui.settings.files-label.properties")),
                propertiesFileChooser,
            )
            .addLabeledComponent(
                JBLabel(LocalizationBundle.message("ui.settings.files-label.config")),
                configFileChooser
            )
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    /**
     * Configures a file chooser with the specified title, description, and project.
     *
     * @param fileChooser The file chooser to configure
     * @param title The title for the file chooser dialog
     * @param description The description for the file chooser dialog
     * @param project The current project
     */
    private fun configureFileChooser(
        fileChooser: TextFieldWithBrowseButton,
        title: String,
        description: String,
        project: Project
    ) {
        val fileChooserDescriptor = FileChooserDescriptor(true, false, false, false, false, false)
            .withTitle(title)
            .withDescription(description)
        
        fileChooser.addBrowseFolderListener(
            title,
            description,
            project,
            fileChooserDescriptor
        )
    }
}
