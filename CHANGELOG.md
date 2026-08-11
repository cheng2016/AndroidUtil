# Changelog

This project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added

- Kotlin-first `core`, `network`, and `permissions` libraries.
- Android 16 / API 36 support with a Java 17 toolchain.
- Sample application, automated tests, linting, formatting, API checks, and Maven Central publishing.
- English and Simplified Chinese documentation and community policies.

### Changed

- Replaced callback-era networking with cancellable OkHttp and coroutine APIs.
- Replaced legacy permission forwarding with the Activity Result API.
- Moved file logs to application-private storage with safe defaults.

### Removed

- Hidden API / Activity launch hooks, hardware identifier collection, AsyncHttp, U8 and legacy Alipay bindings, ButterKnife base classes, and shared-storage implementations.
