# Elite Game Assistant (开源版)

## 📖 项目简介

**Elite Game Assistant** 是一个的 Android 游戏助手应用。本项目展示了如何使用现代 Android 技术栈构建一个功能完善的应用，其中包含了原生库调用、Root 权限管理和模块化架构等关键实践。


## ✨ 主要功能

- **现代化 UI**: 完全使用 Jetpack Compose 构建，提供流畅、美观的用户体验。
- **游戏列表**: 动态加载和展示支持的游戏。
- **游戏增强**: 提供启动游戏辅助功能的核心逻辑（具体功能需自行实现）。
- **卡密管理**: 保留了将卡密写入文件的基础功能，方便二次开发。
- **Root 权限操作**: 集成了 `libsu` 库，演示了如何在拥有 Root 权限的设备上执行高级文件操作。

## 🛠️ 技术栈

- **开发语言**: [Kotlin](https://kotlinlang.org/)
- **UI 框架**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **架构模式**: MVVM (Model-View-ViewModel)
- **依赖注入**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **异步处理**: [Kotlin Coroutines & Flow](https://kotlinlang.org/docs/coroutines-guide.html)
- **数据库**: [Room](https://developer.android.com/training/data-storage/room)
- **网络请求**: [Retrofit & OkHttp](https://square.github.io/retrofit/)
- **Root 权限管理**: [libsu](https://github.com/topjohnwu/libsu)

## 🚀 如何开始

### 环境要求

- Android Studio (建议使用最新稳定版，如 Hedgehog 或更高版本)
- JDK 17 或更高版本

### 安装与构建

1.  **克隆项目**
    ```bash
    git clone [您的项目 Git 仓库地址]
    ```

2.  **在 Android Studio 中打开**
    - 打开 Android Studio，选择 `Open`，然后找到您刚刚克隆的项目目录。

3.  **配置 `JAVA_HOME` (重要)**
    - 本项目构建强依赖正确的 `JAVA_HOME` 环境变量。请确保您的系统环境变量 `JAVA_HOME` 指向了您安装的 JDK 17 (或更高版本) 的根目录。如果配置错误，Gradle 构建将会失败。

4.  **同步与构建**
    - 等待 Android Studio 完成 Gradle 同步并下载所有依赖。
    - 点击 `Build` -> `Make Project` 或直接运行应用到您的模拟器或真实设备上。

## 📂 项目结构与二次开发指南

为了帮助您快速上手二次开发，这里对项目结构和核心流程进行更详细的说明。

### 详细目录结构

```
.
├── app
│   ├── libs                # 存放 .so 原生库文件
│   ├── src/main
│   │   ├── java
│   │   │   └── com/gameassistant/elite
│   │   │       ├── data
│   │   │       │   ├── datasource  # 定义数据源接口 (如系统信息)
│   │   │       │   ├── model       # 数据模型 (如 GameType)
│   │   │       │   ├── nativelib   # 与原生 .so 库交互的管理类
│   │   │       │   ├── repository  # Repository 实现，连接数据源和领域层
│   │   │       │   └── utils       # 工具类 (如卡密处理)
│   │   │       ├── di              # Hilt 依赖注入模块
│   │   │       ├── domain
│   │   │       │   ├── model       # 领域模型 (如 GameInfo)
│   │   │       │   └── repository  # Repository 接口
│   │   │       └── presentation
│   │   │           ├── screens     # 各个界面的 Composable 函数和 ViewModel
│   │   │           └── ui          # 通用 UI 组件和主题
│   │   └── res                 # 资源文件
│   └── build.gradle.kts      # App 模块的 Gradle 配置
└── build.gradle.kts          # 项目根目录的 Gradle 配置
```

### 游戏辅助功能开发流程

核心的“启动游戏辅助”流程涉及表现层、数据层和原生层。以下是二次开发时您需要关注的关键文件和步骤：

1.  **UI 交互入口 (`presentation/screens/gameenhance/GameEnhanceScreen.kt`)**
    - 用户在界面上点击“启动”按钮。
    - 该按钮的 `onClick` 事件会调用 `GameEnhanceViewModel` 中的 `launchGameAssist(gameId)` 方法。

2.  **业务逻辑调度 (`presentation/screens/gameenhance/GameEnhanceViewModel.kt`)**
    - `launchGameAssist(gameId)` 方法是业务逻辑的起点。
    - 它会根据 `gameId` 判断要启动的游戏类型 (`GameType`)。
    - **关键点**: 它会调用 `nativeManager.startGameEnhancement()`，将控制权交给原生层。

3.  **原生库桥梁 (`data/nativelib/NativeLibraryManager.kt`)**
    - 这是连接 Kotlin/Java 世界和 C/C++ 原生库的桥梁。
    - `startGameEnhancement(gameType, ...)` 方法通过 JNI (Java Native Interface) 调用原生函数。
    - **二次开发核心**: 您需要在这里定义新的 `external fun` 来暴露您在 .so 库中编写的新功能。同时，您需要修改 `startGameEnhancement` 的内部逻辑，以调用您自己的原生函数。

4.  **原生功能实现 (C/C++ & .so 文件)**
    - 真正的游戏辅助功能（如内存读写、自瞄、绘制等）是在 C/C++ 中实现的，并被编译成 `.so` 动态链接库文件。
    - 您需要将编译好的 `.so` 文件放置在 `app/libs` 目录下对应的 ABI 文件夹中（如 `arm64-v8a`）。
    - **二次开发核心**: 您需要具备 Android NDK 开发知识，在 C/C++层面实现您想要的功能，并通过 JNI 将其暴露给 `NativeLibraryManager`。

### 卡密功能开发

本项目保留了将卡密写入文件的功能，但移除了验证逻辑，方便您自定义。

- **写入流程**: `GameEnhanceViewModel` 中的 `activateCardKey()` 方法会调用 `gameRepository.saveCardKey(cardKey)`。
- **文件位置**: `GameRepositoryImpl` 的 `saveCardKey` 方法通过 `libsu` 库，以 Root 权限将卡密写入到 `/data/system/uCard.txt` 文件中。
- **二次开发**: 您可以修改 `saveCardKey` 的实现来改变文件路径或加密方式。您也可以在 `NativeLibraryManager` 中添加读取该文件的原生逻辑，用于您自己的验证。

## 🤝 贡献指南

欢迎任何形式的贡献！如果您有任何想法、建议或发现 Bug，请随时提交 Issue 或 Pull Request。

1.  Fork 本项目。
2.  创建您的功能分支 (`git checkout -b feature/YourFeature`)。
3.  提交您的更改 (`git commit -m 'Add some feature'`)。
4.  将您的分支推送到远程仓库 (`git push origin feature/YourFeature`)。
5.  创建一个新的 Pull Request。

## 📄 开源许可

本项目采用 [MIT License](LICENSE) 开源。