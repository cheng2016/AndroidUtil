# AndroidUtil (English)

Chinese docs are the default: **[README.md](README.md)** — full API catalog lives there.

Focused, modern Android utilities. Kotlin-first, Java-friendly, modular, safe defaults (private storage, redaction, no IMEI/MAC, no hidden APIs).

## Modules

| Artifact | Highlights |
| --- | --- |
| `androidutil-core` | Logger, CrashHandler, Clipboard, SoftInput, NetworkStatus, ScreenMetrics, Haptics, Battery, Storage, UriFiles, Preferences, Regex, Digest, Encoding, Debouncer, LunarDate, … |
| `androidutil-network` | `AndroidHttpClient`, `HttpResult`, redacting OkHttp logging |
| `androidutil-permissions` | Activity Result permissions + `PermissionGroups` |

## Install

```kotlin
implementation("io.github.cheng2016:androidutil-core:1.0.0-SNAPSHOT")
implementation("io.github.cheng2016:androidutil-network:1.0.0-SNAPSHOT")
implementation("io.github.cheng2016:androidutil-permissions:1.0.0-SNAPSHOT")
```

```bash
./gradlew publishToMavenLocal
```

## Quick taste

```kotlin
CrashHandler.install(applicationContext)
AndroidUtilLogger.init(this, AndroidUtilLogger.Config(fileEnabled = true))

ClipboardHelper.setText(this, token, isSensitive = true)
if (NetworkStatus.isOnline(this)) {
    val result = AndroidHttpClient().get("https://example.com")
}

val permissions = PermissionRequester.register(this)
permissions.request(*PermissionGroups.notifications()) { /* ... */ }
```

Not a replacement for Retrofit, Coil, Crashlytics, or payment SDKs. See [architecture](docs/architecture.md) and [migration](docs/migration.md).

License: Apache 2.0.
