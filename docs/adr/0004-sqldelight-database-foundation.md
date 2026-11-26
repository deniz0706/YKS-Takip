# ADR 0004: SQLDelight shared database foundation

- Status: Accepted as an isolated schema foundation; not yet wired into app repositories
- Date: 2026-11-26

## Context

The app currently stores exams and study days in Android SharedPreferences JSON. The long-term 4.0 direction needs a transactional store usable from Android and iOS, but the existing backup/import behavior and legacy data must remain authoritative until a lossless migration is implemented and verified.

## Decision

- Add an isolated Kotlin Multiplatform module `:core:database` using SQLDelight 2.0.2.
- Declare an Android library target and iOS device/simulator targets, matching the shared app architecture. The Android app consumes the Android variant rather than a generic JVM artifact.
- Create normalized tables for exam metadata, ordered subject results, and daily study records.
- Keep weak-topic lists as JSON text at the schema boundary for now; do not invent a premature topic relational model.
- Keep the module unreferenced by production app repositories until platform driver factories, transactional import, rollback, and migration verification are implemented.
- Add CI compilation tasks for the Android and iOS database targets. Android APK CI also compiles the Android database target.

## Initial schema mapping

| Legacy concept | SQLDelight table | Important preservation rule |
|---|---|---|
| Exam | `ExamRecord` | Preserve legacy ID, title, date, type, AYT field, notes, publisher |
| Subject result | `SubjectResultRecord` | Preserve subject order, question counts, correct/wrong, time, weak-topic JSON |
| Study day | `StudyDayRecord` | Preserve date, question count, study minutes, estimated marker |

## Explicit non-goals

- No SharedPreferences data is read, deleted, or rewritten by this module.
- No existing repository is switched to SQLDelight.
- No backup format is changed.
- Learning outcomes, question cards, photos, timer state, study plans, AI memory/cache, and secrets are not yet included in this initial schema.
- No assumption is made that Android Keystore ciphertext can move to iOS.

## Migration gate

Before production adoption, implement platform drivers and a staged importer that snapshots raw legacy stores and photo bytes, parses strictly, imports transactionally, verifies IDs/counts/relationships/aggregates/photo checksums, exercises backup round trips, and leaves legacy data untouched on any failure. Only then may a separately versioned migration-complete marker be written.
