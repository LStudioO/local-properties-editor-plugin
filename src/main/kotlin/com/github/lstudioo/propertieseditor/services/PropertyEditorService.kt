package com.github.lstudioo.propertieseditor.services

import com.github.lstudioo.propertieseditor.model.FileSettings
import com.github.lstudioo.propertieseditor.settings.PropertyEditorSettings
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.io.File

@Service(Service.Level.PROJECT)
class PropertyEditorService(private val project: Project) {
    fun getFileSettings(): FileSettings {
        val settings = PropertyEditorSettings.getInstance(project)
        val defaultPath = "${project.basePath}"

        return FileSettings(
            propertiesFile = File(settings.propertiesFilePath.ifEmpty { "$defaultPath/local.properties" }),
            configFile = File(settings.configFilePath.ifEmpty { "$defaultPath/property_schema.json" })
        )
    }
}
