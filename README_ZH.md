# CallGraph - IntelliJ 插件（Fork 版本）

> ⚠️ **注意**：这是一个 **Fork 仓库**，包含自定义修改。本 Fork 版本未发布到 JetBrains 插件市场。如需使用此版本，请自行从源码构建。
>
> **原始仓库**：[yunusemregul/callgraph](https://github.com/yunusemregul/callgraph)

<p align="center">
  <img src="src/main/resources/META-INF/pluginIcon.svg" alt="CallGraph Icon" height="128">
  <br>
  <b>CallGraph</b>
</p>

## 概述

CallGraph 通过可视化方法之间的调用关系，帮助你理解 Java 代码库。只需选择任意方法，生成调用图，即可通过交互式图表探索方法间的调用链。点击图中的边（箭头），可以直接跳转到代码中该方法调用的具体位置。

![示例调用图](sample.jpeg)

## Fork 版本增强功能

本 Fork 版本相比原版新增了以下功能：

- **清空图表按钮**：新增 CLEAR 按钮，可清除当前显示的调用图
- **深度限制警告**：当图表因 maxDepth 限制被截断时，显示可视化警告
- **截断节点标记**：因深度限制未能完全展开的节点会标记 `⚠ [Limited]` 并显示虚线边框
- **右键上下文菜单**：右键点击任意节点可访问选项，不会触发代码跳转
  - **Hide Node**：隐藏选中的节点
  - **Hide Node & All Callers**：隐藏节点及其所有调用者（树形隐藏）
  - **Go to Source**：跳转到方法定义
- **动态边平滑**：改进了拖动节点时边的行为，边的连接点会动态调整

## 使用方法

1. 将光标放置在代码中的任意 Java 方法上
2. 通过以下任一方式生成调用图：
   - 右键点击，从上下文菜单中选择 "Generate Call Graph"
   - 使用快捷键 Alt+Shift+E（macOS 上为 Option+Shift+E）
   - 打开 CallGraph 工具窗口（View → Tool Windows → CallGraph），然后点击 **GENERATE** 按钮
3. 点击图中的任意节点（方法）可跳转到该方法的定义处
4. 点击图中的任意边（箭头）可跳转到该方法调用的具体代码行
5. 右键点击任意节点可打开上下文菜单，选择隐藏节点或跳转到源码

## 功能特性

- **调用图生成**：为代码库中的任意方法生成调用图
- **多种触发方式**：支持右键菜单、快捷键 Alt+Shift+E（macOS 为 Option+Shift+E）或工具窗口按钮
- **方法定义跳转**：点击图中节点可跳转到方法定义
- **调用位置跳转**：点击图中边可跳转到具体的方法调用行
- **隐藏单个节点** 或通过右键菜单 **隐藏整个调用分支**
- 使用 "SHOW ALL" 按钮恢复所有隐藏的节点
- **清空图表**：使用 CLEAR 按钮清除当前图表
- **深度限制节点可视化标记**
- **平移与缩放**：支持图表的平移和缩放操作
- **导出 HTML**：可将调用图保存为独立的 HTML 文件
- **自定义外观**：支持自定义图表背景颜色等选项

## 系统要求

- IntelliJ IDEA（Community 或 Ultimate 版本）2022.1 或更高版本
- Java 8 或更高版本
- 仅支持 Java 项目（需要 Java 模块）

## 安装方式

### 从源码构建（推荐用于本 Fork 版本）

由于这是一个包含自定义修改的 Fork 版本，你需要自行构建插件：

1. 克隆本仓库
2. 按照下方 [开发指南](#开发指南) 部分的说明构建插件
3. 安装 `build/distributions/` 目录下生成的 ZIP 文件

### 手动安装

1. 按照上述步骤构建插件，或下载预构建的 ZIP 文件（如有）
2. 打开 IntelliJ IDEA
3. 进入 Settings（Preferences）→ Plugins
4. 点击齿轮图标，选择 "Install Plugin from Disk..."
5. 选择 ZIP 文件
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

### Fork 版本

#### Fork 1.2
- 新增"隐藏节点和所有调用者"功能，支持树形隐藏
- 新增截断节点标记（⚠ [Limited]），标识因深度限制未完全展开的节点
- 改进边的平滑效果，拖动节点时连接点动态调整

#### Fork 1.1
- 新增 CLEAR 按钮，可清除当前图表
- 新增深度限制警告显示
- 新增右键上下文菜单，包含隐藏节点和跳转源码选项

### 原版版本历史

> 以下版本来自 [原始仓库](https://github.com/yunusemregul/callgraph)：

#### 1.5
- 新增节点隐藏功能 - 点击节点可看到 "Hide Node" 按钮
- 新增 "Show All" 按钮，用于恢复所有隐藏的节点

#### 1.4
- 新增 IDE 索引状态处理（修复 IndexNotReadyException 异常）

#### 1.3
- 新增选项菜单，支持自定义图表背景颜色
- 修复打开新项目时 CallGraph 窗口导致的崩溃问题
- 修复多项目支持，正确响应当前活动编辑器上下文
- 新增右键上下文菜单选项生成调用图
- 新增快捷键 Alt+Shift+E（macOS 为 Option+Shift+E）快速生成调用图

#### 1.1 & 1.2
- 优化插件描述，提升清晰度

#### 1.0
- 首次发布
- 交互式调用图可视化
- 支持 Java 项目
- Spring Controller 端点检测

## 许可证

本项目采用 GPL v3 许可证 - 详见 [LICENSE](LICENSE) 文件。