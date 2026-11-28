# ADR 0001: Kotlin Multiplatform + Compose Multiplatform

- Status: Accepted direction; validate with a buildable vertical slice before broad migration.
- Date: 2026-11-26

## Context

YKS Takip is an established Android application with Kotlin business logic, Jetpack Compose screens, legacy XML/Fragment/ViewBinding screens, SharedPreferences/JSON persistence, backup/restore behavior, and ranking calculations that require strict regression protection. The product must remain Turkish-only, ship Android first, and support iOS as a first-class target. A web client is explicitly out of scope.

The migration must preserve existing users' exam records, study history, curriculum progress, photos, settings, AI configuration boundaries, and backups. Replacing the whole app in one step would create unacceptable data and release risk.

## Decision

Use Kotlin Multiplatform as the shared architecture foundation and Compose Multiplatform for new shared UI where platform parity is adequate.

- Keep domain entities, use cases, validation, ranking contracts, and portable repository interfaces in common code.
- Keep Android Context, SharedPreferences, WorkManager, notifications, widgets, file pickers, secure storage, and Android lifecycle integrations in Android adapters.
- Keep iOS-specific Keychain, notifications, widgets/live activities, photo/file pickers, and lifecycle integrations in iOS adapters.
- Use kotlinx.serialization for shared serialization and SQLDelight for shared relational persistence, introduced behind repository interfaces.
- Retain native Swift/SwiftUI adapters for platform-specific experiences when they materially improve iOS integration; do not duplicate business rules.
- Move one vertical slice at a time. The legacy Android app remains the working reference and rollback path until feature and data parity are demonstrated.

## Consequences

Positive:
- Reuses the existing Kotlin domain knowledge and reduces duplicate business logic.
- Creates a credible shared Android/iOS foundation.
- Enables common unit tests for calculations and domain behavior.
- Allows Compose Multiplatform for shared UI while preserving native escape hatches.

Costs and risks:
- Existing Android classes cannot simply be moved into common code; platform dependencies must be separated deliberately.
- Database introduction requires versioned, lossless migration from legacy JSON/SharedPreferences and photo storage.
- Some UI and system integrations will remain platform-specific.
- iOS build, signing, and distribution require macOS/Xcode and Apple developer tooling; this cannot be fully validated from a Linux Codespace.

## Rejected alternatives

- Flutter as a parallel prototype: rejected for this project because it duplicates the established Kotlin domain layer and creates a second implementation before a validated need is shown.
- Full native rewrite in Swift plus Kotlin: rejected because it duplicates business logic and raises maintenance cost.
- Big-bang replacement of the Android app: rejected because it risks user data, ranking parity, and update continuity.

## Validation required before broad adoption

- Build the same small vertical slice on Android and iOS.
- Verify common domain tests and Android unit tests.
- Verify SQLDelight migration behavior with representative legacy backups, including photos and malformed/partial input.
- Compare startup, scrolling, memory, animation, and accessibility on real devices.
- Confirm the iOS integration path on macOS/Xcode before committing to a release date.
