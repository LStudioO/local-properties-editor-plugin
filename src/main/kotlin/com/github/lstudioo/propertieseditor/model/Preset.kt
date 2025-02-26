package com.github.lstudioo.propertieseditor.model

/**
 * Represents a preset configuration for the property editor.
 *
 * @property name The name of the preset.
 * @property description The description of the preset.
 * @property properties The list of properties in the preset.
 */
data class Preset(
    val name: String,
    val description: String,
    val properties: List<Property>,
)
