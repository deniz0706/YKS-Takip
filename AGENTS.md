# YKS Takip — Engineering Instructions

## Product invariants
- The product is Turkish-first and Turkish-only. Do not add English UI copy or localization unless explicitly requested.
- Android is the first delivery target; iOS is a first-class architecture target. Do not create a web client.
- Keep the Android application ID exactly `com.deniz0706.ykstakip`.
- Preserve existing user data, exam records, study history, learning progress, photos, settings, backup formats, and restore compatibility.
- Preserve the existing exam-entry, TYT/AYT, OBP, ranking, history, statistics, study, timer, AI coach, image-question, backup/restore, and settings capabilities.
- Never embed provider API keys, signing keys, keystores, or personal backups in source control or APK assets.

## Architecture direction
- Target Kotlin Multiplatform + Compose Multiplatform for shared domain/data/UI where platform support is sound.
- Use native Swift/SwiftUI adapters only where iOS platform APIs or UX justify them; do not create a second business-logic implementation.
- Keep domain/business logic platform-independent. Common code must not depend on `android.*`, `java.*`, `org.json`, Android Context, SharedPreferences, or other platform-only APIs.
- Prefer explicit platform interfaces and implementations over expect/actual sprawl.
- Use kotlinx.serialization for shared serialization and SQLDelight for the shared relational persistence layer, introduced behind repository interfaces.
- Keep a modular monolith until measured scale justifies services. AI provider access must go through a provider abstraction/gateway; never ship a product-wide secret.
- New UI is declarative Compose Multiplatform. Do not add new XML screen layouts; retain XML only where required for Android platform configuration or legacy compatibility during migration.
- Build an adaptable design system. Graphite/carbon is a temporary foundation; the later Aurora visual identity must be achievable without rewriting architecture.

## Data safety and migrations
- Treat existing SharedPreferences/JSON and all backup/export formats as production data contracts.
- Build a lossless, idempotent, versioned importer before replacing any legacy store.
- A failed migration must leave the original data intact and provide a recovery path.
- Preserve photo files and their references. Test missing fields, unknown fields, corrupt input, partial backups, repeated imports, and rollback/recovery.
- Do not delete or retire legacy storage until migration parity and backup/restore tests pass.

## Ranking and exam calculations
- Treat `YksRankingEstimator.kt`, `YksOfficialRankingLookup.kt`, `YksRankingCalculator.kt`, and their tests as protected behavior.
- Do not tune constants, replace lookup tables, or refactor ranking semantics without characterization vectors and explicit regression evidence.
- Preserve the known high-net TYT regression: approximately 114 net with diploma 88.2 must remain in the top few hundred (historical expected center around 240; verify against the current fixture before changing).
- Preserve OBP behavior (diploma grade × 0.60 in the app's score flow) and previously-placed coefficient behavior.
- Every ranking change requires before/after fixtures and tests for all supported fields and boundaries.

## Git and delivery workflow
- Never commit directly to `main`.
- Work on `4.0/foundation` or a focused `4.0/<area>` feature branch. Keep branch count intentional; do not create a branch for every tiny edit.
- Inspect existing branches and PRs before recreating work. Do not merge stale integration branches or cherry-pick by branch name alone; inspect ancestry and patches first.
- Do not force-push, rewrite shared history, delete branches, or merge PRs automatically.
- Keep commits focused and descriptive. Do not merge feature work until relevant tests and CI pass.
- Do not change signing configuration or publish an update-compatible APK until the actual installed certificate fingerprint is verified. Never advise uninstalling the installed app as a shortcut.
- Keep `docs/DEVELOPMENT-STATUS.md` current with branch, base SHA, decisions, completed work, test evidence, risks, and next steps.

## Quality gates
- Add tests before or alongside behavior changes.
- Run the narrowest relevant unit tests first, then the full unit-test suite and Android build.
- For platform/shared code, add common tests plus Android/iOS compile checks where available.
- Check accessibility, loading/empty/error states, offline behavior, performance, and Turkish copy.
- Do not claim a test/build passed unless a real CI or local result confirms it. If the environment cannot run a check, state that clearly.
- Do not replace real repositories, ranking, or persisted data with static mock data in a production vertical slice.
