package com.github.lstudioo.propertieseditor.toolWindow

import com.github.lstudioo.propertieseditor.LocalizationBundle
import com.github.lstudioo.propertieseditor.actions.SettingsAction
import com.github.lstudioo.propertieseditor.services.PropertyEditorService
import com.github.lstudioo.propertieseditor.settings.PropertyEditorSettings
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.*

/**
 * Welcome panel shown when no property file is found.
 * Provides a button to open settings and configure the property file path.
 */
class WelcomePanel(private val project: Project) : JPanel() {
    private val propertyService = project.service<PropertyEditorService>()
    private val settings = PropertyEditorSettings.getInstance(project)
    
    init {
        setupUI()
    }
    
    /**
     * Sets up the UI components for the welcome panel
     */
    private fun setupUI() {
        layout = BorderLayout()
        border = JBUI.Borders.empty(20)
        preferredSize = Dimension(500, 800)
        
        val contentPanel = createContentPanel()
        add(contentPanel, BorderLayout.CENTER)
        
        // Add component listener to handle resize events
        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                contentPanel.revalidate()
                contentPanel.repaint()
            }
        })
    }
    
    /**
     * Creates the main content panel with all UI elements
     */
    private fun createContentPanel(): JPanel {
        val contentPanel = JPanel()
        contentPanel.layout = BoxLayout(contentPanel, BoxLayout.Y_AXIS)
        contentPanel.border = JBUI.Borders.empty(10)
        contentPanel.alignmentX = CENTER_ALIGNMENT
        contentPanel.alignmentY = CENTER_ALIGNMENT
        
        // Add title
        val titleLabel = createTitleLabel()
        
        // Add status message
        val statusLabel = createStatusLabel()
        
        // Add property file button
        val addFileButton = createAddFileButton()
        
        // Steps panel
        val stepsPanel = createStepsPanel()
        stepsPanel.alignmentX = CENTER_ALIGNMENT
        stepsPanel.border = JBUI.Borders.emptyTop(20)
        
        // Add components to content panel
        contentPanel.add(Box.createVerticalGlue())
        contentPanel.add(titleLabel)
        contentPanel.add(statusLabel)
        contentPanel.add(addFileButton)
        contentPanel.add(stepsPanel)
        contentPanel.add(Box.createVerticalGlue())
        
        return contentPanel
    }
    
    /**
     * Creates the title label
     */
    private fun createTitleLabel(): JLabel {
        return JLabel(LocalizationBundle.message("ui.welcome.title")).apply {
            font = font.deriveFont(Font.BOLD, 18f)
            alignmentX = CENTER_ALIGNMENT
            border = JBUI.Borders.emptyBottom(20)
        }
    }
    
    /**
     * Creates the status label with appropriate message based on file path
     */
    private fun createStatusLabel(): JLabel {
        val fileSettings = propertyService.getFileSettings()
        val isFilePathDefault = settings.propertiesFilePath.isEmpty()
        
        val message = if (isFilePathDefault) 
            LocalizationBundle.message("ui.welcome.no_file_selected") 
        else 
            LocalizationBundle.message("ui.welcome.file_not_found", fileSettings.propertiesFile.path)
        
        return JLabel(message).apply {
            font = font.deriveFont(Font.ITALIC, 12f)
            foreground = JBColor.GRAY
            alignmentX = CENTER_ALIGNMENT
            border = JBUI.Borders.emptyBottom(20)
        }
    }
    
    /**
     * Creates the add file button that opens settings
     */
    private fun createAddFileButton(): JButton {
        return JButton(LocalizationBundle.message("ui.welcome.add_file")).apply {
            icon = AllIcons.General.Settings
            alignmentX = CENTER_ALIGNMENT
            addActionListener {
                openSettingsDialog()
            }
        }
    }
    
    /**
     * Opens the settings dialog using the SettingsAction
     */
    private fun openSettingsDialog() {
        val settingsAction = SettingsAction()
        val actionEvent = AnActionEvent.createFromDataContext(
            "WelcomePanel",
            null,
            DataContext { dataId -> if (dataId == "project") project else null }
        )
        settingsAction.actionPerformed(actionEvent)
    }
    
    /**
     * Creates the panel showing steps to configure the plugin
     */
    private fun createStepsPanel(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(10)
        
        // Title
        val titleLabel = JLabel(LocalizationBundle.message("ui.welcome.steps.title")).apply {
            font = font.deriveFont(Font.BOLD, 14f)
            horizontalAlignment = JLabel.CENTER
            border = JBUI.Borders.emptyBottom(10)
        }
        
        // Create a panel for steps with vertical BoxLayout
        val stepsContainer = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            
            // Add step labels
            add(createStepLabel("1", LocalizationBundle.message("ui.welcome.steps.step1")))
            add(createStepLabel("2", LocalizationBundle.message("ui.welcome.steps.step2")))
            add(createStepLabel("3", LocalizationBundle.message("ui.welcome.steps.step3")))
        }
        
        // Create a panel to center the steps container
        val centeringPanel = JPanel(FlowLayout(FlowLayout.CENTER, 0, 0))
        centeringPanel.add(stepsContainer)
        
        // Add components to panel
        panel.add(titleLabel, BorderLayout.NORTH)
        panel.add(centeringPanel, BorderLayout.CENTER)
        
        return panel
    }
    
    /**
     * Creates a label for a single step with number and description
     */
    private fun createStepLabel(number: String, text: String): JPanel {
        // Create main panel with BorderLayout
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.empty(2, 0) // Reduced vertical spacing
        
        // Number label
        val numberLabel = JLabel(number).apply {
            font = font.deriveFont(Font.BOLD)
            border = JBUI.Borders.emptyRight(10)
            preferredSize = Dimension(20, preferredSize.height)
            verticalAlignment = JLabel.TOP
        }
        
        // Text area for multiline text with wrapping
        val textArea = JTextArea(text).apply {
            lineWrap = true
            wrapStyleWord = true
            isEditable = false
            isOpaque = false
            font = UIManager.getFont("Label.font")
            border = null
            margin = JBUI.emptyInsets()
            columns = 30 // Set preferred width for the text area
        }
        
        // Add components to panel
        panel.add(numberLabel, BorderLayout.WEST)
        panel.add(textArea, BorderLayout.CENTER)
        
        return panel
    }
}