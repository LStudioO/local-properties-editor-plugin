package com.github.lstudioo.propertieseditor.toolWindow

import com.github.lstudioo.propertieseditor.LocalizationBundle
import com.github.lstudioo.propertieseditor.model.Property
import com.github.lstudioo.propertieseditor.model.PropertyType
import com.github.lstudioo.propertieseditor.model.PropertyValue
import com.github.lstudioo.propertieseditor.toolWindow.viewmodel.PropertyEditorViewModel
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import java.awt.*

class PropertyEditorPanel(
    private val viewModel: PropertyEditorViewModel,
) : JPanel() {
    private val propertiesPanel: JPanel = JPanel(GridBagLayout())
    private val scrollPane: JBScrollPane = JBScrollPane(propertiesPanel)
    private val presetsComboBox: ComboBox<String> = ComboBox()
    private val searchPanel = PropertySearchPanel(
        onSearchChanged = { searchText -> viewModel.filterProperties(searchText) },
        onSortChanged = { sortOption -> viewModel.sortProperties(sortOption) }
    )
    private val errorLabel: JLabel = JLabel().apply {
        border = JBUI.Borders.empty(4)
        foreground = JBColor.RED
        isVisible = false
    }

    init {
        setupUi()
        listenForDataChanges()
        resetScrollPosition()
        listenForErrors()
        loadData()
        renderUi()
    }

    private fun listenForDataChanges() {
        viewModel.addPropertyListener {
            renderUi()
        }
    }

    private fun loadData() {
        viewModel.loadProperties()
    }

    private fun setupUi() {
        layout = BorderLayout()
        border = JBUI.Borders.empty(8)
        preferredSize = Dimension(600, 800)

        add(createControlPanel(), BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)
        add(errorLabel, BorderLayout.SOUTH)
    }

    private fun listenForErrors() {
        viewModel.addErrorListener { error ->
            errorLabel.text = error
            errorLabel.isVisible = error != null
        }
    }

    private fun createWrappedLabel(text: String): JComponent {
        return JTextArea(text).apply {
            lineWrap = true
            wrapStyleWord = true
            isOpaque = false
            isEditable = false
            font = UIManager.getFont("Label.font")
            border = null
            background = null

            preferredSize = Dimension(
                PREFERRED_WRAP_WIDTH_PX,
                getFontMetrics(font).height *
                        (1 + text.length / PROPERTY_NAME_MAX_LENGTH_IN_LINE)
            )
        }
    }

    private fun populatePropertiesPanel(properties: List<Property>) {
        propertiesPanel.removeAll()

        val gridBagConstraints = GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.BASELINE_LEADING
            weighty = 0.0  // Don't allow vertical expansion
            insets = JBUI.insets(10)
        }

        properties.forEachIndexed { index, property ->
            gridBagConstraints.gridy = index

            gridBagConstraints.gridx = 0
            gridBagConstraints.weightx = 0.0
            gridBagConstraints.anchor = GridBagConstraints.BASELINE_LEADING
            propertiesPanel.add(createWrappedLabel(property.key), gridBagConstraints)

            gridBagConstraints.gridx = 1
            gridBagConstraints.weightx = 1.0
            gridBagConstraints.anchor = GridBagConstraints.NORTH
            propertiesPanel.add(createEditorComponent(property), gridBagConstraints)

            gridBagConstraints.gridx = 2
            gridBagConstraints.weightx = 0.0
            propertiesPanel.add(createWrappedLabel(property.description), gridBagConstraints)
        }

        // Add empty component at the bottom to push everything up
        gridBagConstraints.apply {
            gridy = properties.size
            weighty = 1.0
            fill = GridBagConstraints.BOTH
        }
        propertiesPanel.add(JPanel(), gridBagConstraints)

        propertiesPanel.revalidate()
        propertiesPanel.repaint()

        SwingUtilities.invokeLater {
            scrollPane.verticalScrollBar?.value = 0
            scrollPane.horizontalScrollBar?.value = 0
        }
    }

    private fun createEditorComponent(property: Property): JComponent =
        when (property.value) {
            is PropertyValue.BooleanValue -> createBooleanEditor(property)
            is PropertyValue.ArrayValue -> createArrayEditor(property)
            is PropertyValue.StringValue -> createStringEditor(property)
        }

    private fun createBooleanEditor(property: Property): JComponent {
        val value = property.value as PropertyValue.BooleanValue

        return JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
            val group = ButtonGroup()

            val trueButton = JRadioButton(TRUE, value.value).apply {
                addActionListener {
                    if (isSelected) {
                        viewModel.updateProperty(property.copy(value = PropertyValue.BooleanValue(true)))
                    }
                }
            }

            val falseButton = JRadioButton(FALSE, !value.value).apply {
                addActionListener {
                    if (isSelected) {
                        viewModel.updateProperty(property.copy(value = PropertyValue.BooleanValue(false)))
                    }
                }
            }

            group.apply {
                add(trueButton)
                add(falseButton)
            }

            add(trueButton)
            add(Box.createRigidArea(Dimension(MARGIN_BETWEEN_RADIO_BUTTONS_PX, 0)))
            add(falseButton)
        }
    }

    private fun createArrayEditor(property: Property): JComponent {
        var value = property.value as PropertyValue.ArrayValue
        val definition = viewModel.getPropertyDefinition(property.key)
        val possibleValues = (definition?.type as? PropertyType.Array)?.possibleValues ?: emptyList()

        return JPanel(GridLayout(0, 2, 10, 5)).apply {
            possibleValues.forEach { possibleValue ->
                add(JCheckBox(possibleValue).apply {
                    isSelected = value.values.contains(possibleValue)
                    addActionListener {
                        value = if (isSelected) {
                            PropertyValue.ArrayValue(value.values + possibleValue)
                        } else {
                            PropertyValue.ArrayValue(value.values - possibleValue)
                        }
                        viewModel.updateProperty(
                            property.copy(value = value)
                        )
                    }
                })
            }
        }
    }

    private fun createStringEditor(property: Property): JComponent {
        val value = property.value as PropertyValue.StringValue
        val definition = viewModel.getPropertyDefinition(property.key)

        return when (definition?.type) {
            is PropertyType.Enum -> {
                val possibleValues = (definition.type as PropertyType.Enum).possibleValues
                ComboBox(DefaultComboBoxModel(possibleValues.toTypedArray())).apply {
                    selectedItem = value.value
                    addActionListener {
                        selectedItem?.toString()?.let { selected ->
                            viewModel.updateProperty(
                                property.copy(value = PropertyValue.StringValue(selected))
                            )
                        }
                    }
                }
            }

            else -> {
                JTextField(value.value).apply {
                    preferredSize = Dimension(200, 30)
                    document.addDocumentListener(object : SimpleDocumentListener {
                        override fun update() {
                            viewModel.updateProperty(
                                property.copy(value = PropertyValue.StringValue(text))
                            )
                        }
                    })
                }
            }
        }
    }

    fun interface SimpleDocumentListener : DocumentListener {
        fun update()
        override fun insertUpdate(e: DocumentEvent) = update()
        override fun removeUpdate(e: DocumentEvent) = update()
        override fun changedUpdate(e: DocumentEvent) = update()
    }

    private fun renderUi() {
        presetsComboBox.apply {
            removeAllItems()
            addItem(LocalizationBundle.message("ui.no_preset"))
            viewModel.getAvailablePresets().forEach { addItem(it) }
            viewModel.getSelectedPresetIndex()?.let { selectedIndex = it + 1 }
        }

        populatePropertiesPanel(viewModel.getProperties())
    }

    private fun createControlPanel(): JPanel = JPanel(BorderLayout()).apply {
        border = JBUI.Borders.emptyBottom(8)
        val preset = JPanel(BorderLayout()).apply {
            add(JLabel(LocalizationBundle.message("ui.preset")).apply {
                setBorder(JBUI.Borders.emptyRight(8))
            }, BorderLayout.WEST)

            presetsComboBox.apply {
                addActionListener {
                    selectedItem?.toString()?.let { selected ->
                        if (selected == LocalizationBundle.message("ui.no_preset")) return@let
                        viewModel.loadPreset(selected)
                    }
                }
            }
            add(presetsComboBox, BorderLayout.CENTER)
        }
        add(preset, BorderLayout.NORTH)
        add(searchPanel, BorderLayout.SOUTH)
    }

    private fun resetScrollPosition() {
        SwingUtilities.invokeLater {
            val scrollPane = parent as? JBScrollPane
            scrollPane?.verticalScrollBar?.value = 0
        }
    }

    companion object {
        private const val PROPERTY_NAME_MAX_LENGTH_IN_LINE = 15
        private const val PREFERRED_WRAP_WIDTH_PX = 200
        private const val MARGIN_BETWEEN_RADIO_BUTTONS_PX = 50
        private const val TRUE = "True"
        private const val FALSE = "False"
    }
}
