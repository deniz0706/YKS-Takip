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
- Moved the `Exam` aggregate, exam types, AYT fields, and TYT/AYT subject configurations into `commonMain`. Existing Android model package names remain source-compatible aliases/adapters; the existing JSON field names and parsing defaults remain in the Android adapter.
- Added Android-side JSON adapter regression tests for round-trip exam data and legacy records with omitted optional fields; updated repository and backup boundaries to import the adapter extensions explicitly.
- Reviewed the Aurora branch against the current UI implementation; rejected its stale AI coach snapshot because it predates current image support and richer conversation behavior. Only cosmetic dashboard typography changes remained.
- Updated the 4.0 APK workflow to execute the shared domain JVM test task before Android unit tests and APK builds.
- Added Android adapter regression tests to ensure Turkish validation messages and TYT/AYT time boundaries remain unchanged.
- Recorded the incremental migration strategy and explicit iOS validation limits in ADR 0002.
- Ranking estimator, lookup tables, ranking fixtures, persistence formats, signing configuration, and application ID remain untouched.
- No build or test has been run from the GitHub connector. The latest commit has no reported CI status checks yet; do not infer a green build from the source-level review.

## Latest continuation work
- Added a macOS CI workflow at `.github/workflows/verify-kotlin-multiplatform.yml` to run shared JVM tests and compile the iOS device and simulator targets on pushes to `4.0/**` and pull requests to main.
- Hardened `SubjectResult.minutesPerQuestion`: malformed/legacy records with zero questions now return null rather than attempting division by zero. Added a common test for this edge case.
- Expanded the data inventory after inspecting theme, settings, AI cache/secret/memory, weekly plan, focus timer, and exam timer stores. Recorded write-on-read migration side effects and Android Keystore portability limits.
- Latest documentation/code commit in this continuation: `a5390dea6243e1f9497972c36f1aef698e73c51e`.
- CI status for that exact commit: no workflow runs were returned. No build/test success is claimed.

## Latest continuation work — shared serialization
- Added Kotlin Serialization plugin (aligned to Kotlin 2.0.21) and `kotlinx-serialization-json:1.7.3` to the shared domain module.
- Marked shared exam, subject, study-day, exam-type, AYT-field, subject-rule, and validation-result models serializable. No Android JSON dependency was introduced into `commonMain`.
- Added common tests for nested exam round trips with Turkish text, legacy-shaped input with omitted optional fields, polymorphic validation results, and study-day/AYT-field serialization.
- Kept the existing Android exam JSON adapter and backup formats unchanged. This is a new shared serialization capability, not a data migration.
- Added ADR 0003 to document this boundary and compatibility policy.
- Latest source commit: `d05f07f8f8b6c84a5a646e1383118e7db0ace945`. No Gradle build has been run from the GitHub connector; CI must validate plugin resolution and compilation.

## Latest continuation work — shared serialization and database foundation
- Added Kotlin Serialization 2.0.21 plugin and kotlinx-serialization-json 1.7.3 to `:core:domain`; shared exam/subject/study models and validation results are serializable from common code.
- Added common serialization tests for Turkish text, nested subject records, optional legacy fields, polymorphic validation results, study-day values, and AYT enum values.
- Added isolated `:core:database` Kotlin Multiplatform module using SQLDelight 2.0.2 with JVM/iOS targets.
- Added initial normalized schema for exams, ordered subject results, and daily study records, plus basic read/write/delete queries.
- Added ADR 0003 (serialization boundary) and ADR 0004 (SQLDelight foundation and migration gate).
- Extended macOS KMP CI to compile the database module for JVM and iOS; extended Android APK CI to compile its JVM target.
- The SQLDelight module is deliberately not connected to production repositories. No legacy SharedPreferences or photo data has been touched; no migration has been run.
- Latest schema/CI/docs commits: `ad6f1e91e664981f3e4e04cfbcfadafc32351f2e`. GitHub connector cannot execute Gradle; no green build is claimed.

## Latest continuation work — platform database drivers
- Converted `:core:database` from a generic JVM/iOS module into an Android library + iOS multiplatform module. The Android app now consumes the Android variant; the generic JVM target is no longer used as a stand-in for Android.
- Added Android SQLDelight driver factory using app-private `yks_takip_v4.db` and iOS NativeSqliteDriver factory with the same database filename in the platform sandbox.
- Added the SQLDelight Android and native driver dependencies and kept the runtime in commonMain.
- Wired the database module into the Android app dependency graph. It is compiled as part of the app but is not yet used by the production repositories.
- Added durable migration checkpoint schema with source fingerprint, schema version, state, start/completion timestamps, and error details. This is bookkeeping only; it does not mark any migration complete.
- Updated Android and macOS CI tasks to compile Android/iOS database targets.
- No legacy preferences, backup files, photos, or signing material were read or modified by these changes. No importer has run.
- No Gradle execution is available through the GitHub connector; build status remains unverified.

## Latest continuation work — transactional data stores
- Added shared `SqlDelightExamStore` and `SqlDelightStudyDayStore` adapters using the common domain models.
- Exam save now updates the parent and replaces ordered subject rows inside one SQLDelight transaction. Full replacement rejects duplicate IDs before deleting anything, then clears child rows and parents transactionally. Deletion explicitly removes children before parents rather than relying on driver foreign-key defaults.
- Exam parent writes use SQLite `ON CONFLICT DO UPDATE`; avoided `INSERT OR REPLACE`, which can delete the old parent and cascade-delete child rows.
- Preserved the legacy ascending chronological order for exam history.
- Added an in-memory SQLite JVM test suite covering aggregate replacement, duplicate-ID protection, deletion, and study-day upsert.
- Added a JVM target and SQLite test driver to the database module; both CI workflows now run the database JVM tests.
- These adapters are still isolated: app production repositories continue using legacy SharedPreferences. No data migration or legacy writes have been enabled.
- Build/test execution is still unverified from this GitHub-only session; the new Gradle tasks must run in Actions before any production wiring.

## Immediate next steps
1. Add SQLDelight platform driver factories and database tests, then implement a staged, lossless importer behind existing repository interfaces.
2. Introduce Compose Multiplatform UI only after shared domain and persistence boundaries are stable; do not rewrite screens in one pass.
3. Complete the preference/key and model/backup schema census, including settings, timers, AI cache, and photo ownership.
4. Add CI execution for `:core:domain:allTests` and Android build; add iOS compile validation on a macOS runner.
5. Build a lossless legacy importer behind existing repository interfaces before changing storage.
6. Keep each vertical slice independently testable and preserve Android update/signing safety.

## Risks / limitations
- GitHub API inspection is not a substitute for a local checkout and full Gradle build.
- The installed APK signing certificate has not been verified against the CI preview certificate; update compatibility is not guaranteed.
- iOS compilation and signing require macOS/Xcode and Apple tooling.


## Current integration checkpoint — 2026-11-26

- Active integration branch: `4.0/foundation`.
- Latest known pre-checkpoint source commit: `8728e8616fe0e5959ed33ea4fee172496360708d`; the architecture decision and this status update are being recorded as follow-up documentation commits.
- `main` is at `2205ada64597c955ab1e546d2dc9b859a07f3927` after the isolated documentation-only PR #52 merge. The foundation branch compares as 109 commits ahead and 1 behind; the one-behind difference is the separate PR #52 merge commit. No reset, force-push, or automatic merge was performed.
- PR #51 (`4.0/architecture-audit` -> `main`) remains open and draft. It contains 15 commits focused on build/version/signing safeguards and the initial architecture audit. Its changes overlap the foundation branch's ancestry; do not merge it independently or close it without an explicit integration review.
- The dedicated KMP CI run `45869883815` passed on foundation commit `8728e861`: shared JVM tests, database JVM tests, Android database compilation, iOS arm64 and iOS simulator arm64 domain compilation, and iOS arm64/simulator/x64 database compilation all passed.
- The dedicated APK CI run `45869883822` passed on the same foundation commit: APK build, in-place update identity/version verification, signing fingerprint report, and artifact upload passed.
- Main's post-PR #52 Android build run `45870601053` was still in progress at the last check. The Android verification workflow on the docs branch passed; this does not replace the foundation branch's own validation.
- These CI results prove Kotlin target compilation and Android APK generation, not a complete iOS app build, Xcode archive, Apple code signing, simulator launch, or App Store readiness.
- Added `docs/adr/ADR-001-platform-architecture.md` to formalize the already selected KMP + Compose Multiplatform direction, module boundaries, offline-first policy, backend deferral, platform adapters, migration gate, and explicit non-goals.
- The shared SQLDelight stores remain isolated from production repositories. No migration was run; legacy SharedPreferences/JSON, photos, timers, backup snapshots, and AI credentials remain untouched.
- The ranking engine remains unchanged. No ranking engine refactor or lookup-table modification is included in this checkpoint.

## Next engineering slice

1. Audit `commonMain` imports and module dependency direction; enforce the no-`android.*` / no-`java.*` / no-`org.json` rule with CI checks.
2. Inspect the real legacy backup/import code paths and add source-derived, privacy-safe fixtures before implementing `LegacyBackupImporter`.
3. Add characterization tests around the current ranking estimator/lookup/calculator using existing regression vectors; do not change the calculation implementation.
4. Design repository interfaces and a migration coordinator without switching production storage.
5. Start the first Compose Multiplatform vertical slice only when it can use real domain/repository/ranking code rather than static demo data.

## Continuation update — architecture decision and CI verification
- Rechecked repository state after documentation-only PR #52 merged. main now points to 2205ada64597c955ab1e546d2dc9b859a07f3927; 4.0/foundation remains the integration branch and is 109 commits ahead / 1 commit behind main. The one-behind commit is the documentation-only main merge; no branch was reset or force-pushed.
- PR #51 (4.0/architecture-audit to main) remains open and draft. It is not merged automatically; its 15-commit branch overlaps the foundation ancestry and must be closed or retargeted only after an explicit integration decision.
- Added docs/adr/ADR-001-platform-architecture.md as the requested platform architecture decision. It formalizes KMP + Compose Multiplatform, Android/iOS adapter boundaries, SQLDelight/Kotlin Serialization, offline-first local authority, future modular-monolith service boundaries, replaceable design tokens, XML exit policy, and explicit validation gates.
- Verified the latest completed foundation CI runs at commit 8728e8616fe0e5959ed33ea4fee172496360708d: Android APK workflow run 45869883822 succeeded, including APK build, in-place update identity/version check, signing fingerprint report, and artifact upload; KMP workflow run 45869883815 succeeded, including shared JVM tests and iOS device/simulator target compilation on macOS.
- These CI results validate the foundation commit only. They do not establish a signed iOS app, Apple distribution readiness, or a working Compose Multiplatform UI; no Compose vertical slice or production SQLDelight migration is complete yet.
- Main's post-merge APK run 45870601053 was still in progress at the last status check; do not report its result until rechecked.
- Ranking estimator/lookup/calculator, user persistence, backup formats, application ID, and signing configuration were not changed by this continuation.

## Master prompt preservation
- The complete YKS Takip 4.0 master development specification is preserved in `docs/MASTER-DEVELOPMENT-PROMPT.md`.
- Commit: `2e1609eaa0ad9c9d49b1858446a60eb3e9620d0c` on `4.0/foundation`.
- This file is the durable project-level source of truth. At the start of every continuation, read it together with this status file and verify the live Git/CI state. It is not a new scope change; it records the previously agreed master instructions.


## CI failure investigation — 2026-11-26
- Latest APK workflow at commit `6474287db577c4c00b117105f6351af65d6cf1e4` (run `45871452264`) failed at `:app:compileDebugUnitTestKotlin`, not in production app compilation.
- Root cause: `YksRankingEstimatorTest.kt` declared a Kotlin test function beginning with the numeric token `114Net...`, which is invalid Kotlin syntax. Production ranking implementation and expected calibration assertions are unchanged.
- Corrective change prepared: rename the test to `tyt114NetRegressionRemainsNearCalibratedRank`. The correction is not considered verified until a new APK CI run passes.
- KMP workflow run `45871452275` was still running at the time of this checkpoint; its result must be checked before another push to avoid cancelling it.


## CI dependency-resolution incident — 2026-11-26
- APK run `45871984455` reached Gradle but stopped during root build configuration because Gradle could not resolve the SQLDelight plugin marker `app.cash.sqldelight:app.cash.sqldelight.gradle.plugin:2.0.2`. It failed before Kotlin compilation or tests. The same plugin declaration/repositories were unchanged from earlier successful foundation builds; do not alter plugin coordinates without evidence of a configuration defect. Check the paired macOS KMP run and retry after determining whether this was a transient repository-resolution failure.
- The corrected ranking test name is committed in `49ba58598f2dc9f3ede08db3b29d68f8df2842f6`; its new APK build remains unverified because dependency resolution failed before compilation.

## Legacy JSON write-protection slice — staged for next commit
- The current exam and study repositories convert malformed legacy JSON into an empty list. Without a guard, a later save/import could overwrite the unreadable original with that empty fallback.
- A focused guard is staged for both repositories: parse failures mark the store unreadable; exam saves/replacements and study saves/deletes/imports/undo are blocked before any SharedPreferences write. Read paths keep their existing fallback contract, and the original raw preference remains untouched.
- The repositories expose `hasUnreadableLegacyData()` for a future recovery UI. The guard and its state transitions have focused JVM tests.
- This slice is staged for integration only after the currently running KMP validation is checked; no production data has been read, migrated, or modified by CI.


## Verified foundation CI checkpoint — 2026-11-26
- Commit `d3999f7d229bd07b8a635ba15b943ddcf33958d1` passed Android APK workflow run `45872229685`: APK build, in-place update identity/version verification, signing fingerprint report, and debug/unsigned-release artifact uploads succeeded. Preview release publication was skipped by workflow conditions.
- The same commit passed Kotlin Multiplatform workflow run `45872229533`: commonMain platform-boundary enforcement, shared domain/database JVM tests, and iOS device/simulator target compilation succeeded.
- These results validate the existing foundation and data write-protection slice. They do not validate the next Compose Multiplatform toolchain/module work or production database migration.

## Backup export safety follow-up — integrated
- During review, found that standalone study JSON export and full ZIP export read guarded repositories but did not check their unreadable flags. Both export paths now stop before opening/writing the destination if their source data is unreadable.
- The original SharedPreferences values remain untouched and backup schemas are unchanged. This prevents a corrupt legacy store from being silently exported as an apparently valid empty backup.
- This follow-up is committed separately and requires its own APK/KMP CI validation.


## Compose Multiplatform shared UI slice — branch in progress
- Working branch: `4.0/compose-multiplatform-shell`, based on the verified foundation checkpoint `4521b4ef6f66162c700a985a2f34849528307f1a`.
- Toolchain baseline selected for the first CMP slice: Kotlin/Kotlin Multiplatform/Compose compiler plugin `2.2.21`, Compose Multiplatform Gradle plugin `1.9.3`, kotlinx.serialization `1.9.0`; existing AGP `8.7.3` and CI Gradle `8.11.1` retained. SQLDelight remains `2.0.2` pending compatibility validation rather than being upgraded speculatively.
- Added a real `:composeApp` KMP module with Android, JVM test, iOS device, iOS simulator, and iOS x64 targets. It exposes a static `YksTakipShared` framework and an iOS `MainViewController` entry point.
- The Android home screen now uses the shared Compose Multiplatform exam-trend/goal component for its main net evidence card. The remaining Android dashboard sections and workflows are retained; this is an incremental migration, not a replacement with a static demo.
- The iOS entry point currently proves the shared UI shell only. iOS persistence, navigation, ranking, app project/signing, and release are not complete and must not be described as shipped.
- Added a common test for Turkish decimal formatting and extended CI to test the shared UI module, compile its Android target and compile iOS device/simulator targets. Ranking estimator/lookup/calculator and legacy persistence formats remain unchanged.
- This branch has not yet been merged into `4.0/foundation`; CI must pass before integration.


## Latest continuation — Compose dashboard contract
- Replaced the static Compose Multiplatform placeholder with a data-driven dashboard surface and explicit `DashboardUiState` input plus platform navigation callbacks.
- Added TYT/AYT switching, latest net/change/goal/trend presentation, empty-state messaging, add-exam action, history and study navigation actions, and daily question/exam countdown context.
- Added validation for impossible dashboard state (negative counts/goals/days and invalid net trend values) with common tests. Platform repositories must supply persisted values; iOS is not claimed to be connected to Android storage.
- KMP verification run `45873501403` passed for the preceding shell commit. Android APK run `45873501372` was still running at the time of this update; this new commit will trigger fresh verification.
- Active branch remains `4.0/compose-multiplatform-shell`; no merge to foundation or main has been performed.


## Latest continuation — shared exam-entry domain rules
- Added common Kotlin `ExamEntryRules` to validate and construct an exam consistently for Android and iOS without coupling domain logic to platform storage or UI.
- Validation now checks required TYT/selected AYT subject sets, duplicate/missing subjects, AYT field requirements, correct/wrong limits, negative time, and total duration (TYT 165 min / AYT 180 min). Creation trims metadata and refuses invalid drafts.
- Extended the shared validation result model and added common tests for valid TYT construction, blanks/net calculation, incomplete/duplicate subjects, AYT field requirements, duration limits, and invalid metadata/answer counts.
- Existing Android `ExamRepository` and legacy JSON format were not changed; this is a shared domain layer step, not yet a production repository migration.
- Branch: `4.0/compose-multiplatform-shell`; pending verification on commit `18c710850ad3bdbcef90273b59107096cb94bfe3` remains separate from this new change.


## Latest continuation — shared exam-entry CI repair and Android integration
- Current feature branch: `4.0/compose-multiplatform-shell`; checkpoint before this repair: `d57791370495621a163823ac4c25e6ffd1191540`.
- Both latest CI workflows failed at `:core:domain:compileKotlinJvm`, not at lint. The concrete cause was a stale `SubjectRule` type reference: the shared subject catalogue had already standardized on serializable `SubjectConfig`.
- Replaced the missing type with the existing common `SubjectConfig` in shared validation and the Android message adapter; updated the common validation test fixture accordingly.
- Connected the existing Android `NewExamScreenV2` save path to `ExamEntryRules.validate/create`. The screen now uses the common validator as its final gate and passes the validated shared `Exam` aggregate to the existing repository callback. The SharedPreferences JSON adapter and persistence contract are unchanged.
- No ranking files, signing material, application ID, existing exam JSON format, or production database writes were changed.
- CI for the repair commit is pending; no build or test success is claimed until the new workflows complete.


## Shared domain analytics hardening — 2026-11-26
- Opened isolated branch `4.0/shared-domain-analytics` from `4.0/compose-multiplatform-shell` checkpoint `6b95aec27eddac18e1362f57bc4f659675695562`; it is intentionally separate so the active APK/KMP validation on the parent branch is not cancelled.
- Added `ExamAnalytics` and `ExamTrendSummary` to commonMain. Latest/previous net, delta, average, best, rolling five-exam averages, and chronological sparkline values now have one platform-independent implementation.
- Connected the existing Android HomeScreenV2 performance metrics and shared Compose Multiplatform trend card to the common analytics contract. Existing UI sections and navigation remain in place.
- Hardened exam validation to reject subject records whose stored question count does not match the official TYT/selected AYT subject configuration.
- Added common tests for empty history, date ordering, trend limits, latest/change/average/best values, complete five-exam comparison windows, AYT subject sets, negative time, combined answer limits, and question-count mismatch.
- This branch is not merged. Its own CI must pass before opening an integration PR to the CMP shell branch.


## Shared dashboard data bridge — 2026-11-26
- Extended the common Compose dashboard contract with `DashboardUiState.fromExams`, which derives TYT/AYT latest net, previous net, and the latest ten chronological attempts from the shared domain analytics engine.
- Added a direct commonMain dependency from `:composeApp` to `:core:domain`; platform repositories can now provide the same shared Exam aggregates and platform-owned settings to the common dashboard without duplicating score/trend calculations.
- Added common tests proving real TYT/AYT records and goals/countdown/study counters map into the shared UI state.
- This remains a data-contract bridge, not a claim that iOS persistence or the complete app navigation has shipped. Android's current HomeScreenV2 remains the production home while migration proceeds incrementally.
- Updated branch: `4.0/shared-domain-analytics`; integration PR #53 remains draft and awaits both Android and KMP/iOS-target CI workflows.
