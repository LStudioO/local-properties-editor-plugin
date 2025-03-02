package com.github.lstudioo.propertieseditor.toolWindow

import com.github.lstudioo.propertieseditor.LocalizationBundle
import com.github.lstudioo.propertieseditor.actions.*
import com.github.lstudioo.propertieseditor.repository.PropertyRepository
import com.github.lstudioo.propertieseditor.services.PropertyEditorService
import com.github.lstudioo.propertieseditor.toolWindow.viewmodel.PropertyEditorViewModel
import com.github.lstudioo.propertieseditor.utils.SystemClipboardManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.util.messages.MessageBusConnection
import java.awt.BorderLayout
import java.io.File

/**
 * Factory class for creating the Property Editor tool window.
 * Responsible for initializing the tool window content and setting up listeners
 * for file system changes and settings changes.
 */
class PropertyEditorToolWindowFactory : ToolWindowFactory {
    private val logger = thisLogger()

    init {
        logger.info("Initializing Property Editor Tool Window")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        // Add settings action to the tool window toolbar
        addToolWindowActions(toolWindow)

        val propertyEditorWindow = PropertyEditorWindow(toolWindow)
        val propertyService = project.service<PropertyEditorService>()
        val fileSettings = propertyService.getFileSettings()

        // Use the file name for the tab title if file exists, otherwise show "No file selected"
        val tabTitle = getTabTitle(fileSettings.propertiesFile.exists(), fileSettings.propertiesFile.name)

        val content = ContentFactory.getInstance().createContent(
            propertyEditorWindow.getContent(),
            tabTitle,
            false,
        )
        toolWindow.contentManager.addContent(content)

        // Listen for file system changes to refresh the tool window when property file is created
        setupFileChangeListener(project, toolWindow, propertyEditorWindow)
    }

    /**
     * Returns the appropriate tab title based on whether the property file exists
     */
    private fun getTabTitle(fileExists: Boolean, fileName: String): String {
        return if (fileExists) {
            fileName
        } else {
            LocalizationBundle.message("ui.tab.no_file_selected")
        }
    }

    private fun addToolWindowActions(toolWindow: ToolWindow) {
        // Add settings action to the tool window toolbar
        val settingsAction = SettingsAction()
        toolWindow.setTitleActions(listOf(settingsAction))
    }

    private fun setupFileChangeListener(
        project: Project,
        toolWindow: ToolWindow,
        propertyEditorWindow: PropertyEditorWindow
    ) {
        val connection: MessageBusConnection = project.messageBus.connect()
        val propertyService = project.service<PropertyEditorService>()

        // Listen for file system changes
        connection.subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: List<VFileEvent>) {
                val fileSettings = propertyService.getFileSettings()

                // Check if any of the changed files is our property file
                val propertyFilePath = fileSettings.propertiesFile.absolutePath

                for (event in events) {
                    val file = event.file
                    val propertyFile = File(propertyFilePath)
                    if (file != null && FileUtil.filesEqual(propertyFile, File(file.path))) {
                        val fileExists = fileSettings.propertiesFile.exists()
                        val tabTitle = getTabTitle(fileExists, fileSettings.propertiesFile.name)
                        refreshToolWindowContent(toolWindow, propertyEditorWindow, tabTitle)
                        break
                    }
                }
            }
        })

        // Also listen for settings changes
        val settingsListener = project.messageBus.connect()
        settingsListener.subscribe(PropertyEditorSettingsListener.TOPIC, object : PropertyEditorSettingsListener {
            override fun settingsChanged() {
                val fileSettings = propertyService.getFileSettings()
                val fileExists = fileSettings.propertiesFile.exists()
                val tabTitle = getTabTitle(fileExists, fileSettings.propertiesFile.name)
                refreshToolWindowContent(toolWindow, propertyEditorWindow, tabTitle)
            }
        })
    }

    /**
     * Refreshes the tool window content with new content
     */
    private fun refreshToolWindowContent(
        toolWindow: ToolWindow,
        propertyEditorWindow: PropertyEditorWindow,
        tabTitle: String
    ) {
        toolWindow.contentManager.removeAllContents(true)
        val content = ContentFactory.getInstance().createContent(
            propertyEditorWindow.getContent(),
            tabTitle,
            false,
        )
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    /**
     * Inner class that manages the content of the Property Editor tool window.
     * Responsible for creating the toolbar and property editor panel.
     */
    class PropertyEditorWindow(toolWindow: ToolWindow) {
        private val project = toolWindow.project
        private val propertyService = project.service<PropertyEditorService>()
        private val viewModel = PropertyEditorViewModel(PropertyRepository(propertyService))
        private val clipboardManager = SystemClipboardManager()

        private fun createToolbar(): ActionToolbar {
            val actionGroup = DefaultActionGroup().apply {
                add(RefreshAction(viewModel))
                add(ResetToDefaultAction(viewModel))
                add(CopyToClipboardAction(viewModel, clipboardManager))
                add(OpenInEditorAction(propertyService))
            }

            return ActionManager.getInstance().createActionToolbar(
                "PropertyEditorToolbar",
                actionGroup,
                true,
            )
        }

        fun getContent(): DialogPanel {
            // Check if property file exists
            val fileSettings = propertyService.getFileSettings()
            val propertiesFileExists = fileSettings.propertiesFile.exists()

            return DialogPanel(BorderLayout()).apply {
                if (propertiesFileExists) {
                    // Show the toolbar only if the property file exists
                    val toolbar = createToolbar()
                    add(toolbar.component, BorderLayout.NORTH)

                    // Show the property editor panel if the file exists
                    add(PropertyEditorPanel(viewModel))
                } else {
                    // Show the welcome panel if the file doesn't exist
                    add(WelcomePanel(project))
                }
            }
        }
    }
}

/**
 * Interface for listening to settings changes
 */
interface PropertyEditorSettingsListener {
    fun settingsChanged()

    companion object {
        val TOPIC = com.intellij.util.messages.Topic.create(
            "PropertyEditorSettingsChanged",
            PropertyEditorSettingsListener::class.java
        )
    }
}
