package com.github.lstudioo.propertieseditor.toolWindow

import com.github.lstudioo.propertieseditor.LocalizationBundle
import com.github.lstudioo.propertieseditor.model.Property
import com.github.lstudioo.propertieseditor.model.PropertyDefinition
import com.github.lstudioo.propertieseditor.model.PropertyType
import com.github.lstudioo.propertieseditor.model.PropertyValue
import com.github.lstudioo.propertieseditor.toolWindow.viewmodel.PropertyEditorViewModel
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * Main UI panel for the Property Editor.
 * Displays properties with their current values and allows editing.
 */
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

    /**
     * Populates the properties panel with file and schema-only properties
     */
    private fun populatePropertiesPanel(properties: List<Property>, schemaOnlyProperties: List<Property>) {
        propertiesPanel.removeAll()

        val gridBagConstraints = createDefaultConstraints()
        var currentRow = 0

        // Add properties from file
        currentRow = addPropertiesSection(properties, gridBagConstraints, currentRow, false)

        // Add schema-only properties section if we have any
        if (schemaOnlyProperties.isNotEmpty()) {
            currentRow = addSchemaOnlySection(schemaOnlyProperties, gridBagConstraints, currentRow)
        }

        // Add empty component at the bottom to push everything up
        addBottomSpacer(gridBagConstraints, currentRow)

        refreshPropertiesPanel()
    }

    /**
     * Creates default GridBagConstraints for property panels
     */
    private fun createDefaultConstraints(): GridBagConstraints {
        return GridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            anchor = GridBagConstraints.BASELINE_LEADING
            weighty = 0.0  // Don't allow vertical expansion
            insets = JBUI.insets(10)
        }
    }

    /**
     * Adds a section separator and schema-only properties 
     */
    private fun addSchemaOnlySection(
        schemaOnlyProperties: List<Property>,
        gridBagConstraints: GridBagConstraints,
        startRow: Int
    ): Int {
        var currentRow = startRow
        
        // Add separator
        val separator = JSeparator()
        gridBagConstraints.gridx = 0
        gridBagConstraints.gridy = currentRow++
        gridBagConstraints.gridwidth = 4 // Span across all columns
        propertiesPanel.add(separator, gridBagConstraints)

        // Add section header
        val headerLabel = JLabel(LocalizationBundle.message("ui.schema.section"))
        headerLabel.font = headerLabel.font.deriveFont(Font.BOLD)
        headerLabel.foreground = JBColor.GRAY
        gridBagConstraints.gridy = currentRow++
        propertiesPanel.add(headerLabel, gridBagConstraints)

        // Reset gridwidth for property rows
        gridBagConstraints.gridwidth = 1

        // Add schema-only properties
        return addPropertiesSection(schemaOnlyProperties, gridBagConstraints, currentRow, true)
    }

    /**
     * Adds spacer at the bottom to push content up
     */
    private fun addBottomSpacer(gridBagConstraints: GridBagConstraints, row: Int) {
        gridBagConstraints.apply {
            gridy = row
            weighty = 1.0
            fill = GridBagConstraints.BOTH
        }
        propertiesPanel.add(JPanel(), gridBagConstraints)
    }

    /**
     * Refreshes the properties panel UI
     */
    private fun refreshPropertiesPanel() {
        propertiesPanel.revalidate()
        propertiesPanel.repaint()

        SwingUtilities.invokeLater {
            scrollPane.verticalScrollBar?.value = 0
            scrollPane.horizontalScrollBar?.value = 0
        }
    }

    /**
     * Adds a section of properties to the panel
     */
    private fun addPropertiesSection(
        properties: List<Property>,
        gridBagConstraints: GridBagConstraints,
        startRow: Int,
        isSchemaOnly: Boolean
    ): Int {
        var currentRow = startRow
        properties.forEach { property ->
            gridBagConstraints.gridy = currentRow
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL

            // Property key
            addPropertyKey(property, gridBagConstraints, isSchemaOnly)

            // Property value editor
            addPropertyValueEditor(property, gridBagConstraints)

            // Property description
            addPropertyDescription(property, gridBagConstraints, isSchemaOnly)

            // Delete button - only for non-schema properties
            addPropertyActions(property, gridBagConstraints, isSchemaOnly)

            currentRow++
        }
        return currentRow
    }

    /**
     * Adds the property key label to the panel
     */
    private fun addPropertyKey(property: Property, constraints: GridBagConstraints, isSchemaOnly: Boolean) {
        constraints.gridx = 0
        constraints.weightx = 0.0
        constraints.anchor = GridBagConstraints.BASELINE_LEADING
        val keyLabel = createWrappedLabel(property.key)
        if (isSchemaOnly) {
            keyLabel.foreground = JBColor.GRAY
        }
        propertiesPanel.add(keyLabel, constraints)
    }

    /**
     * Adds the property value editor to the panel
     */
    private fun addPropertyValueEditor(property: Property, constraints: GridBagConstraints) {
        constraints.gridx = 1
        constraints.weightx = 1.0
        constraints.anchor = GridBagConstraints.NORTH
        propertiesPanel.add(createEditorComponent(property), constraints)
    }

    /**
     * Adds the property description to the panel
     */
    private fun addPropertyDescription(property: Property, constraints: GridBagConstraints, isSchemaOnly: Boolean) {
        constraints.gridx = 2
        constraints.weightx = 0.0
        val descLabel = createWrappedLabel(property.description)
        if (isSchemaOnly) {
            descLabel.foreground = JBColor.GRAY
        }
        propertiesPanel.add(descLabel, constraints)
    }

    /**
     * Adds action buttons for the property (delete button)
     */
    private fun addPropertyActions(property: Property, constraints: GridBagConstraints, isSchemaOnly: Boolean) {
        constraints.gridx = 3
        constraints.weightx = 0.0
        
        if (!isSchemaOnly) {
            constraints.anchor = GridBagConstraints.NORTH
            propertiesPanel.add(createDeleteButton(property), constraints)
        } else {
            // Add empty space for schema-only properties to maintain grid alignment
            propertiesPanel.add(JPanel(), constraints)
        }
    }

    /**
     * Creates a delete button for the property
     */
    private fun createDeleteButton(property: Property): JButton {
        return JButton("✖").apply {
            toolTipText = LocalizationBundle.message("ui.delete.tooltip")
            addActionListener {
                val result = Messages.showYesNoDialog(
                    LocalizationBundle.message("ui.delete.confirm", property.key),
                    LocalizationBundle.message("ui.delete.title"),
                    Messages.getQuestionIcon()
                )

                if (result == Messages.YES) {
                    try {
                        viewModel.deleteProperty(property)
                    } catch (ex: Exception) {
                        Messages.showErrorDialog(
                            LocalizationBundle.message("ui.error.delete", ex.message.orEmpty()),
                            LocalizationBundle.message("ui.delete.title")
                        )
                    }
                }
            }
            margin = JBUI.insets(2)
            preferredSize = Dimension(28, 24)
            isFocusable = false
            border = JBUI.Borders.empty(4)
        }
    }

    /**
     * Creates the appropriate editor component based on property type
     */
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
            is PropertyType.Enum -> createEnumEditor(property, value, definition)
            else -> createTextEditor(property, value)
        }
    }
    
    private fun createEnumEditor(property: Property, value: PropertyValue.StringValue, definition: PropertyDefinition): JComponent {
        val possibleValues = (definition.type as PropertyType.Enum).possibleValues
        return ComboBox(DefaultComboBoxModel(possibleValues.toTypedArray())).apply {
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
    
    private fun createTextEditor(property: Property, value: PropertyValue.StringValue): JComponent {
        return JTextField(value.value).apply {
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

        populatePropertiesPanel(
            properties = viewModel.getFileAndSchemaProperties(),
            schemaOnlyProperties = viewModel.getSchemaOnlyProperties(),
        )
    }

    private fun createControlPanel(): JPanel = JPanel(BorderLayout()).apply {
        border = JBUI.Borders.emptyBottom(8)
        add(createPresetPanel(), BorderLayout.NORTH)
        add(searchPanel, BorderLayout.CENTER)
    }
    
    private fun createPresetPanel(): JPanel {
        return JPanel(BorderLayout()).apply {
            add(JLabel(LocalizationBundle.message("ui.preset")).apply {
                border = JBUI.Borders.emptyRight(8)
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
    }

    private fun resetScrollPosition() {
        SwingUtilities.invokeLater {
            scrollPane.verticalScrollBar?.value = 0
            scrollPane.horizontalScrollBar?.value = 0
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
