# Migration from the source-snippet repository

Version 1.0 is a deliberate reset. The old `util/` directory was not a buildable library and its default-package APIs are not binary compatible with the new modules.

| Legacy source | Replacement | Notes |
| --- | --- | --- |
| `Logger` / `CrashHandler` | `core` logging API | Uses app-private storage; crash-handler installation is intentionally not automatic. |
| `PreferenceUtils` | `Preferences` | Consistent `apply()` writes and application context only. |
| `RegexUtils` / `MD5Utils` | `RegexUtils` / `DigestUtils` | MD5 is retained only for non-security checksums. Prefer SHA-256. |
| `AppUtils` / `ResourceUtils` | `AppInfo` / `ResourceReader` | No global application singleton or hard-coded asset name. |
| `OkHttpUtil`, `HttpUtil`, `HttpClientUtils` | `AndroidHttpClient` | Coroutine-first, cancellable, injectable, and UI-independent. |
| `PermissionHelper`, `Permissons4App` | `PermissionRequester` | Register before `STARTED`; no result forwarding is required. |
| `ImageDownLoader` | Coil or Glide | Image loading is outside this focused library's scope. |
| `BaseFragment` | AndroidX Fragment lifecycle APIs | Use `viewLifecycleOwner` and `repeatOnLifecycle`. |
| `DeviceUtils` IMEI/MAC methods | No replacement | Restricted persistent identifiers are not appropriate for a general utility library. |
| `HookStartActivityUtil` | No replacement | It bypassed manifest checks and relied on hidden, unstable platform APIs. |
| `AlipayUtil` / `PluginFactory` | Vendor SDK integration modules in the app | Security-sensitive vendor integrations must track current official SDKs. |
| `XmlUtils` token storage | DataStore / encrypted app storage | Tokens must not be logged or written to shared external storage. |

## Dependency migration

Replace copied Java files with only the modules you need:

```kotlin
dependencies {
    implementation("io.github.cheng2016:androidutil-core:<version>")
    implementation("io.github.cheng2016:androidutil-network:<version>")
    implementation("io.github.cheng2016:androidutil-permissions:<version>")
}
```

Until the first Maven Central release, use `publishToMavenLocal` and the snapshot version shown in `gradle.properties`.
