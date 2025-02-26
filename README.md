# local-properties-editor-plugin

![Build](https://github.com/LStudioO/local-properties-editor-plugin/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

<!-- Plugin description -->
An IntelliJ IDEA plugin that provides a user-friendly graphical interface for editing property files (`local.properties`, `gradle.properties`, etc.) in your project. This plugin transforms the tedious task of manually editing property files into a streamlined, type-safe experience with validation and preset support.
<!-- Plugin description end -->

## Key Features

- **Visual Property Editor**: Edit properties through an intuitive GUI instead of directly editing text files
- **Type Safety**: Edit properties with appropriate controls based on their data type (checkboxes for booleans, dropdowns for enums, etc.)
- **Schema Support**: Define property structures with JSON schema files specifying types, descriptions, and valid values
- **Presets**: Create and apply property presets to quickly switch between configurations
- **Type Validation**: Automatic validation ensures property values conform to their defined types

## Installation

- Using the IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "local-properties-editor-plugin"</kbd> >
  <kbd>Install</kbd>
  
- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/LStudioO/local-properties-editor-plugin/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


## Schema File Creation

The schema file defines the structure, types, and possible values for your properties. By default, the plugin looks for a file named `property_schema.json` in your project root.

### Schema Structure

```json
{
  "properties": [
    {
      "key": "debug.enabled",
      "type": "boolean",
      "defaultValue": false,
      "description": "Enable debug mode"
    },
    {
      "key": "api.environments",
      "type": "array",
      "values": ["dev", "staging", "prod"],
      "description": "Active API environments"
    },
    {
      "key": "build.type",
      "type": "enum",
      "values": ["debug", "release", "profile"],
      "defaultValue": "debug",
      "description": "Build configuration type"
    },
    {
      "key": "api.url",
      "type": "string",
      "description": "API base URL"
    }
  ],
  "presets": [
    {
      "name": "Development",
      "description": "Settings for development environment",
      "properties": {
        "debug.enabled": { "value": true },
        "api.environments": { "value": ["dev", "staging"] },
        "build.type": { "value": "debug" },
        "api.url": { "value": "http://localhost:8080" }
      }
    },
    {
      "name": "Production",
      "description": "Settings for production environment",
      "properties": {
        "debug.enabled": { "value": false },
        "api.environments": { "value": ["prod"] },
        "build.type": { "value": "release" },
        "api.url": { "value": "https://api.production.com" }
      }
    }
  ]
}
```

### Property Types

The plugin supports four property types:

1. **boolean**: Renders as true/false radio buttons
2. **string**: Renders as a text field
3. **enum**: Renders as a dropdown with predefined options
4. **array**: Renders as a list of checkboxes for multiple selections

### Creating a Schema File

1. Create a new file named `property_schema.json` in your project root.
2. Define your properties in the `properties` array, specifying at minimum:
   - `key`: The property identifier
   - `type`: One of "boolean", "string", "enum", or "array"
   - `description`: A human-readable description of the property
3. For enum and array types, include a `values` array with all possible options.
4. Optionally include a `defaultValue` that matches the property type.
5. Optionally add presets in the `presets` array to define groups of property values.

## Usage Guide

### Basic Usage

1. Open your project in IntelliJ IDEA.
2. Look for the "Local Property Editor" tool window (usually on the right side).
3. If you've configured your schema file correctly, you'll see your properties listed with appropriate editors.
4. Make changes using the UI controls.
5. Changes are automatically saved to your properties file.

### Using Presets

1. Select a preset from the dropdown at the top of the property editor panel.
2. The preset values will be applied to your properties file.
3. You can modify individual properties after applying a preset.

### Customizing File Paths

1. Click the settings icon (⚙️) in the property editor toolbar.
2. Set custom paths for your properties file and schema file.
3. Click "Apply" to save your settings.

### Toolbar Actions

- **Refresh**: Reload property values from disk
- **Reset to Default**: Restore properties to their default values (as defined in the schema)
- **Copy to Clipboard**: Copy all properties in key=value format to the clipboard
- **Settings**: Configure plugin settings

## Requirements

- IntelliJ IDEA 2023.3 or newer
- Project with property files to edit

## Plans for Future Features

1. **Multi-file Mode**: Support editing multiple property files simultaneously with tab-based navigation.
2. **Import/Export Functionality**: Allow importing and exporting property configurations to share between projects.
3. **Diff View**: Provide a visual difference view between current properties and presets.
4. **Version History**: Track changes to property values over time.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
