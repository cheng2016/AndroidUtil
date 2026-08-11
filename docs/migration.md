# 从旧「源码片段」仓库迁移

1.0 是刻意重置。旧的 `util/` 目录本身无法作为可构建的库发布，且默认包名 API 与新模块不存在二进制兼容。

| 旧源码 | 替代 | 说明 |
| --- | --- | --- |
| `Logger` | `AndroidUtilLogger` | 文件日志默认关闭；开启后写入 `{filesDir}/logs/`。 |
| `CrashHandler` | `CrashHandler` | 需显式 `install`；报告写入 `{filesDir}/logs/crash/`，不再写外部存储、不弹 Toast、不反射整表 `Build`。 |
| `PreferenceUtils` | `Preferences` | 统一 `apply()` 写入，仅用 Application Context。 |
| `RegexUtils` / `MD5Utils` | `RegexUtils` / `DigestUtils` | MD5 仅保留非安全校验场景，完整性优先用 SHA-256。 |
| `AppUtils` / `ResourceUtils` | `AppInfo` / `ResourceReader` | 无全局 Application 单例，无写死的 asset 名。 |
| `OkHttpUtil`、`HttpUtil`、`HttpClientUtils` | `AndroidHttpClient` | 协程优先、可取消、可注入、不绑 UI。 |
| `PermissionHelper`、`Permissons4App` | `PermissionRequester` | 在 STARTED 之前注册；无需转发权限回调。 |
| `ImageDownLoader` | Coil / Glide | 图片加载不在本库聚焦范围内。 |
| `BaseFragment` | AndroidX Fragment 生命周期 API | 使用 `viewLifecycleOwner`、`repeatOnLifecycle`。 |
| `DeviceUtils` 的 IMEI/MAC | 无替代 | 受限持久标识不适合作为通用工具库能力。 |
| `HookStartActivityUtil` | 无替代 | 绕过清单检查且依赖不稳定的隐藏 API。 |
| `AlipayUtil` / `PluginFactory` | 应用内对接官方厂商 SDK | 支付等敏感能力必须跟随官方 SDK。 |
| `XmlUtils` 存 token | DataStore / 加密应用存储 | Token 不得打日志，也不得写入共享外部存储。 |

## 依赖迁移

删掉拷贝的 Java 文件，只引入需要的模块：

```kotlin
dependencies {
    implementation("io.github.cheng2016:androidutil-core:<version>")
    implementation("io.github.cheng2016:androidutil-network:<version>")
    implementation("io.github.cheng2016:androidutil-permissions:<version>")
}
```

公开 API 的详细用法见根目录 [README.md](../README.md)。英文对照见 [README.en.md](../README.en.md)。
