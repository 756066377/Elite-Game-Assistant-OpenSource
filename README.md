# Elite Game Assistant (Open Source Edition)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 📖 Project Introduction

**Elite Game Assistant** is a powerful and feature-rich Android game assistant application. This open-source project demonstrates how to build a fully functional application using a modern Android technology stack, including key practices like native library integration, Root permission management, and a modular architecture. It serves as an excellent learning resource and a solid foundation for secondary development.

## ✨ Main Features

- **Modern UI**: Built entirely with Jetpack Compose for a smooth, fluid, and aesthetically pleasing user experience.
- **System Monitoring Dashboard**: Provides a real-time overview of device status, including kernel version, security status (SELinux), and hardware information.
- **Game List**: Dynamically loads and displays a list of supported games.
- **Game Enhancement**: Includes the core logic for launching game assistance functions (specific implementations need to be developed).
- **Card Key Management**: Retains the basic functionality of writing card keys to files, facilitating custom authentication logic for secondary development.
- **Root Permission Operations**: Integrates the `libsu` library, demonstrating how to perform advanced file operations and execute commands on rooted devices.
- **High-Performance Scrolling**: Optimized scrolling experience for high refresh rate displays (e.g., 120Hz), ensuring silky-smooth list navigation.

## 🛠️ Tech Stack

- **Primary Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Asynchronous Programming**: [Kotlin Coroutines & Flow](https://kotlinlang.org/docs/coroutines-guide.html)
- **Root Permission Management**: [libsu](https://github.com/topjohnwu/libsu)
- **UI Components**: Material 3

## 🚀 Getting Started

### Prerequisites

- Android Studio (Latest stable version, e.g., Hedgehog or newer, is recommended)
- JDK 17 or higher

### Installation and Build

1.  **Clone the Repository**
    ```bash
    git clone https://github.com/your-username/Elite-Game-Assistant-OpenSource.git
    cd Elite-Game-Assistant-OpenSource
    ```

2.  **Open in Android Studio**
    - Launch Android Studio, select `Open`, and navigate to the cloned project directory.

3.  **Configure `JAVA_HOME` (Crucial)**
    - This project's build is heavily dependent on the correct `JAVA_HOME` environment variable. Ensure that `JAVA_HOME` points to the root directory of your installed JDK 17 (or higher). An incorrect configuration will cause the Gradle build to fail.

4.  **Sync and Build**
    - Wait for Android Studio to complete the Gradle sync and download all dependencies.
    - Click `Build` -> `Make Project` or run the app directly on your emulator or physical device.

## 📖 Usage Instructions

1.  **Grant Root Permission**: Upon first launch, the application will request Root access. This is necessary for core functionalities like system information retrieval and file management.
2.  **Dashboard**: The main screen displays a dashboard with key system information.
3.  **Game Enhancement**: Navigate to the "Game Enhancement" tab to see the list of supported games. Select a game and use the provided options to launch the assistance features.
4.  **Card Key Activation**: If you need to use the card key feature, enter your key in the designated input field and click "Activate".

## 🤝 Contribution Guidelines

We welcome contributions of all forms! If you have ideas, suggestions, or have found a bug, please feel free to submit an Issue or a Pull Request.

1.  **Fork** the project.
2.  Create your feature branch (`git checkout -b feature/YourAmazingFeature`).
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4.  Push to the branch (`git push origin feature/YourAmazingFeature`).
5.  Open a new **Pull Request**.

## 📄 License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.