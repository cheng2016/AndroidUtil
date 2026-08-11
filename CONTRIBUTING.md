# Contributing

Thanks for helping improve AndroidUtil.

## Before opening a change

1. Search existing issues and discussions.
2. Keep utilities focused; prefer platform or established libraries when they already solve the problem.
3. Never add APIs that collect hardware identifiers, bypass Android security, or write sensitive data to shared storage.

## Development

Requirements: JDK 17, Android SDK 36, and Git.

```bash
./gradlew spotlessApply
./gradlew check apiCheck
./gradlew assembleRelease
```

Public API changes must include KDoc, tests, a migration note, and an updated API dump (`./gradlew apiDump`). Use Conventional Commit-style subjects when practical.

By contributing, you agree that your contribution is licensed under Apache-2.0.
