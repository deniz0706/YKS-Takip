# ADR 0002: Incremental shared-domain extraction

- Status: Accepted
- Date: 2026-11-26

## Context

The existing Android application contains business rules mixed with Android-facing repositories and UI. A wholesale rewrite would put exam records, study history, learning progress, photos, backups, and the ranking estimator at unnecessary risk. The 4.0 direction is Kotlin Multiplatform with a shared Compose UI, but the first migration step must be independently buildable and preserve the installed Android application's identity.

## Decision

Create `:core:domain` as a Kotlin Multiplatform module with a JVM target consumed by the current Android app and iOS device/simulator targets declared from the beginning. Move only deterministic, platform-independent rules and value models into `commonMain`.

The first shared slices are:
- Net arithmetic (`NetScore`).
- Exam answer-count and total-duration validation (`ExamValidation`).
- Focus countdown/stopwatch elapsed-time arithmetic (`FocusTimerMath`).
- Daily study-time addition and the `StudyDay` value model.

Keep Android-facing APIs in their existing packages as thin adapters or a typealias. Keep Turkish validation copy in the Android presentation adapter. Existing ranking estimator and lookup tables are not refactored in this step.

## Compatibility and quality gates

- Keep `applicationId = com.deniz0706.ykstakip`.
- Preserve current calculation semantics and user-visible validation messages.
- Add common tests for each shared rule and Android adapter tests for translated validation messages.
- Run `:core:domain:jvmTest`, Android unit tests, and APK builds in CI.
- iOS targets are declared but must not be described as validated until a macOS/Xcode CI job compiles them.
- Do not move persistence, JSON serialization, repositories, photos, or backups until a lossless migration protocol and repository parity tests exist.

## Consequences

The Android application remains the shipping application while shared domain rules are established. iOS can consume the same business rules without a second implementation. The migration is intentionally incremental; this ADR does not claim that Compose Multiplatform UI or shared persistence is complete.
