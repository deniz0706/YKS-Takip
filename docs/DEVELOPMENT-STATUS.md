# YKS Takip 4.0 — Development Status

Updated: 2026-11-26

## Current integration point
- Working branch: `4.0/foundation`
- Branch created from: `4.0/architecture-audit`
- Base project main commit observed during audit: `93f7f044717d10b4c54014d6fbfc7a72bd9fbfeb`
- Architecture-audit branch is 15 commits ahead of main; its changes are limited to CI/build/signing documentation and the initial 4.0 architecture audit.
- Existing PR #51 remains open and targets main. Do not merge it automatically.

## Repository inventory findings
- The repository has a substantial history of feature branches (AI coach, exam controls, exam detail, error notebook, backup, notifications, study insights, curriculum, ranking, performance, and UI foundations).
- The branch `integration/complete-mainline` is 36 commits behind current main and has no commits ahead of main according to the compare result. Do not use it as a newer integration base.
- Useful work already consolidated into main includes the v3 AI coach, calibrated ranking engine, curriculum migration, UI foundation, performance fixes, study plan insights, and exam/statistics features. Inspect exact current files and tests before rebuilding any feature.
- The active branch inventory includes old and newer feature branches. No branch should be merged or deleted based only on its name.

## Confirmed product constraints and direction
- Turkish-only product UI; no web application.
- Android first, iOS architecture supported from the beginning.
- Keep `applicationId = com.deniz0706.ykstakip`.
- Target direction: Kotlin Multiplatform + Compose Multiplatform, with native Swift/SwiftUI adapters only where justified.
- New screen UI should be declarative; XML remains only for required platform configuration and legacy migration.
- Preserve all existing app features and user data during incremental migration.
- Shared domain code must be platform independent. SQLDelight and kotlinx.serialization are the planned persistence/serialization foundations.
- Do not embed API keys or signing material.

## Protected regression
- The current main branch has ranking regression tests in `YksRankingEstimatorTest.kt`.
- Its high-net TYT fixture uses 38 Turkish + 18 Social + 38 Mathematics + 20 Science = 114 net and expects a center rank in 200–300.
- The user's known calibration expectation is approximately rank 240 at 114 TYT net with diploma 88.2.
- No ranking engine or lookup-table changes are authorized without preserving and extending these regression fixtures.

## Work completed in this session
- Verified PR #51 and the `4.0/architecture-audit` branch.
- Compared `integration/complete-mainline` with main; confirmed it is stale (36 commits behind, zero ahead).
- Inspected the 4.0 architecture audit, Gradle configuration, MainActivity, ranking estimator, and ranking regression tests.
- Created `4.0/foundation` from `4.0/architecture-audit`.
- Added root engineering instructions in `AGENTS.md`.
- No application runtime code has been changed in this session.
- No build or test has been run from the GitHub connector; do not infer a green build from this audit.

## Immediate next steps
1. Complete branch/PR inventory and inspect unmerged branch patches for unique useful work.
2. Inspect current data repositories, SharedPreferences keys, backup/restore formats, photo storage, and migration code.
3. Create ranking characterization fixtures from the current passing behavior; do not alter estimator semantics.
4. Create an architecture decision record for KMP/Compose Multiplatform and document platform-boundary rules.
5. Establish a small, buildable shared-domain extraction slice before introducing a large dependency or moving screens.
6. Add CI checks for common tests and Android build; add iOS compile validation when a macOS runner is available.
7. Keep each vertical slice independently testable and preserve Android update/signing safety.

## Risks / limitations
- GitHub API inspection is not a substitute for a local checkout and full Gradle build.
- The installed APK signing certificate has not been verified against the CI preview certificate; update compatibility is not guaranteed.
- iOS compilation and signing require macOS/Xcode and appropriate Apple tooling.
