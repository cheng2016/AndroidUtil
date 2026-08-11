# AndroidUtil

[![CI](https://github.com/cheng2016/AndroidUtil/actions/workflows/ci.yml/badge.svg)](https://github.com/cheng2016/AndroidUtil/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)
[![API](https://img.shields.io/badge/Android-23%2B-brightgreen.svg)](https://developer.android.com/)

一套少而精、默认安全的 Android 工具库：Kotlin-first、Java 友好、按需引入，不试图成为大而全的框架。

[English](README.md) · [迁移指南](docs/migration.md) · [架构与边界](docs/architecture.md) · [参与贡献](CONTRIBUTING.md)

<p align="center"><img src="docs/sample-preview.svg" width="280" alt="AndroidUtil 示例应用预览"></p>

## 为什么选择 AndroidUtil？

- `core`、`network`、`permissions` 相互独立，只引入所需能力。
- 面向现代 Android：AndroidX、Activity Result、协程、应用私有存储、API 36。
- 默认安全：不采集 IMEI/MAC，不使用隐藏 API，不向公共存储写敏感信息，不在 Release 打印网络正文。
- 易于测试：网络客户端可注入、结果类型明确、工具层不私自展示 UI。
- Kotlin 简洁调用，同时照顾 Java 项目。

本项目不会重复造 Retrofit、Coil、DataStore、崩溃平台或支付 SDK。它只解决那些足够常见、又不值得引入完整框架的小问题。

## 环境要求

- Android API 23+
- compileSdk 36
- 构建仓库需要 JDK 17

## 安装

首个 Maven Central 正式版本正在准备中。目前可以先发布到本机 Maven：

```bash
./gradlew publishToMavenLocal
```

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.github.cheng2016:androidutil-core:1.0.0-SNAPSHOT")
    implementation("io.github.cheng2016:androidutil-network:1.0.0-SNAPSHOT")
    implementation("io.github.cheng2016:androidutil-permissions:1.0.0-SNAPSHOT")
}
```

## 五分钟上手

### 写入应用私有日志

```kotlin
AndroidUtilLogger.init(
    context = applicationContext,
    config = AndroidUtilLogger.Config(fileEnabled = true),
)
AndroidUtilLogger.i("Checkout", "Order accepted")
```

文件日志默认关闭；开启后只写入应用私有目录，并可配置脱敏规则。

### 发起轻量、可取消的 HTTP 请求

```kotlin
val client = AndroidHttpClient()
when (val result = client.get("https://example.com/status")) {
    is HttpResult.Success -> render(result.body)
    is HttpResult.HttpFailure -> showError("HTTP ${result.code}")
    is HttpResult.NetworkFailure -> showError(result.cause.message)
    HttpResult.Cancelled -> Unit
}
```

可以注入自己的 `OkHttpClient`。诊断日志默认关闭；显式启用时会遮盖凭证字段。

### 生命周期安全的权限请求

```kotlin
private val permissions = PermissionRequester.register(this)

fun requestNotifications() {
    val required = PermissionGroups.notifications()
    if (required.isEmpty()) return continueFeature()
    permissions.request(*required) { result ->
        when (val status = result.status) {
            PermissionStatus.Granted -> continueFeature()
            is PermissionStatus.Denied -> explain(status.permissions)
            is PermissionStatus.PermanentlyDenied -> openAppSettings()
        }
    }
}
```

请在 Activity/Fragment 初始化阶段注册；不再需要转发 `onRequestPermissionsResult`。

## 模块

| 模块 | 能力 |
| --- | --- |
| `androidutil-core` | 私有日志、应用信息、偏好、资源读取、正则、摘要、农历 |
| `androidutil-network` | 协程与回调式 OkHttp、明确结果、可选安全日志 |
| `androidutil-permissions` | Activity Result 权限请求与现代权限组 |
| `sample` | 覆盖全部受支持能力的可运行示例 |

## 构建与验证

```bash
./gradlew spotlessCheck check apiCheck assembleRelease
```

旧仓库是无法整体编译的 Java 片段集合。1.0 不保留会误导用户的不安全兼容层，详见[迁移指南](docs/migration.md)。

## 许可证

Copyright 2018–2026 Cheng Zhenjia，使用 [Apache License 2.0](LICENSE)。
