# YKS Takip 4.0 — Development Status

Updated: 2026-11-26

## Current integration point
- Working branch: `4.0/foundation`
- Branch created from: `4.0/architecture-audit`
- Base project main commit observed during audit: `93f7f044717d10b4c54014d6fbfc7a72bd9fbfeb`
- Architecture-audit branch is 15 commits ahead of main; its changes are limited to CI/build/signing documentation and the initial 4.0 architecture audit.
- Existing PR #51 remains open and targets main. Do not merge it automatically.

## Repository / branch audit
- `integration/complete-mainline` is 36 commits behind main and has zero commits ahead; it is stale.
- `feature/osym-ranking-engine` is 17 ahead / 115 behind and contains an older estimator/lookup rewrite. Current main already has a later implementation and high-net regression fixture; do not merge it wholesale.
- `feature/nvidia-ai-coach` and `v3.0/ui-foundation` have no unique commits ahead of main.
- `feat/full-app-backup` is 6 ahead / 6 behind; all five inspected implementation/UI/test files have identical blob SHAs on main, so no port is needed.
- `feat/ai-coach-image-input` is 9 ahead / 7 behind; all five inspected image-input/service/UI/test files have identical blob SHAs on main, so the feature is already present.
- `design/aurora-restoration-dashboard-v4` is 1 ahead / 32 behind with small theme/dashboard changes; review selectively, never use as a merge base.
- Additional findings are recorded in `docs/BRANCH-AUDIT.md`.

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

## Data migration findings
- Exam store: SharedPreferences `yks_takip_prefs`, key `exams_json`; legacy single-file backup is a JSON array.
- Study store: SharedPreferences `yks_takip_study`, key `study_days`, plus exact pre/post import rollback snapshots. Study backup has a versioned envelope and legacy array compatibility.
- Learning store: SharedPreferences `yks_learning_journal`, keys `outcomes` and `questions`, multiple curriculum migration flags and recovery snapshots.
- Learning question photos live in private `filesDir/review_photos`; ZIP archives contain photo bytes and references.
- Full backup is a versioned ZIP with manifest, exams JSON, study JSON, and learning ZIP. AI secrets are excluded.
- Important hazard: current repositories may return empty collections after JSON parse failure. The new importer must preserve raw source and report errors rather than treating malformed data as empty.
- Detailed initial inventory and migration protocol: `docs/DATA-MIGRATION-INVENTORY.md`.

## Work completed in this session
- Verified PR #51 and the `4.0/architecture-audit` branch.
- Compared historical branches against main and identified stale branches and two selective-review candidates.
- Inspected the 4.0 architecture audit, Gradle configuration, MainActivity, ranking estimator, and ranking regression tests.
- Inspected exam, study, learning, photo, and full-backup persistence paths.
- Created `4.0/foundation` from `4.0/architecture-audit`.
- Added root engineering instructions in `AGENTS.md`.
- Recorded KMP/Compose Multiplatform direction in `docs/adr/0001-kotlin-multiplatform-compose.md`.
- Added `docs/DATA-MIGRATION-INVENTORY.md` with observed stores, schemas, hazards, and a safe migration protocol.
- Added `docs/BRANCH-AUDIT.md` with selected branch ancestry and integration decisions.
- Compared the full-backup and AI image-input branch files against main; their inspected implementation, UI, and test files are byte-identical to main, avoiding duplicate integration.
- Added the first real Kotlin Multiplatform domain module (`:core:domain`) with JVM + iOS targets; Android app consumes its JVM variant.
- Extracted the canonical correct-minus-wrong/4 net calculation into `commonMain` and added shared common tests for quarter-net and rounding behavior.
- Moved exam answer-count and TYT/AYT total-duration validation rules into `commonMain`; the Android adapter preserves the existing Turkish validation messages and public API. Added common tests for negative answers, subject limits, combined limits, and 165/180-minute boundaries.
- Extracted focus countdown/stopwatch elapsed-time arithmetic into `commonMain`; the existing Android API delegates to the shared implementation. Added shared tests for pause/clock rollback, configured countdowns, 24-hour cap, and target clamping.
- Moved the `StudyDay` value model and daily study-time addition rules into `commonMain`, retaining the original Android package through a Kotlin typealias and adapter. Shared tests cover data preservation, invalid input, date mismatch, and the 24-hour limit.
- Moved the `SubjectResult` exam value model and derived blank/net/time calculations into `commonMain`; Android JSON encoding/decoding remains in a compatibility adapter so the persisted exam JSON contract is unchanged. Added shared model tests.
- Updated the 4.0 APK workflow to execute the shared domain JVM test task before Android unit tests and APK builds.
- Added Android adapter regression tests to ensure Turkish validation messages and TYT/AYT time boundaries remain unchanged.
- Recorded the incremental migration strategy and explicit iOS validation limits in ADR 0002.
- Ranking estimator, lookup tables, ranking fixtures, persistence formats, signing configuration, and application ID remain untouched.
- No build or test has been run from the GitHub connector; do not infer a green build from this audit.

## Immediate next steps
1. Add the next pure shared-domain slice (exam validation and time/net primitives) with characterization tests before moving repositories.
2. Introduce Compose Multiplatform UI only after shared domain and persistence boundaries are stable; do not rewrite screens in one pass.
3. Complete the preference/key and model/backup schema census, including settings, timers, AI cache, and photo ownership.
4. Add CI execution for `:core:domain:allTests` and Android build; add iOS compile validation on a macOS runner.
5. Build a lossless legacy importer behind existing repository interfaces before changing storage.
6. Keep each vertical slice independently testable and preserve Android update/signing safety.

## Risks / limitations
- GitHub API inspection is not a substitute for a local checkout and full Gradle build.
- The installed APK signing certificate has not been verified against the CI preview certificate; update compatibility is not guaranteed.
- iOS compilation and signing require macOS/Xcode and Apple tooling.
