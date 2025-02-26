package com.github.lstudioo.propertieseditor.model

import java.io.File

/**
 * Represents the file settings for the properties editor.
 *
 * @property propertiesFile The file containing the properties.
 * @property configFile The file containing the property schema and presets.
 */
data class FileSettings(
    val propertiesFile: File,
    val configFile: File,
)
