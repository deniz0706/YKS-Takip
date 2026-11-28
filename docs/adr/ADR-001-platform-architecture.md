# ADR-001 — Platform Architecture for YKS Takip 4.0

- Status: Accepted
- Date: 2026-11-26
- Scope: Android and iOS clients, shared application core, local persistence, and future service boundaries

## Context

YKS Takip is an existing Android product with working exam entry, TYT/AYT calculations, OBP and ranking estimation, exam history, study tracking, timers, AI features, image/file handling, and backup/restore behavior. The 4.0 transformation must preserve those capabilities and all user-owned data while establishing Android and iOS as first-class targets.

The product is Turkish-only. A web application is explicitly out of scope. The current Android implementation contains a mixture of Compose and legacy XML/View/Fragment UI, and its established repositories still use SharedPreferences and JSON. A normalized SQLDelight foundation and shared Kotlin domain module now exist on the integration branch, but production repositories have not been migrated and no legacy data import has been enabled.

## Decision

Adopt **Kotlin Multiplatform + Compose Multiplatform** as the primary cross-platform architecture.

- Android remains a first-class target, with Kotlin and Compose UI.
- iOS is a first-class target using shared Kotlin domain/data code and Compose Multiplatform UI where it provides a consistent, maintainable experience.
- Swift and SwiftUI are allowed for the iOS application shell, Apple-specific APIs, and native integrations where they materially improve platform behavior. They must call shared contracts rather than reimplement business rules.
- Shared domain, use cases, validation, calculations, and repository contracts belong in platform-independent Kotlin.
- SQLDelight owns the shared relational schema, queries, and migrations. Platform-specific database drivers remain in Android and iOS source sets.
- Kotlin Serialization defines shared serialization contracts. Existing JSON backup and persistence formats remain compatibility boundaries until a separately tested migration is complete.
- Platform capabilities (files, camera/gallery, notifications, sharing, secure storage, lifecycle, and haptics) are exposed through narrow platform adapters.
- The architecture is offline-first. Core exam, ranking, history, and study workflows must not require network access.
- The backend is deferred until account, cloud sync, or product-provided AI access is an approved requirement. When required, begin with a modular monolith and versioned API contracts; do not introduce microservices pre-emptively.
- Python may be introduced for measurable analytics/ML research, model evaluation, data pipelines, or a separately deployed inference service. It is not an Android runtime dependency.
- Rust or C/C++ may be introduced only for a demonstrated performance or native-library requirement.
- TypeScript is not part of the default mobile architecture; no web client is planned.

## Module and dependency boundaries

The intended dependency direction is:

`platform UI -> application/use cases -> domain contracts`

`platform data adapters -> shared repository contracts`

The domain layer must not import `android.*`, `java.*`, `org.json`, UIKit/SwiftUI, Compose UI, or server frameworks from `commonMain`. Platform implementations belong in platform source sets. Shared code must not depend on platform implementations.

The current `:core:domain` and `:core:database` modules are the first foundations, not proof that the full target architecture is complete. Further modules should be introduced when a real boundary or independently testable capability justifies them; avoid empty modules and speculative microservices.

## UI and design-system decision

All newly implemented screens use declarative UI. Existing XML/View screens are migrated incrementally to Compose, preserving navigation, state, accessibility, and behavior. XML required by Android packaging or system contracts (manifest, backup rules, widget metadata, and similar platform descriptors) remains permitted.

The initial graphite visual direction is a temporary prototype theme, not the final brand. Colors, surfaces, typography, effects, motion, and components must be tokenized or otherwise replaceable so a future Aurora design system does not require rewriting navigation, domain, or data layers.

Responsive layouts must cover phones and tablets. Platform-specific interactions may differ where platform conventions require it, but feature semantics and persisted outcomes must remain consistent.

## Data safety and migration gate

The existing SharedPreferences/JSON repositories remain authoritative until all of the following are true:

1. Every legacy store, key, schema, backup envelope, photo reference, timer state, and recovery snapshot has been inventoried.
2. The importer validates all source material before writing and never interprets malformed data as an empty dataset.
3. Import is idempotent, versioned, transactional where possible, and has explicit duplicate, missing-field, old-schema, corrupt-backup, and interrupted-run tests.
4. Photo bytes and references are transferred and verified without losing ownership or paths.
5. AI credentials remain excluded from ordinary backups and are re-established only through platform secure storage.
6. Counts and content are compared after import; a verified backup and rollback path exist.
7. Production repositories are switched behind stable interfaces only after migration tests and Android/iOS integration checks pass.

No migration is considered complete merely because SQLDelight tables or store adapters exist. Legacy data is not deleted as part of the initial cutover.

## Protected compatibility constraints

- Android application ID remains `com.deniz0706.ykstakip`.
- Existing signing identity and preview update path must not be changed without explicit verification against the installed APK.
- CI versionCode remains monotonically increasing.
- Existing backup formats and JSON field names remain readable until compatibility is demonstrated.
- The ranking engine files (`YksRankingEstimator.kt`, `YksOfficialRankingLookup.kt`, and `YksRankingCalculator.kt`) are protected. Characterization fixtures must pass before any separately authorized change.
- No secret, keystore, password, or personal backup is committed to the repository.

## Validation and rollout

1. Keep Android CI green and run shared domain/database tests.
2. Compile shared Kotlin targets for iOS device and simulator on macOS CI.
3. Add the first real Compose Multiplatform vertical slice using the existing ranking engine and repository contracts.
4. Complete legacy data inventory and importer tests before production persistence cutover.
5. Migrate Android screens incrementally and remove obsolete XML UI only after reference and behavior checks.
6. Add iOS app shell, platform adapters, signing documentation, and device validation with macOS/Xcode and Apple tooling.
7. Add backend/AI gateway only when the product requirement and security model are approved.

CI compilation of Kotlin iOS targets is not equivalent to a complete iOS app build, Xcode archive, code signing, simulator launch, or App Store validation. Those claims require the corresponding Apple toolchain checks.

## Consequences

Positive:
- Business rules and data contracts can be shared without duplicating ranking and exam behavior.
- Android and iOS can evolve with a common offline-first core.
- Native integrations remain possible without contaminating shared code.
- UI design can evolve independently from domain and persistence.
- Migration risk is constrained by an explicit legacy compatibility gate.

Trade-offs:
- KMP/Compose Multiplatform introduces Kotlin Native and Apple toolchain requirements.
- Some platform features need separate adapters and platform-specific testing.
- Sharing UI does not eliminate the need for iOS-specific accessibility, interaction, signing, and release validation.
- Migration is incremental; legacy Android persistence and XML screens remain until each slice is verified.

## Rejected alternatives

- **Flutter/Dart as a second prototype:** rejected because the platform decision is already KMP + Compose Multiplatform; a parallel Flutter prototype would duplicate effort and fragment the product.
- **Two fully separate native clients:** rejected as the default because it duplicates domain behavior and increases parity risk. Native SwiftUI remains available for justified iOS-specific surfaces.
- **Backend-first or microservices-first:** rejected because the current product's core workflows should remain offline-capable and there is no approved account/sync requirement yet.
- **Polyglot for its own sake:** rejected. New languages require a concrete capability, measurable benefit, and CI/toolchain support.
