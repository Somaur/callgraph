# CallGraph - IntelliJ 插件

<p align="center">
  <img src="src/main/resources/META-INF/pluginIcon.svg" alt="CallGraph Icon" height="128">
  <br>
  <b>CallGraph</b>
  <br><br>
  <a href="https://plugins.jetbrains.com/plugin/27227-callgraph">
    <img src="get-from-marketplace.png" alt="Get from Marketplace" height="64">
  </a>
</p>

## 概述

CallGraph 通过可视化方法之间的调用关系，帮助你理解 Java 代码库。只需选择任意方法，生成调用图，即可通过交互式图表探索方法间的调用链。点击图中的边（箭头），可以直接跳转到代码中该方法调用的具体位置。

![示例调用图](sample.jpeg)

## 使用方法

1. 将光标放置在代码中的任意 Java 方法上
2. 通过以下任一方式生成调用图：
   - 右键点击，从上下文菜单中选择 "Generate Call Graph"
   - 使用快捷键 Alt+Shift+E（macOS 上为 Option+Shift+E）
   - 打开 CallGraph 工具窗口（View → Tool Windows → CallGraph），然后点击 **GENERATE** 按钮
3. 点击图中的任意节点（方法）可跳转到该方法的定义处
4. 点击图中的任意边（箭头）可跳转到该方法调用的具体代码行
5. 点击节点后，可使用 "Hide Node" 按钮隐藏不需要的节点，使用 "Show All" 按钮恢复所有隐藏的节点

## 功能特性

- **调用图生成**：为代码库中的任意方法生成调用图
- **多种触发方式**：支持右键菜单、快捷键 Alt+Shift+E（macOS 为 Option+Shift+E）或工具窗口按钮
- **方法定义跳转**：点击图中节点可跳转到方法定义
- **调用位置跳转**：点击图中边可跳转到具体的方法调用行
- **节点隐藏**：支持隐藏不需要的节点，简化图表显示
- **平移与缩放**：支持图表的平移和缩放操作
- **导出 HTML**：可将调用图保存为独立的 HTML 文件
- **自定义外观**：支持自定义图表背景颜色等选项

## 系统要求

- IntelliJ IDEA（Community 或 Ultimate 版本）2021.1 或更高版本
- Java 8 或更高版本
- 仅支持 Java 项目（需要 Java 模块）

## 安装方式

### 从 JetBrains 插件市场安装

<p align="center">
   <a href="https://plugins.jetbrains.com/plugin/27227-callgraph">
      <img src="get-from-marketplace.png" alt="Get from Marketplace" height="64">
   </a>
</p>

1. 点击上方按钮，或在 IDE 中搜索 "CallGraph" 进行安装

### 手动安装

1. 从 [Releases 页面](https://github.com/yunusemregul/callgraph/releases) 下载最新版本
2. 打开 IntelliJ IDEA
3. 进入 Settings（Preferences）→ Plugins
4. 点击齿轮图标，选择 "Install Plugin from Disk..."
5. 选择下载的 ZIP 文件
6. 按提示重启 IntelliJ IDEA

## 架构设计

插件由两个主要部分组成：

### 后端（Java）

- 分析 Java 代码，提取方法调用关系
- 使用 IntelliJ Platform API 追踪编辑器活动和方法引用
- 处理调用层次数据并暴露给前端

### 前端（JavaScript）

- 使用 vis-network.js 构建交互式图表可视化
- 提供响应式 UI 用于图表导航和探索
- 通过 JavaScript Bridge 与 Java 后端通信

## 开发指南

开发环境需要：

- JDK 17+
- Node.js v18.10.0（或更高版本）和 npm
- IntelliJ IDEA

**快速开始**：最简单的方式是在 IntelliJ IDEA 中打开项目，运行 "Run Plugin" 配置。

### 手动构建步骤

> **重要**：必须先构建前端，再构建后端插件，否则插件运行时会因缺少 HTML 资源而报错。

#### 1. 构建前端

前端使用 webpack 打包 JavaScript 和 HTML 文件。

```bash
# 进入前端目录
cd src/main/frontend

# 安装依赖
npm install

# 构建前端资源
npm run build:prod
```

构建产物将生成在 `src/main/resources/build/` 目录下（包含 `callgraph.html`、`saveas.html` 等文件）。

#### 2. 构建后端插件

```bash
# 返回项目根目录
cd ../../..

# 使用 Gradle 构建插件
./gradlew buildPlugin
```

插件 ZIP 文件将生成在 `build/distributions/` 目录下。

#### 一键构建脚本

你也可以使用以下命令一次性完成完整构建：

```bash
cd src/main/frontend && npm install && npm run build:prod && cd ../../.. && ./gradlew clean buildPlugin
```

## 参与贡献

欢迎贡献代码！如果你想参与贡献，请：

1. Fork 此仓库
2. 创建功能分支
3. 提交你的更改
4. 发起 Pull Request

## 版本历史

### 1.5
- 新增节点隐藏功能 - 点击节点可看到 "Hide Node" 按钮
- 新增 "Show All" 按钮，用于恢复所有隐藏的节点

### 1.4
- 新增 IDE 索引状态处理（修复 IndexNotReadyException 异常）

### 1.3
- 新增选项菜单，支持自定义图表背景颜色
- 修复打开新项目时 CallGraph 窗口导致的崩溃问题
- 修复多项目支持，正确响应当前活动编辑器上下文
- 新增右键上下文菜单选项生成调用图
- 新增快捷键 Alt+Shift+E（macOS 为 Option+Shift+E）快速生成调用图

### 1.1 & 1.2
- 优化插件描述，提升清晰度

### 1.0
- 首次发布
- 交互式调用图可视化
- 支持 Java 项目
- Spring Controller 端点检测

## 许可证

本项目采用 GPL v3 许可证 - 详见 [LICENSE](LICENSE) 文件。