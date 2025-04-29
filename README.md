# Callgraph - IntelliJ Plugin

## Overview

Callgraph is an IntelliJ IDEA plugin that generates and visualizes call graphs for Java methods. It helps developers understand code flow by showing how methods call each other, making it easier to navigate and comprehend complex codebases.

![Sample Callgraph](sample.jpeg)

## Features

- **Interactive Call Graph Visualization**: Displays method calls in an interactive graph format
- **Real-time Updates**: Updates the graph as you navigate through your code
- **Caller Identification**: Quickly identifies all callers of a selected method
- **Controller Endpoint Detection**: Special highlighting for Spring controller endpoints
- **Customizable Visualization**: Color-coded by class for better readability
- **Interactive Network Graph**: Built with vis-network.js for smooth, interactive graph navigation

## Requirements

- IntelliJ IDEA (Community or Ultimate) 2022.1 or later
- Java 17 or higher
- Java projects only (requires the Java module)

## Installation

### From JetBrains Marketplace

1. Open IntelliJ IDEA
2. Go to Settings (Preferences) → Plugins → Marketplace
3. Search for "Callgraph"
4. Click "Install"
5. Restart IntelliJ IDEA when prompted

### Manual Installation

1. Download the latest release from the [Releases page](https://github.com/yunusemregul/callgraph/releases)
2. Open IntelliJ IDEA
3. Go to Settings (Preferences) → Plugins
4. Click the gear icon and select "Install Plugin from Disk..."
5. Select the downloaded ZIP file
6. Restart IntelliJ IDEA when prompted

## Usage

1. Open a Java project in IntelliJ IDEA
2. Navigate to a Java method in the editor
3. The call graph tool window will appear at the bottom of the IDE
4. The graph will automatically update as you navigate between methods
5. Click on nodes in the graph to navigate to the corresponding method
6. Hover over edges to see the line number where the call occurs

### Keyboard Shortcuts

- Use standard navigation to move between methods, the graph will update accordingly

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