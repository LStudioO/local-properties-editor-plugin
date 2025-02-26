package com.github.lstudioo.propertieseditor.toolWindow

import com.github.lstudioo.propertieseditor.LocalizationBundle
import com.github.lstudioo.propertieseditor.model.SortOption
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.panels.HorizontalLayout
import com.intellij.openapi.ui.ComboBox
import javax.swing.JPanel
import javax.swing.JLabel
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.DefaultComboBoxModel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import java.awt.event.ActionListener

class PropertySearchPanel(
    private val onSearchChanged: (String) -> Unit,
    private val onSortChanged: (SortOption) -> Unit
) : JPanel(BorderLayout()) {

    private var searchListener: DocumentListener? = null
    private var sortListener: ActionListener? = null

    private val searchField = JBTextField().apply {
        preferredSize = Dimension(200, 30)
        emptyText.text = LocalizationBundle.message("ui.search.placeholder")
    }

    private val sortComboBox = ComboBox(DefaultComboBoxModel(SortOption.entries.toTypedArray()))

    init {
        val controlPanel = JPanel(HorizontalLayout(4))
        controlPanel.add(JLabel(LocalizationBundle.message("ui.search.title")))
        controlPanel.add(searchField)
        controlPanel.add(JLabel(LocalizationBundle.message("ui.sort")))
        controlPanel.add(sortComboBox)

        add(controlPanel, BorderLayout.CENTER)

        setupListeners()
    }

    private fun setupListeners() {
        searchListener = object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                update()
            }

            override fun removeUpdate(e: DocumentEvent) {
                update()
            }

            override fun changedUpdate(e: DocumentEvent) {
                update()
            }

            private fun update() {
                onSearchChanged(searchField.text)
            }
        }.also { searchField.document.addDocumentListener(it) }

        sortListener = ActionListener {
            onSortChanged(sortComboBox.selectedItem as SortOption)
        }.also { sortComboBox.addActionListener(it) }
    }
}
