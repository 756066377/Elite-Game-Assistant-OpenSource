# Elite Game Assistant（开源版）

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📖 项目简介
Elite Game Assistant 是一个基于现代 Android 技术栈的游戏助手应用示例，涵盖 Jetpack Compose 界面、Root 权限（libsu）、模块化架构、滚动性能与高刷优化等实践。适合学习、二次开发或作为业务项目脚手架。

## ✨ 主要功能
- 现代化 UI：基于 Jetpack Compose（Material3），视觉清晰、交互流畅
- 系统监控仪表盘：展示内核版本、构建指纹、SELinux 状态等
- 游戏列表与增强入口：提供游戏增强的调用流程（示例链路已打通）
- 卡密写入示例：以 Root 权限写入系统路径，便于自定义鉴权
- Root 操作：集成 libsu，可执行 su 命令（需设备具备 Root 环境）
- 高刷滚动优化：针对 90/120Hz 等高刷新率设备优化滚动体验

## 🛠 技术栈
- 语言：Kotlin
- UI：Jetpack Compose（Material3）
- 架构：MVVM + Hilt 依赖注入
- 并发：Kotlin Coroutines & Flow
- Root：libsu（topjohnwu）
- 构建：Gradle Kotlin DSL

---

## 📂 目录结构（二次开发重点）
下面仅列出对二次开发最关键的结构与示例文件，实际以仓库为准：

```
Elite-Game-Assistant-OpenSource/
├── app/
│   ├── src/main/
│   │   ├── java/com/gameassistant/elite/
│   │   │   ├── presentation/               # 表现层（Compose UI）
│   │   │   │   ├── screens/
│   │   │   │   │   ├── dashboard/
│   │   │   │   │   │   └── DashboardScreen.kt            # 仪表盘 UI
│   │   │   │   │   └── gameenhance/                      # 游戏增强入口 UI（示例）
│   │   │   │   ├── components/common/
│   │   │   │   │   ├── AnimatedFlowBackground.kt
│   │   │   │   │   └── GlassMorphismCard.kt
│   │   │   │   └── utils/
│   │   │   │       ├── HighRefreshRateManager.kt         # 高刷支持
│   │   │   │       ├── ScrollPerformanceOptimizer.kt     # 滚动性能优化
│   │   │   │       └── TouchEventOptimizer.kt
│   │   │   ├── data/                     # 数据层（含 libsu、原生桥接等）
│   │   │   │   ├── datasource/
│   │   │   │   │   ├── LibsuSystemInfoDataSource.kt      # 通过 libsu 获取系统信息
│   │   │   │   │   └── SystemInfoDataSource.kt           # 非 root 获取系统信息
│   │   │   │   ├── repository/
│   │   │   │   │   └── SystemMonitorRepositoryImpl.kt    # 仓储实现（示例）
│   │   │   │   ├── nativelib/
│   │   │   │   │   └── NativeLibraryManager.kt (建议位置) # JNI 桥接（示例入口，若无可自行创建）
│   │   │   │   └── utils/
│   │   │   ├── domain/                  # 领域层
│   │   │   │   ├── model/               # 领域模型（如 SystemInfo、SELinuxStatus）
│   │   │   │   └── repository/          # 仓储接口
│   │   │   └── di/                      # Hilt 模块（依赖注入配置）
│   │   ├── res/                         # 资源文件
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts                 # App 模块 Gradle 配置
│   └── release/                         # 构建产物（已在 .gitignore 中忽略大部分不必要内容）
├── build.gradle.kts                     # 根构建脚本
├── settings.gradle.kts
├── README.md
└── LICENSE
```

> 提示：
> - 如果您在 `data/nativelib/` 下未找到 `NativeLibraryManager.kt`，可按下文“SO 接入指南”新建同名文件作为 JNI 桥接入口。
> - 本项目默认未强制要求 CMake，支持直接放置预编译 .so 文件完成集成。

---

## 🔗 SO 接入指南（重点）
根据您的场景选择“预编译 .so 直接集成”或“使用 CMake 构建源码”。

### 方案一：预编译 .so 直接集成（推荐）
1. 目录放置（标准做法）
   - 在模块目录创建：`app/src/main/jniLibs/`
   - 按 ABI 放置对应 .so：
     ```
     app/src/main/jniLibs/
     ├── arm64-v8a/
     │   └── libyourlib.so
     └── armeabi-v7a/
         └── libyourlib.so
     ```
   - 也可沿用 `app/libs/<abi>/libxxx.so` 的结构，但 Android 官方推荐使用 `jniLibs`。

2. 加载与命名规范
   - `System.loadLibrary("yourlib")` 中填写去掉前缀 `lib` 与后缀 `.so` 的部分。
   - 建议在“原生桥接类”中，静态块或初始化时加载：
     ```kotlin
     package com.gameassistant.elite.data.nativelib

     object NativeLibraryManager {
         init {
             try {
                 System.loadLibrary("yourlib")
             } catch (e: UnsatisfiedLinkError) {
                 // TODO: 上报或降级处理
             }
         }

         external fun nativeInit(gameType: Int): Int
         external fun startGameEnhancement(gameType: Int, flags: Int = 0): Int
         external fun stopGameEnhancement(): Int
     }
     ```

3. Kotlin ↔ JNI 方法签名
   - Kotlin 声明 `external fun` 后，在 C/C++ 中实现对应 JNI 方法：
     ```cpp
     // 对应包名/类名/方法名：
     // Java_com_gameassistant_elite_data_nativelib_NativeLibraryManager_nativeInit
     extern "C"
     JNIEXPORT jint JNICALL
     Java_com_gameassistant_elite_data_1nativelib_NativeLibraryManager_1nativeInit(
         JNIEnv* env, jclass clazz, jint gameType
     ) {
         // TODO: 初始化逻辑
         return 0; // 0 表示成功，按需自定义
     }
     ```
   - 注意：包名中的下划线与路径需用 JNI 规则转义（如上 `_1`），也可通过 `RegisterNatives` 动态注册规避命名复杂度。

4. R8/Proguard 混淆配置（如开启混淆）
   - 确保 JNI 符号不被移除或重命名：
     ```
     -keepclasseswithmembers class * {
         native <methods>;
     }
     -keep class com.gameassistant.elite.data.nativelib.** { *; }
     ```

5. ABI 过滤（可选）
   - 仅打包目标 ABI，减小体积：
     ```kotlin
     // app/build.gradle.kts
     android {
         defaultConfig {
             ndk {
                 abiFilters += listOf("arm64-v8a", "armeabi-v7a")
             }
         }
     }
     ```

6. 常见问题排查
   - `UnsatisfiedLinkError`：名称不匹配（`lib` 前缀/后缀、包名、方法签名）；ABI 不匹配；未放入 `jniLibs` 正确目录；未调用 `loadLibrary`
   - 64/32 位不匹配：仅放了 arm64-v8a，但设备/模拟器只支持 32 位（或相反）
   - Proguard 移除了 native 方法：添加 keep 规则
   - 对接 lib 名称变更：确保 `System.loadLibrary("实际名")` 与文件一致

### 方案二：使用 CMake 构建源码（可选）

## 🔧 SO 接入强约束与自动打包
- 目录组织（管理用，子目录默认不参与打包）：
  ```
  app/src/main/jniLibs/arm64-v8a/
  ├── delta/     # 放置 Delta 的一个 .so（任意文件名）
  ├── pubg/      # 放置 PUBG 的一个 .so（任意文件名）
  └── valorant/  # 放置 Valorant 的一个 .so（任意文件名）
  ```
- 自动复制与重命名（Gradle 任务）：
  - 构建前，会自动将上述子目录中的 .so 复制到 `app/src/main/jniLibs/arm64-v8a/` 根目录，并统一命名为：
    - delta -> `libdelta.so`
    - pubg -> `libpubg.so`
    - valorant -> `libvalorant.so`
  - 注意：Android 仅打包 ABI 根目录下的 .so；子目录不打包。本项目通过预构建任务确保最终产物包含目标 .so。
- 代码加载规范（按需加载）：
  ```kotlin
  // 以 PUBG 为例：
  NativeLibraryManager.ensureLoaded(NativeLibraryManager.GameLib.Pubg)
  // 然后再调用 native 方法
  NativeLibraryManager.startGameEnhancement(gameFlag = 2, options = 0)
  ```
- 常见问题：
  - 运行时报 UnsatisfiedLinkError：
    - 检查 `app/src/main/jniLibs/arm64-v8a` 根下是否存在 `libdelta.so/libpubg.so/libvalorant.so`
    - 确认设备为 arm64-v8a；若非 64 位设备，请提供对应 ABI 的 .so
    - 混淆规则需保留 native 方法：
      ```
      -keepclasseswithmembers class * { native <methods>; }
      -keep class com.gameassistant.elite.data.nativelib.** { *; }
      ```
1. 添加 `CMakeLists.txt`（示例）
   ```cmake
   cmake_minimum_required(VERSION 3.22.1)
   project(yourlib)

   add_library(
       yourlib
       SHARED
       src/main/cpp/yourlib.cpp
   )

   find_library(log-lib log)
   target_link_libraries(yourlib ${log-lib})
   ```

2. 在 `app/build.gradle.kts` 启用 externalNativeBuild
   ```kotlin
   android {
       defaultConfig {
           externalNativeBuild {
               cmake {
                   arguments += listOf("-DANDROID_STL=c++_shared")
               }
           }
           ndk {
               abiFilters += listOf("arm64-v8a", "armeabi-v7a")
           }
       }
       externalNativeBuild {
           cmake {
               path = file("src/main/cpp/CMakeLists.txt")
           }
       }
   }
   ```

3. 在 Kotlin 侧加载库并声明 `external` 方法，同方案一。

> 建议：若已有成熟的预编译 .so，优先用“方案一”；若需自行编译源码或做平台特定优化，再选择 CMake。

---

## 🚀 安装步骤
1. 克隆项目
   ```bash
   git clone https://github.com/756066377/Elite-Game-Assistant-OpenSource.git
   cd Elite-Game-Assistant-OpenSource
   ```
2. 使用 Android Studio 打开项目（建议最新稳定版）
3. 配置 JDK（重要）：将系统环境变量 `JAVA_HOME` 指向 JDK 17+
4. 同步依赖并构建：等待 Gradle 同步完成后，直接运行到真机或模拟器

> 提示：
> - 首次启动如需使用系统信息、写系统文件等能力，请授予 Root 权限（设备需具备 Root 环境，libsu 会弹窗授权）
> - 部分定制 ROM/设备上 `getenforce` 可能无输出，本项目按要求仅保留该方式，UI 将显示 Unknown

---

## 📘 使用说明
1. 仪表盘页查看设备与系统信息（内核、构建指纹、SELinux 等）
2. 进入“游戏增强”页选择游戏并启动增强流程（示例链路：UI → ViewModel → Repository/NativeManager）
3. 卡密写入示例：输入卡密后将写入 `/data/system/uCard.txt`（路径/加密可在仓库层自定义）

---

## 🤝 贡献指南
欢迎提交 Issue / PR：
1. Fork 本仓库
2. 新建分支：`git checkout -b feature/your-feature`
3. 提交修改：`git commit -m "feat: your message"`
4. 推送分支：`git push origin feature/your-feature`
5. 提交 Pull Request

建议：
- 遵循 Kotlin/Compose 常规代码风格
- 提交前本地跑通构建与基本功能

---

## 📄 许可证
本项目基于 MIT 协议开源。详见 [LICENSE](LICENSE)。