# ADR 0006: Transactional SQLDelight aggregate stores

- Status: Accepted for the isolated 4.0 database module
- Date: 2026-11-26

## Decision

- Keep the database adapters in `:core:database`; they consume the shared domain models and do not depend on Android APIs in commonMain.
- Save an exam parent row and all ordered subject rows inside one SQLDelight transaction.
- Use SQLite `ON CONFLICT(id) DO UPDATE` for the exam parent. Do not use `INSERT OR REPLACE` for parent rows: SQLite REPLACE deletes the existing row before inserting and can trigger foreign-key cascades.
- Delete child subject rows explicitly before deleting parent rows or replacing the complete exam set. This does not rely on a driver's foreign-key pragma being enabled.
- Reject duplicate exam IDs before starting a full replacement.
- Preserve legacy chronological ascending order for exam and study history reads.
- Keep daily study history as an independent date-keyed store.
- Exercise the adapters against an in-memory SQLite database in JVM tests.

## Safety boundary

These stores are not yet installed as the production repositories and do not import SharedPreferences. The legacy stores remain authoritative until a separate importer can snapshot raw source state, parse strictly, stage all records, verify counts/IDs/aggregates/photos and commit with a tested recovery path.
