# Legacy Data & Backup Inventory — 4.0

Updated: 2026-11-26
Scope: source inspection of the current `main` baseline (commit `93f7f044717d10b4c54014d6fbfc7a72bd9fbfeb`). This is an initial verified inventory, not a complete key census.

## Confirmed persistence stores

| Store / file | Keys / fields observed | Current representation |
|---|---|---|
| SharedPreferences `yks_takip_prefs` | `exams_json` | JSON array of exams |
| SharedPreferences `yks_takip_study` | `study_days`, `study_before_import`, `study_after_import` | Study JSON array plus rollback snapshots |
| SharedPreferences `yks_learning_journal` | `outcomes`, `questions`, migration flags and pre-migration snapshots | JSON arrays; flags/snapshots retained across multiple curriculum migrations |
| Private files `filesDir/review_photos` | photo filenames referenced by question cards | JPEG/PNG/WebP image files |
| Additional stores | Theme, settings, AI secret/cache, and other feature preferences exist | Requires full census before schema design |

## Exam records

- `ExamRepository` uses preference file `yks_takip_prefs` and key `exams_json`.
- `Exam` JSON fields: `id`, `title`, `date`, `type`, `aytField`, `notes`, `publisher`, `subjects`.
- `SubjectResult` fields: `subject`, `questionCount`, `correct`, `wrong`, `timeMinutes`, `weakTopics`.
- Derived values include blank count, net (correct − wrong/4), total net, and minutes per question. Derived values should not be persisted as independent truth unless an explicit compatibility requirement exists.
- Legacy single-file exam backup is a JSON array, not the study-backup envelope.

## Study records

- `StudyRepository` uses preference file `yks_takip_study`.
- Key `study_days` contains date, questionCount, studyMinutes, and estimated.
- `study_before_import` and `study_after_import` are exact rollback snapshots. Undo is permitted only if current data still exactly equals the recorded post-import snapshot.
- `StudyBackupManager` export envelope: schema `yks_takip_study_backup`, version 1, top-level estimated marker, and days array. Parser also accepts the older bare JSON array format.
- Study imports merge by date and preserve existing local values unless overwrite is explicitly selected.

## Learning journal and photos

- `LearningRepository` uses preference file `yks_learning_journal`.
- Primary keys: `outcomes` and `questions`; the store also contains multiple one-time migration flags and raw pre-migration snapshots. Observed flags include `learning_topics_v1`, `learning_meb_units_v1`, `learning_meb_units_v2`, `learning_kitapsec_v1`, `learning_science_ayt_separation_v1`, and `learning_supplied_curriculum_v1`.
- Learning outcomes and question cards are JSON arrays. Question cards reference outcomes by stable IDs and photos by filenames.
- Photos live in private `filesDir/review_photos`. The archive importer validates filenames, image decodability, sizes, references, and ID uniqueness. Existing photo files must never be overwritten on name collision.
- Curriculum migrations preserve/remap outcome IDs and question links; old raw snapshots are retained for recovery.

## Full backup format

- `FullBackupManager` writes a ZIP with exactly four entries: `manifest.json`, `exams.json`, `study.json`, and `learning.zip`.
- Manifest schema: `yks_takip_full_backup`, version 1, counts for exams/study days/learning outcomes/questions/photos, and `secretsIncluded=false`.
- Full restore is staged and validated before merge. Existing local exams and study days win on conflict; learning data is merged by stable IDs; photos are staged and collision-renamed.
- The full archive imposes explicit per-section and total size limits. Preserve those safety checks or replace them with equally strict tested controls.
- AI credentials are deliberately excluded from backups. Never include them in SQLDelight exports or cloud sync.

## Migration hazards discovered

1. `ExamRepository.getAllExams()` catches any parse exception and returns an empty list. This is dangerous as a migration source: malformed JSON must be reported/quarantined, never silently interpreted as an empty database and then overwritten.
2. `StudyRepository.getAll()` also returns an empty list on parse failure. Migration must preserve the original raw value and surface a recoverable error.
3. `LearningRepository` runs a chain of one-time migrations in its initializer. A new importer must not instantiate or mutate legacy repositories casually; capture raw preferences and files before invoking code with migration side effects.
4. Existing rollback snapshots are user-data recovery assets. Do not clear or repurpose them during schema conversion.
5. Exam JSON, study JSON, learning ZIP, and full ZIP are separate public compatibility formats. They must all remain readable; do not normalize them into one undocumented format.
6. Photo references are part of question data. A migration that copies question rows but not photo bytes and filename remapping is lossy.
7. `FullBackupManager.merge` coordinates exam, study, and learning commits with compensating rollback. SQL migration must provide an explicit transaction/recovery story for the whole dataset, not only one table at a time.

## Required migration protocol

1. Read and checksum raw preference files and enumerate every key before any write.
2. Snapshot the entire legacy state and private photo directory to a recoverable staging area.
3. Parse each source format independently with strict validation; report per-section counts and errors.
4. Import into a versioned staging database in a single transaction where possible.
5. Verify row counts, stable IDs, totals/nets, completion flags, question-to-outcome links, photo checksums, and backup round-trips.
6. Keep the legacy source untouched until verification passes and a durable migration-complete marker is committed.
7. Make migration idempotent. A second launch must not duplicate records or re-run curriculum transformations.
8. On any error, keep the old stores authoritative and expose a retry/recovery path. Never silently replace malformed source data with empty collections.

## Still to inventory

- Every preference file and every key, including theme/settings/AI/cache/timer/widget state.
- Exact `LearningOutcome` and `QuestionCard` JSON schema and all migration snapshot keys.
- Image filename ownership rules and any orphan-photo cleanup behavior.
- Other exports/imports and any Android backup/restore rules.
- Current tests covering corrupt files, partial imports, duplicate IDs, rollback, and photo restoration.
