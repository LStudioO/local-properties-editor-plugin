package com.github.lstudioo.propertieseditor.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

/**
 * Persistent settings for the Property Editor plugin.
 * 
 * This service stores user configuration for the Property Editor, including file paths
 * for properties and schema files. Settings are persisted per-project and automatically
 * saved/loaded by the IntelliJ platform.
 */
@Service(Service.Level.PROJECT)
@State(
    name = "PropertyEditorSettings",
    storages = [Storage("propertyEditorSettings.xml")]
)
class PropertyEditorSettings : PersistentStateComponent<PropertyEditorSettings.State> {
    /**
     * Data class representing the persisted state of the settings.
     *
     * @property propertiesFilePath Path to the properties file
     * @property configFilePath Path to the schema configuration file
     */
    data class State(
        var propertiesFilePath: String = "",
        var configFilePath: String = ""
    )

    private var myState = State()

    override fun getState(): State = myState.copy()

    override fun loadState(state: State) {
        myState = state
    }

    /**
     * Path to the properties file.
     * An empty string indicates that the default path should be used.
     */
    var propertiesFilePath: String
        get() = myState.propertiesFilePath
        set(value) {
            myState.propertiesFilePath = value
        }

    /**
     * Path to the schema configuration file.
     * An empty string indicates that the default path should be used.
     */
    var configFilePath: String
        get() = myState.configFilePath
        set(value) {
            myState.configFilePath = value
        }

    companion object {
        /**
         * Gets the settings instance for the specified project.
         *
         * @param project The project for which to get settings
         * @return The PropertyEditorSettings instance for the project
         */
        fun getInstance(project: Project): PropertyEditorSettings =
            project.getService(PropertyEditorSettings::class.java)
    }
}
