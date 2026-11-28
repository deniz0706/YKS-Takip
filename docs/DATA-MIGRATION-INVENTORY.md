# Legacy Data & Backup Inventory — 4.0

Updated: 2026-11-26
Scope: source inspection of the current `main` baseline (commit `93f7f044717d10b4c54014d6fbfc7a72bd9fbfeb`). This is a verified partial census; continue searching for less frequently used feature stores before designing the final database schema.

## Confirmed persistence stores

| Store / file | Keys / fields observed | Current representation |
|---|---|---|
| SharedPreferences `yks_takip_prefs` | `exams_json`, `theme_mode`, `theme_palette_v2`, `target_net_tyt`, `target_net_ayt`, `yks_exam_date_millis`, `daily_question_goal`, `obp`, `learning_ayt_field`, `ai_base_url`, `ai_model` | Exam JSON plus app preferences |
| SharedPreferences `yks_takip_study` | `study_days`, `study_before_import`, `study_after_import` | Study JSON array plus rollback snapshots |
| SharedPreferences `yks_learning_journal` | `outcomes`, `questions`, migration flags and pre-migration snapshots | JSON arrays; flags/snapshots retained across multiple curriculum migrations |
| SharedPreferences `yks_takip_study_plan` | `study_plan_items` | Weekly plan JSON |
| SharedPreferences `yks_focus_session` | `duration`, `stopwatch_mode`, `accumulated`, `started_at`, `carry_date`, `carry_ms` | Active/persisted timer state and sub-minute carry |
| SharedPreferences `yks_exam_session_v1` | `exam_type`, `duration`, `splits`, legacy timer fields, `shared_timer_migrated` | Exam-mode metadata/splits and one-time migration marker |
| SharedPreferences `ai_coach_cache` | `advice`, `advice_date`, `last_attempt_date`, `last_exam_id` | Latest AI advice and daily generation gate |
| SharedPreferences `ai_secret_store` | `provider_api_key` | AES-GCM ciphertext; key is non-exportable Android Keystore alias `yks_takip_ai_secret_v1` |
| SharedPreferences `ai_coach_long_term_memory` | `memories_v1` | JSON array of explicit AI memory records |
| Private files `filesDir/review_photos` | photo filenames referenced by question cards | JPEG/PNG/WebP image files |

## App preferences and theme

- `yks_takip_prefs` is shared by exam records, OBP, target nets, exam date, daily question goal, learning AYT field, AI endpoint/model, and theme state. Do not migrate it as one opaque exam blob.
- Theme keys: `theme_mode` and `theme_palette_v2`. The theme reader performs a one-time migration: legacy `dark` becomes `aurora`, then the v2 marker is set. This is a write-on-read side effect; capture raw preferences before invoking it during migration.
- AI endpoint/model are user preferences, not credentials. Preserve values, but do not assume an old model identifier remains available at the provider.
- The actual API key is stored separately as AES-GCM ciphertext in `ai_secret_store`, with its decryption key in Android Keystore. It is intentionally not portable via ordinary backup. Do not export ciphertext as if it were a usable credential, and never log plaintext.

## Exam records

- `ExamRepository` uses preference file `yks_takip_prefs` and key `exams_json`.
- `Exam` JSON fields: `id`, `title`, `date`, `type`, `aytField`, `notes`, `publisher`, `subjects`.
- `SubjectResult` fields: `subject`, `questionCount`, `correct`, `wrong`, `timeMinutes`, `weakTopics`.
- Derived values include blank count, net (correct − wrong/4), total net, and minutes per question. Derived values should not be persisted as independent truth unless an explicit compatibility requirement exists.
- Legacy single-file exam backup is a JSON array, not the study-backup envelope.

## Study records, plans, and timers

- `StudyRepository` uses preference file `yks_takip_study`. Key `study_days` contains date, questionCount, studyMinutes, and estimated.
- `study_before_import` and `study_after_import` are exact rollback snapshots. Undo is permitted only if current data still exactly equals the recorded post-import snapshot.
- `StudyBackupManager` export envelope: schema `yks_takip_study_backup`, version 1, top-level estimated marker, and days array. Parser also accepts the older bare JSON array format.
- Study imports merge by date and preserve existing local values unless overwrite is explicitly selected.
- Weekly plan store: `yks_takip_study_plan` / `study_plan_items`. Records include id, dayOfWeek, startTime, endTime, subject, topic, completed, and completedWeek. Legacy permanent completed flags are converted to a week-scoped marker on read and persisted.
- Focus timer state is in `yks_focus_session`; exam timer metadata is in `yks_exam_session_v1`. The exam timer migrates legacy elapsed/start/duration values once into the shared focus timer. Sub-minute elapsed milliseconds are carried by date so saved study minutes are not rounded up or discarded.
- Timer state is user work in progress. Preserve running/paused state and the same-day carry fields; do not reset timers during storage migration.

## Learning journal and photos

- `LearningRepository` uses `yks_learning_journal`.
- Primary keys: `outcomes` and `questions`; the store also contains multiple one-time migration flags and raw pre-migration snapshots. Observed flags include `learning_topics_v1`, `learning_meb_units_v1`, `learning_meb_units_v2`, `learning_kitapsec_v1`, `learning_science_ayt_separation_v1`, and `learning_supplied_curriculum_v1`.
- Learning outcomes and question cards are JSON arrays. Question cards reference outcomes by stable IDs and photos by filenames.
- Photos live in private `filesDir/review_photos`. The archive importer validates filenames, image decodability, sizes, references, and ID uniqueness. Existing photo files must never be overwritten on name collision.
- Curriculum migrations preserve/remap outcome IDs and question links; old raw snapshots are retained for recovery.
- `LearningRepository` performs a migration chain in its initializer. Capture raw preferences and photo files before constructing it in any migration tool.

## AI coach state

- `ai_coach_cache` stores only the latest generated advice, its date, and the daily attempt gate (including the last exam ID). This is cache/product state, not exam source-of-truth.
- `ai_coach_long_term_memory` stores memory records with id, category, content, createdAt, updatedAt, and source. Its read path filters legacy heuristic auto-captured records and may persist the filtered set. Treat reads as potentially mutating until this behavior is deliberately refactored.
- `ai_secret_store` contains encrypted provider credentials; encryption depends on Android Keystore and is not directly portable to iOS. A future cross-platform credential layer must use platform secure storage adapters and an explicit user re-entry/transfer flow, not copy Android ciphertext.

## Full backup format

- `FullBackupManager` writes a ZIP with exactly four entries: `manifest.json`, `exams.json`, `study.json`, and `learning.zip`.
- Manifest schema: `yks_takip_full_backup`, version 1, counts for exams/study days/learning outcomes/questions/photos, and `secretsIncluded=false`.
- Full restore is staged and validated before merge. Existing local exams and study days win on conflict; learning data is merged by stable IDs; photos are staged and collision-renamed.
- The full archive imposes explicit per-section and total size limits. Preserve those safety checks or replace them with equally strict tested controls.
- AI credentials are deliberately excluded from backups. Never include them in SQLDelight exports or cloud sync.

## Migration hazards discovered

1. `ExamRepository.getAllExams()` catches any parse exception and returns an empty list. This is dangerous as a migration source: malformed JSON must be reported/quarantined, never silently interpreted as an empty database and then overwritten.
2. `StudyRepository.getAll()` also returns an empty list on parse failure. Migration must preserve the original raw value and surface a recoverable error.
3. `StudyPlanRepository.getAll()` returns an empty list on parse failure and may persist a legacy completion migration during reads. Protect the raw value before using it as a migration source.
4. `LearningRepository` runs a chain of one-time migrations in its initializer. A new importer must not instantiate or mutate legacy repositories casually; capture raw preferences and files before invoking code with migration side effects.
5. `ThemeManager.currentTheme()` can write migration state during a read. Capture raw preference state first.
6. `AiMemoryRepository.readAll()` may filter and persist legacy records during reads. Do not treat a read as side-effect-free during migration.
7. Existing rollback snapshots and timer carry fields are user-data recovery assets. Do not clear or repurpose them during schema conversion.
8. Exam JSON, study JSON, learning ZIP, and full ZIP are separate public compatibility formats. They must all remain readable; do not normalize them into one undocumented format.
9. Photo references are part of question data. A migration that copies question rows but not photo bytes and filename remapping is lossy.
10. `FullBackupManager.merge` coordinates exam, study, and learning commits with compensating rollback. SQL migration must provide an explicit transaction/recovery story for the whole dataset, not only one table at a time.
11. Android Keystore ciphertext cannot be assumed to decrypt on iOS or another installation. Credentials require platform-specific secure storage and a user-controlled transfer path.

## Required migration protocol

1. Read and checksum raw preference files and enumerate every key before any write.
2. Snapshot the entire legacy state and private photo directory to a recoverable staging area.
3. Parse each source format independently with strict validation; report per-section counts and errors.
4. Import into a versioned staging database in a single transaction where possible.
5. Verify row counts, stable IDs, totals/nets, completion flags, timer elapsed/carry state, question-to-outcome links, photo checksums, and backup round-trips.
6. Keep the legacy source untouched until verification passes and a durable migration-complete marker is committed.
7. Make migration idempotent. A second launch must not duplicate records or re-run curriculum transformations.
8. On any error, keep the old stores authoritative and expose a retry/recovery path. Never silently replace malformed source data with empty collections.

## 4.0 SQLDelight schema foundation (not yet production-wired)

- An isolated `:core:database` Kotlin Multiplatform module now defines `ExamRecord`, `SubjectResultRecord`, and `StudyDayRecord`.
- Exam IDs and metadata are preserved; subject order is explicit and weak topics remain a JSON boundary field; study dates remain primary keys with question/minute/estimated values.
- The module is intentionally not connected to Android repositories. No legacy data is being imported or mutated.
- Learning journal entities/photos, weekly plans, timers, AI cache/memory, and credentials are deliberately excluded pending their own mapping and platform storage decisions.
- See ADR 0004 for the schema mapping and production migration gate.

## Still to inventory

- Exact `LearningOutcome` and `QuestionCard` JSON schema and every migration snapshot key.
- Any less frequently accessed preferences not found by the initial source search.
- Image filename ownership rules and any orphan-photo cleanup behavior.
- Other exports/imports and Android Auto Backup / device-to-device restore rules.
- Current tests covering corrupt files, partial imports, duplicate IDs, rollback, and photo restoration.
