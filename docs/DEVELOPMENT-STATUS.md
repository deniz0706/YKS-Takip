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
- `feat/full-app-backup` is 6 ahead / 6 behind, but main already contains a full backup implementation; preserve main and compare behavior before considering any port.
- `feat/ai-coach-image-input` is 9 ahead / 7 behind and has a potentially unmerged image-input capability; inspect its exact diff and provider payload behavior before selective integration.
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
- No application runtime code has been changed in this session.
- No build or test has been run from the GitHub connector; do not infer a green build from this audit.

## Immediate next steps
1. Inspect the unmerged AI image-input branch for capabilities absent from main and provider payload safety.
2. Inspect the Aurora dashboard branch for UI improvements compatible with the current design direction.
3. Complete the preference/key and model/backup schema census, including settings, timers, AI cache, and photo ownership.
4. Establish a small, buildable shared-domain extraction slice before introducing a large dependency or moving screens.
5. Add CI checks for common tests and Android build; add iOS compile validation when a macOS runner is available.
6. Keep each vertical slice independently testable and preserve Android update/signing safety.

## Risks / limitations
- GitHub API inspection is not a substitute for a local checkout and full Gradle build.
- The installed APK signing certificate has not been verified against the CI preview certificate; update compatibility is not guaranteed.
- iOS compilation and signing require macOS/Xcode and Apple tooling.
