# AndroidUtil

[![CI](https://github.com/cheng2016/AndroidUtil/actions/workflows/ci.yml/badge.svg)](https://github.com/cheng2016/AndroidUtil/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)
[![API](https://img.shields.io/badge/Android-23%2B-brightgreen.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-first-7F52FF.svg)](https://kotlinlang.org/)
[![Java](https://img.shields.io/badge/Java-friendly-orange.svg)](sample/src/main/java/io/github/cheng2016/androidutil/sample/JavaExamples.java)

**把现代 Android 开发里真正反复用到的工具，收成可依赖、默认可上架的库。**

Kotlin 优先 · Java 可直接调用 · 按模块引入 · 私有存储 / 脱敏 / 无 IMEI·MAC · 面向 API 36

[English](README.en.md) · [迁移指南](docs/migration.md) · [架构边界](docs/architecture.md) · [参与贡献](CONTRIBUTING.md)

<p align="center"><img src="docs/sample-preview.svg" width="280" alt="AndroidUtil 示例应用预览"></p>

---

## 一句话定位

| 你得到的 | 你不会得到的 |
| --- | --- |
| 日志、崩溃落盘、权限、轻量 HTTP、剪贴板、键盘、网络状态、屏幕密度、触感、电池、存储、Uri、通知渠道、防抖、编解码… | 又一套 Retrofit / Coil / 支付 / 埋点 / 大而全 UI 框架 |
| 可测、可注入、结果类型明确 | 隐藏 API、Hook、公共目录写密钥 |
| 复制即用的 Kotlin / Java 示例 | 「私人片段仓库」式无法编译的源码堆 |

---

## 安装

```bash
./gradlew publishToMavenLocal
```

```kotlin
dependencies {
    implementation("io.github.cheng2016:androidutil-core:1.0.0-SNAPSHOT")
    implementation("io.github.cheng2016:androidutil-network:1.0.0-SNAPSHOT")
    implementation("io.github.cheng2016:androidutil-permissions:1.0.0-SNAPSHOT")
}
```

| 模块 | 何时引入 |
| --- | --- |
| **core** | 几乎所有 App：日志、崩溃、设备/屏幕/存储/剪贴板/… |
| **network** | 需要轻量 OkHttp 胶水，又不想上完整 Retrofit 时 |
| **permissions** | 运行时权限 + Android 13+ 通知/媒体权限组 |

环境：minSdk **23** · compileSdk **36** · 构建需 **JDK 17**

---

## 能力全景（按场景选工具）

### 1. 稳定性与可观测

| 类 | 一句话 | 亮点 |
| --- | --- | --- |
| **AndroidUtilLogger** | 线程安全日志 | 文件默认关；开启后写入 `{filesDir}/logs/` |
| **RedactingSanitizer** | 日志脱敏 | 自动遮盖 password / token / Authorization / Cookie |
| **CrashHandler** | 本地崩溃落盘 | 显式 `install`；`{filesDir}/logs/crash/`；链式系统处理器 |

```kotlin
AndroidUtilLogger.init(this, AndroidUtilLogger.Config(fileEnabled = true))
AndroidUtilLogger.setSanitizer(RedactingSanitizer)
CrashHandler.install(applicationContext)
```

### 2. 权限（`androidutil-permissions`）

| 类 | 一句话 | 亮点 |
| --- | --- | --- |
| **PermissionRequester** | Activity Result 权限 | 无需转发 `onRequestPermissionsResult` |
| **PermissionGroups** | 按 SDK 展开权限组 | `notifications()` / `images()` / `mediaAll()`… |
| **PermissionStatus** | 授权结果折叠 | Granted / Denied / PermanentlyDenied |

```kotlin
private val permissions = PermissionRequester.register(this)
permissions.request(*PermissionGroups.notifications()) { result ->
    when (result.status) {
        PermissionStatus.Granted -> Unit
        is PermissionStatus.Denied -> explain(it.permissions)
        is PermissionStatus.PermanentlyDenied -> Intents.appDetailsSettings(this)
    }
}
```

### 3. 网络（`androidutil-network` + core 状态）

| 类 | 一句话 | 亮点 |
| --- | --- | --- |
| **AndroidHttpClient** | 可取消 OkHttp 胶水 | `suspend` + Java `*Async`；可注入客户端 |
| **HttpResult** | 四种明确结果 | Success / HttpFailure / NetworkFailure / Cancelled |
| **RedactingLoggingInterceptor** | 诊断日志 | 默认 `NONE`；脱敏头与 token 字段 |
| **NetworkStatus** | 连通性快照/监听 | `NetworkCapabilities`；不读 SSID/MAC |

```kotlin
val client = AndroidHttpClient()
when (val r = client.get("https://example.com")) {
    is HttpResult.Success -> r.body
    is HttpResult.HttpFailure -> "HTTP ${r.code}"
    is HttpResult.NetworkFailure -> r.cause.message
    HttpResult.Cancelled -> null
}

val sub = NetworkStatus.observe(this) { snap ->
    // snap.online / validated / wifi / cellular / vpn
}
// onDestroy: sub.close()
```

### 4. 系统交互与 UI 胶水

| 类 | 一句话 | 亮点 |
| --- | --- | --- |
| **ClipboardHelper** | 剪贴板 | API 33+ `isSensitive` 弱化预览 |
| **SoftInput** | 软键盘显隐 | show / hide / toggle |
| **Toasts** | 轻提示 | Application Context；可 `cancel` 上一条 |
| **Haptics** | 触感震动 | `VibrationEffect`，兼容新旧 API |
| **TextSpans** | 富文本拼接 | 链式 bold / color / size |
| **Intents** | 常用系统跳转 | 分享、设置页、浏览器、拨号、邮件；失败返回 false |
| **NotificationChannels** | 通知渠道 | API 26+ `ensure` 一键创建 |

```kotlin
ClipboardHelper.setText(this, token, isSensitive = true)
SoftInput.hide(this)
Toasts.short(this, "已复制")
Haptics.tick(this)
Intents.shareText(this, "试试 AndroidUtil")
NotificationChannels.ensure(this, "msg", "消息")
```

### 5. 屏幕 / 设备 / 进程 / 电源 / 存储

| 类 | 一句话 | 亮点 |
| --- | --- | --- |
| **ScreenMetrics** | dp/sp/px · 屏宽高 · 深色模式 | 含 `Context.dp()` 扩展 |
| **DeviceProfile** | 机型摘要 | **无** IMEI/MAC/广告 ID |
| **ProcessInfo** | 主进程判断 | 多进程初始化时常用 |
| **BatteryStatus** | 电量/充电快照 | USB / AC / 无线 |
| **StorageSpace** | 可用空间 | internal / externalApp |
| **AppInfo** | 包名/版本/应用名 | 只读宿主元数据 |

```kotlin
val pad = dp(16)
if (ScreenMetrics.isDarkMode(this)) { /* ... */ }
if (ProcessInfo.isMainProcess(this)) CrashHandler.install(this)
val space = StorageSpace.internal(this)
Toasts.short(this, ByteSize.format(space.freeBytes) + " 可用")
```

### 6. 存储与配置

| 类 | 一句话 | 亮点 |
| --- | --- | --- |
| **Preferences** | SharedPreferences 薄封装 | 一律 `apply()`；可 `wrap` |
| **ResourceReader** | assets / raw | 自动关流；Properties 便捷读取 |
| **UriFiles** | content Uri | 显示名/大小/MIME；拷贝到 cache |

### 7. 文本 / 校验 / 摘要 / 编解码 / 时间

| 类 | 一句话 | 亮点 |
| --- | --- | --- |
| **RegexUtils** / **RegexConstants** | 手机号·邮箱·身份证… | 匹配 / 提取 / 替换 |
| **DigestUtils** | SHA-256 / MD5 | MD5 仅非安全场景 |
| **EncodingUtils** | Base64 / Hex | URL_SAFE 可选 |
| **ByteSize** | `1.5 MB` | 人类可读 |
| **TimeFormats** | 相对时间 · 时长 | 「3分钟前」/ `mm:ss` |
| **VersionUtils** | 版本比较 | `isAtLeast("2.1.0", "2.0.0")` |
| **LunarDate** | 公历→农历 | 生肖 / 干支 / 闰月 |

### 8. 并发与交互节奏

| 类 | 一句话 | 亮点 |
| --- | --- | --- |
| **MainThread** | 主线程投递 | `run` / `post` / `postDelayed` / `Executor` |
| **Debouncer** | 防抖 | 按钮连点杀手 |
| **Throttler** | 节流 | 滑动/传感器场景 |

```kotlin
val debounce = Debouncer(600)
button.setOnClickListener {
    if (debounce.tryAcquire()) submit()
}
MainThread.postDelayed(300) { refresh() }
```

---

## Java 一样好用

```java
CrashHandler.install(context);
Preferences prefs = Preferences.of(context);
ClipboardHelper.setText(context, "secret", "token", true);
String dig = DigestUtils.sha256Hex("AndroidUtil");
client.getAsync(url, result -> { /* OkHttp 线程回调 */ });
```

完整互操作见 [`JavaExamples.java`](sample/src/main/java/io/github/cheng2016/androidutil/sample/JavaExamples.java)，可运行演示见 `:sample`。

---

## 设计原则（为什么值得信赖）

1. **默认可上架**：私有目录、敏感剪贴板标记、日志脱敏、无隐藏 API。
2. **UI 归属宿主**：工具层不抢 Activity 生命周期；Toast 仅轻提示。
3. **可测试**：网络可注入；纯函数工具有单测；公开 API 有 binary-compatibility 门禁。
4. **不重复造轮子**：图片用 Coil/Glide，列表网络用 Retrofit，崩溃云端用 Crashlytics/Sentry——本库与之**并存**，不替代。

详见 [架构与边界](docs/architecture.md)。从旧 `util/` 片段迁移见 [迁移指南](docs/migration.md)。

## 构建

```bash
./gradlew spotlessCheck check apiCheck assembleRelease
```

## 许可证

Copyright 2018–2026 Cheng Zhenjia · [Apache License 2.0](LICENSE)
