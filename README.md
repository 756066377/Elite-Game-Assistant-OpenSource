# Elite Game Assistant（开源版）

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📖 项目简介
Elite Game Assistant 是一个基于现代 Android 技术栈的游戏助手应用示例，涵盖 Jetpack Compose 界面、Root 权限管理（libsu）、模块化架构等实践。适合学习、二次开发或作为业务项目的脚手架。

## ✨ 主要功能
- 现代化 UI：基于 Jetpack Compose，视觉清晰、交互流畅
- 系统监控仪表盘：展示内核版本、构建指纹、SELinux 状态等
- 游戏列表与增强入口：提供游戏增强的调用流程（具体能力请自行实现/接入）
- 卡密写入示例：以 Root 权限写入指定文件，便于自定义鉴权
- Root 权限操作：集成 libsu，支持以 su 执行命令
- 高刷滑动优化：针对 120Hz 等高刷新率设备优化滚动体验

## 🛠 技术栈
- 语言：Kotlin
- UI：Jetpack Compose（Material3）
- 架构：MVVM + Hilt 依赖注入
- 并发：Kotlin Coroutines & Flow
- Root：libsu（topjohnwu）
- 其他：Gradle Kotlin DSL

## 🚀 安装步骤
1. 克隆项目
   ```bash
   git clone https://github.com/756066377/Elite-Game-Assistant-OpenSource.git
   cd Elite-Game-Assistant-OpenSource
   ```
2. 使用 Android Studio 打开项目（建议最新稳定版）
3. 配置 JDK（重要）
   - 请将系统环境变量 `JAVA_HOME` 指向 JDK 17 或以上版本
4. 同步依赖并构建
   - 等待 Gradle 同步完成
   - 直接运行到真机或模拟器

## 📘 使用说明
1. 首次启动授予 Root 权限（如需使用系统信息、写文件等能力）
2. 仪表盘页查看设备与系统信息（内核、构建指纹、SELinux 等）
3. 进入“游戏增强”页选择游戏并启动增强流程（示例调用链已打通）
4. 卡密写入：在相应入口输入卡密后写入到系统路径（示例为 `/data/system/uCard.txt`），可按需修改

提示：
- 部分定制 ROM/设备上 `getenforce` 结果可能获取不到，此时 UI 会显示 Unknown（本项目按您的要求仅保留 `getenforce` 方式）
- 高刷新率滚动优化已启用，低端设备上可按需关闭或下调参数

## 🤝 贡献指南
欢迎提交 Issue / PR 参与共建：
1. Fork 本仓库
2. 新建分支：`git checkout -b feature/your-feature`
3. 提交修改：`git commit -m "feat: your message"`
4. 推送分支：`git push origin feature/your-feature`
5. 提交 Pull Request

建议：
- 遵循 Kotlin/Compose 常规代码风格
- 提交前本地跑通构建

## 📄 许可证
本项目基于 MIT 协议开源。详见 [LICENSE](LICENSE)。