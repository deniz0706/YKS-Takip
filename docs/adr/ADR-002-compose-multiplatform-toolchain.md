# ADR-002 — Compose Multiplatform Toolchain Baseline

- Status: Accepted for the first shared UI slice; subject to CI validation
- Date: 2026-11-26
- Scope: Kotlin/Compose toolchain used by the shared Android/iOS UI module

## Decision

Use the following conservative, mutually compatible baseline for the first Compose Multiplatform implementation:

| Component | Version |
|---|---:|
| Kotlin / Kotlin Multiplatform | 2.2.21 |
| Kotlin Compose compiler Gradle plugin | 2.2.21 |
| Compose Multiplatform Gradle plugin | 1.9.3 |
| Android Gradle Plugin | 8.10.1 (updated to align the bundled Lint/UAST toolchain) |
| Gradle in CI | 8.11.1 (existing) |
| kotlinx.serialization | 1.9.0 |
| SQLDelight | 2.0.2 (unchanged pending separate compatibility validation) |

Kotlin's compatibility guide lists Kotlin 2.2.21 as compatible with Gradle through 8.14 and AGP through 8.11.1. AGP 8.10.1 requires Gradle 8.11.1, which remains the CI baseline. Compose Multiplatform's 1.9.3 release is based on Compose UI/Foundation 1.9.4 and Material3 1.4.0. JetBrains recommends Kotlin 2.2.20 for targets with rapidly evolving support such as iOS. This makes 2.2.21 a suitable baseline for the repository's existing Gradle/AGP setup without jumping to the newest Kotlin toolchain.

Official references:
- https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html
- https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html
- https://kotlinlang.org/docs/multiplatform/whats-new-compose-190.html

## Implementation boundaries

- Compose Multiplatform UI belongs in `:composeApp` commonMain; no Android, Java, or `org.json` APIs are allowed there.
- The existing Android app remains the installable Android application and consumes the shared module as a library.
- iOS builds a static `YksTakipShared` framework and exposes `MainViewController()`. This does not by itself constitute a complete iOS app or a signed release.
- SQLDelight is deliberately not upgraded in this toolchain change. Its Kotlin/AGP compatibility will be evaluated independently against the actual CI result.
- Existing ranking logic, SharedPreferences storage, backup schemas, signing identity, application ID, and versionCode policy are protected.

## Validation gates

- Shared formatting tests pass on JVM.
- `:composeApp:compileDebugKotlinAndroid` passes.
- iOS device and simulator framework compilation pass on macOS CI.
- Existing app APK build and in-place update/signing checks remain green.


## Lint compatibility follow-up
A reproducible release-lint crash was traced to the Lifecycle `NonNullableMutableLiveDataDetector` and a Kotlin Analysis API binary mismatch. Lifecycle 2.9.0 release notes document this exact `KaCallableMemberCall` class/interface incompatibility and recommend aligning the lint toolchain. The project now upgrades AGP to 8.10.1 (within Kotlin 2.2.21's supported AGP range and compatible with Gradle 8.11.1) and pins the Android app's Lifecycle artifacts to stable 2.9.4. Lint remains enabled; the fix is considered accepted only after the APK workflow passes.
