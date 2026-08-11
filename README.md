# AndroidUtil

[![CI](https://github.com/cheng2016/AndroidUtil/actions/workflows/ci.yml/badge.svg)](https://github.com/cheng2016/AndroidUtil/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)
[![API](https://img.shields.io/badge/Android-23%2B-brightgreen.svg)](https://developer.android.com/)

Focused Android utilities with safe defaults. Kotlin-first, Java-friendly, modular, and deliberately smaller than a framework.

[简体中文](README.zh-CN.md) · [Migration guide](docs/migration.md) · [Architecture](docs/architecture.md) · [Contributing](CONTRIBUTING.md)

<p align="center"><img src="docs/sample-preview.svg" width="280" alt="AndroidUtil sample application preview"></p>

## Why AndroidUtil?

- Add only what you need: `core`, `network`, and `permissions` are independent.
- Modern Android: AndroidX, Activity Result APIs, coroutines, app-private storage, and API 36.
- Safe defaults: no IMEI/MAC collection, hidden-API hooks, shared-storage secrets, or release body logging.
- Testable: injectable network clients, deterministic result types, and no UI hidden inside utilities.
- Easy from Kotlin and Java.

AndroidUtil does **not** compete with Retrofit, Coil, DataStore, crash-reporting platforms, or vendor payment SDKs. It removes repetitive glue around smaller jobs.

## Requirements

- Android API 23+
- compileSdk 36
- JDK 17 for building this repository

## Installation

The first Maven Central release is being prepared. For now, publish snapshots locally:

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

## Five-minute tour

### Private, redacted logs

```kotlin
AndroidUtilLogger.init(
    context = applicationContext,
    config = AndroidUtilLogger.Config(fileEnabled = true),
)
AndroidUtilLogger.i("Checkout", "Order accepted")
```

Logs are opt-in and written below the application's private files directory.

### Small cancellable HTTP calls

```kotlin
val client = AndroidHttpClient()
when (val result = client.get("https://example.com/status")) {
    is HttpResult.Success -> render(result.body)
    is HttpResult.HttpFailure -> showError("HTTP ${result.code}")
    is HttpResult.NetworkFailure -> showError(result.cause.message)
    HttpResult.Cancelled -> Unit
}
```

The client accepts your own `OkHttpClient`. Diagnostic logging is disabled unless explicitly installed and redacts credentials.

### Lifecycle-aware permissions

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

Register during Activity or Fragment initialization. No `onRequestPermissionsResult` forwarding is needed.

## Modules

| Module | Purpose |
| --- | --- |
| `androidutil-core` | Private logging, app metadata, preferences, resource reading, regex, digests, and lunar date utilities |
| `androidutil-network` | Coroutine and callback OkHttp APIs with explicit results and safe optional logging |
| `androidutil-permissions` | Activity Result permission requests and modern permission groups |
| `sample` | Runnable Kotlin examples for every supported capability |

## Build and verify

```bash
./gradlew spotlessCheck check apiCheck assembleRelease
```

See [CONTRIBUTING.md](CONTRIBUTING.md) before changing public APIs. Releases follow Semantic Versioning and are documented in [CHANGELOG.md](CHANGELOG.md).

## Migrating from the old repository

The historical repository was a collection of Java snippets and could not compile as a library. Version 1.0 removes unsafe or host-specific code instead of preserving misleading compatibility. See the [migration table](docs/migration.md) for replacements and rationale.

## License

Copyright 2018–2026 Cheng Zhenjia.

Licensed under the [Apache License 2.0](LICENSE).
