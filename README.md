# CallGraph - IntelliJ Plugin

<p align="center">
  <img src="src/main/resources/META-INF/pluginIcon.svg" alt="CallGraph Icon" height="128">
  <br>
  <b>CallGraph</b>
  <br><br>
  <a href="https://plugins.jetbrains.com/plugin/27227-callgraph">
    <img src="get-from-marketplace.png" alt="Get from Marketplace" height="64">
  </a>
</p>

## Overview

CallGraph is an IntelliJ IDEA plugin that generates and visualizes call graphs for Java methods. It helps developers understand code flow by showing how methods call each other, making it easier to navigate and comprehend complex codebases.

![Sample Callgraph](sample.jpeg)

## Usage

1. Place your caret (text cursor) on any Java method in your code
2. Click on the CallGraph icon in the toolbar (or use View → Tool Windows → CallGraph)
3. Click on the **GENERATE** button, your graph will be generated.
4. Click on any node in the graph to navigate to that method
5. Click on any edge in the graph to navigate to that call

## Features

- **Interactive Call Graph Visualization**: Displays method calls in an interactive graph format
- **Interactive Network Graph**: Built with vis-network.js for smooth, interactive graph navigation

## Requirements

- IntelliJ IDEA (Community or Ultimate) 2022.1 or later
- Java 8 or higher
- Java projects only (requires the Java module)

## Installation

You can install CallGraph in several ways:

### From JetBrains Marketplace

<p align="center">
   <a href="https://plugins.jetbrains.com/plugin/27227-callgraph">
      <img src="get-from-marketplace.png" alt="Get from Marketplace" height="64">
   </a>
</p>

1. Click the button above

### Manual Installation

1. Download the latest release from the [Releases page](https://github.com/yunusemregul/callgraph/releases)
2. Open IntelliJ IDEA
3. Go to Settings (Preferences) → Plugins
4. Click the gear icon and select "Install Plugin from Disk..."
5. Select the downloaded ZIP file
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

## Building from Source

### Backend

1. Clone this repository
2. Open the project in IntelliJ IDEA
3. Use Gradle to build the plugin:
   ```
   ./gradlew buildPlugin
   ```
4. The plugin ZIP file will be generated in `build/distributions/`

### Frontend

The frontend uses webpack to bundle JavaScript and HTML files.

1. Navigate to the frontend directory:
   ```
   cd src/main/frontend
   ```

2. Install dependencies:
   ```
   npm install
   ```

3. Build the frontend assets:
   ```
   npx webpack
   ```

This will generate the bundled files in `src/main/resources/build/`, which are then included in the plugin.

## Development

For development, you'll need:

- JDK 17+
- Node.js and npm
- IntelliJ IDEA

The frontend uses:
- vis-network.js for graph visualization
- webpack for bundling

## Contributing

Contributions are welcome! If you'd like to contribute, please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This project is licensed under [LICENSE](LICENSE) - see the LICENSE file for details.