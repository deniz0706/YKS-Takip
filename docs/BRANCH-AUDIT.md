# Branch Audit — 4.0 Foundation

Updated: 2026-11-26
Comparison base: `main` at `93f7f044717d10b4c54014d6fbfc7a72bd9fbfeb`.

This is a targeted audit of branches with likely product value. Ahead/behind counts are Git ancestry counts, not a recommendation to merge.

## Current 4.0 branches
- `4.0/architecture-audit`: 15 commits ahead of main; current PR #51. Changes are limited to build/version/signing workflow and architecture audit documentation.
- `4.0/foundation`: created from `4.0/architecture-audit`; now contains the first Kotlin Multiplatform shared-domain module, migrated exam/study value models and pure rules, Android compatibility adapters, and common/Android regression tests in addition to the engineering guardrails and migration inventory.

## Findings

| Branch | Relation to main | Unique patch signal | Decision |
|---|---:|---|---|
| `integration/complete-mainline` | 0 ahead / 36 behind | No unique commits | Stale; do not integrate |
| `feature/osym-ranking-engine` | 17 ahead / 115 behind | Historical ranking lookup/estimator rewrite and regression tests | Do not cherry-pick wholesale; current main has a later ranking implementation and live regression fixtures |
| `feature/nvidia-ai-coach` | 0 ahead / 63 behind | No unique commits | Already superseded by main |
| `design/aurora-restoration-dashboard-v4` | 1 ahead / 32 behind | Small theme/dashboard/AI-coach refinements across 6 files; reviewed against current foundation | Do not merge: AI coach variant is stale and would discard current image-attachment and richer conversation behavior; remaining dashboard typography edits are cosmetic and too small to justify importing the old branch |
| `v3.0/ui-foundation` | 0 ahead / 47 behind | No unique commits | Superseded by main |
| `feat/full-app-backup` | 6 ahead / 6 behind | Full backup ZIP implementation, study backup, settings UI, tests | Main already contains a full backup implementation; compare exact behavior before considering any patch |
| `feat/ai-coach-image-input` | 9 ahead / 7 behind | AI image encoder, capability checks, service and UI changes, tests | Unmerged feature candidate; inspect exact diff and provider/model support before selective port |
| `fix/curriculum-learning-2026` | 0 ahead / 107 behind | No unique commits | Superseded by main |
| `perf/async-dashboard-data` | 2 ahead / 110 behind | Small historical workflow/screen patch | Main has later performance integration; do not cherry-pick without need |

## Integration policy
- Do not merge old branches wholesale merely because they have commits ahead; they are generally based on old ancestors.
- For each candidate, inspect file-level diffs against current main, preserve later main behavior, and port only a demonstrably missing capability.
- Never change ranking behavior while resolving the historical ranking branch.
- Preserve the existing full-backup implementation already present in main; do not replace it with an older branch snapshot.
- Review AI image input as a product feature separately from the external coding-agent provider. The NVIDIA coding key must not be embedded in the Android app.

## Completed candidate reviews
- Aurora was inspected file-by-file against `4.0/foundation`. Its AI coach version predates the current image-attachment/conversation implementation and must not be copied over it. Remaining dashboard changes are limited to typography/spacing and are not worth importing from a branch 62 commits behind this foundation.
- Full-backup and AI image-input candidate files previously inspected are already present byte-for-byte in main; no duplicate port is needed.

## Next candidate reviews
1. Complete the remaining preference/key and model/backup schema census before introducing shared persistence.
2. Review current UI components directly and design the Compose Multiplatform screen migration around the existing product behavior; do not revive stale screen snapshots.
