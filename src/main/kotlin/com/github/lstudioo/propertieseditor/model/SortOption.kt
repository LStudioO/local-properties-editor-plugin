package com.github.lstudioo.propertieseditor.model

/**
 * Enum representing the different sorting options for properties.
 *
 * @property displayName The display name of the sorting option.
 */
enum class SortOption(private val displayName: String) {
    /**
     * Natural sorting order.
     */
    NATURAL("Natural"),
    /**
     * Sorting by property key in ascending order.
     */
    KEY_ASC("Key (A-Z)"),
    /**
     * Sorting by property key in descending order.
     */
    KEY_DESC("Key (Z-A)"),
    /**
     * Sorting by property type.
     */
    TYPE("Type");

    override fun toString(): String = displayName
}
