# CallGraph - IntelliJ Plugin (Fork)

> ⚠️ **Note**: This is a **forked repository** with custom modifications. This fork is not published to JetBrains Marketplace. If you want to use this version, you need to build it from source.
>
> **Original Repository**: [yunusemregul/callgraph](https://github.com/yunusemregul/callgraph)

<p align="center">
  <img src="src/main/resources/META-INF/pluginIcon.svg" alt="CallGraph Icon" height="128">
  <br>
  <b>CallGraph</b>
</p>

## Overview

CallGraph helps you understand your Java codebase by visualizing how methods call each other. Simply select any method, generate a graph, and explore the connections through an interactive diagram. When you click on edges in the graph, you'll be taken directly to where that method call appears in your code editor.

![Sample Callgraph](sample.jpeg)

## Fork Enhancements

This fork includes the following enhancements over the original:

- **Clear Graph Button**: Added a CLEAR button to remove the current call graph display
- **Depth Limit Warning**: Shows a visual warning when the graph is truncated due to maxDepth limit
- **Truncated Node Markers**: Nodes that couldn't be fully explored due to depth limits are marked with `⚠ [Limited]` and dashed borders
- **Right-click Context Menu**: Right-click on any node to access options without triggering code navigation
  - **Hide Node**: Hide the selected node
  - **Hide Node & All Callers**: Hide the node and all its callers (tree-style hiding)
  - **Go to Source**: Navigate to the method definition
- **Dynamic Edge Smoothing**: Improved edge behavior when dragging nodes - edges now dynamically adjust their connection points

## Usage

1. Place your caret (text cursor) on any Java method in your code
2. Either:
   - Right-click and select "Generate Call Graph" from the context menu
   - Use keyboard shortcut Alt+Shift+E (Option+Shift+E on macOS)
   - Click on the CallGraph icon in the toolbar (or use View → Tool Windows → CallGraph) and click the **GENERATE** button
3. Click on any node (method) in the graph to jump to its definition
4. Click on any edge (arrow) in the graph to jump to the exact line where that method is being called
5. Right-click on any node to access the context menu for hiding nodes or navigating to source

## Features

- Generate call graphs for any method in your codebase
- Generate graphs via right-click context menu or Alt+Shift+E (Option+Shift+E on macOS) shortcut
- Navigate to method definitions by clicking nodes
- Navigate to exact method call locations by clicking edges
- **Hide individual nodes** or **hide entire call branches** via right-click menu
- Show all hidden nodes with the "SHOW ALL" button
- **Clear the graph** with the CLEAR button
- **Visual indicators** for depth-limited nodes
- Pan & zoom the graph
- Save the graph as an HTML file
- Customize graph appearance with options like background color

## Requirements

- IntelliJ IDEA (Community or Ultimate) 2022.1 or later
- Java 8 or higher
- Java projects only (requires the Java module)

## Installation

### Build from Source (Recommended for this Fork)

Since this is a fork with custom modifications, you need to build the plugin yourself:

1. Clone this repository
2. Follow the [Development](#development) section below to build the plugin
3. Install the generated ZIP file from `build/distributions/`

### Manual Installation

1. Build the plugin following the steps above, or download a pre-built ZIP if available
2. Open IntelliJ IDEA
3. Go to Settings (Preferences) → Plugins
4. Click the gear icon and select "Install Plugin from Disk..."
5. Select the ZIP file
6. Restart IntelliJ IDEA when prompted

## Architecture

The plugin consists of two main components:

### Backend (Java)

- Analyzes Java code to extract method call relationships
- Uses IntelliJ Platform APIs to track editor activity and method references
- Processes the call hierarchy data and exposes it to the frontend

### Frontend (JavaScript)

- Built with vis-network.js for interactive graph visualizations
- Provides a responsive UI for graph navigation and exploration
- Communicates with the Java backend through a JavaScript bridge

# Development

For development, you'll need:

- JDK 17+
- Node.js v18.10.0 (or later) and npm
- IntelliJ IDEA

**Quick Start:** The easiest way to run the project is to open it in IntelliJ IDEA and run the "Run Plugin" configuration.

## Manual Build Steps

> **Important**: You must build the frontend first, then build the backend plugin. Otherwise, the plugin will fail at runtime due to missing HTML resources.

### 1. Build Frontend

The frontend uses webpack to bundle JavaScript and HTML files.

```bash
# Navigate to the frontend directory
cd src/main/frontend

# Install dependencies
npm install

# Build the frontend assets
npm run build:prod
```

This will generate the bundled files in `src/main/resources/build/` (including `callgraph.html`, `saveas.html`, etc.).

### 2. Build Backend Plugin

```bash
# Return to project root
cd ../../..

# Use Gradle to build the plugin
./gradlew buildPlugin
```

The plugin ZIP file will be generated in `build/distributions/`.

### One-liner Build Script

You can also use the following command to complete the full build in one step:

```bash
cd src/main/frontend && npm install && npm run build:prod && cd ../../.. && ./gradlew clean buildPlugin
```

## Contributing

Contributions are welcome! If you'd like to contribute, please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## Version History

### Fork Versions

#### Fork 1.2
- Added "Hide Node & All Callers" feature for tree-style hiding
- Added truncated node markers (⚠ [Limited]) for depth-limited nodes
- Improved edge smoothing with dynamic connection points

#### Fork 1.1
- Added CLEAR button to remove current graph
- Added depth limit warning display
- Added right-click context menu with Hide Node and Go to Source options

### Original Versions

> The following versions are from the [original repository](https://github.com/yunusemregul/callgraph):

#### 1.3
- Added options menu for customizing graph background color
- Fixed crash when opening new projects with CallGraph window open
- Fixed multi-project support to respect active editor context
- Added right-click context menu option to generate call graphs
- Added keyboard shortcut (Alt+Shift+E) for quick graph generation

#### 1.1 & 1.2
- Improved plugin description for better clarity

#### 1.0
- Initial release
- Interactive call graph visualization
- Support for Java projects
- Spring controller endpoint detection

## License

This project is licensed under GPL v3 - see the [LICENSE](LICENSE) file for details.