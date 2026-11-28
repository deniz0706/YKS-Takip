# ADR 0005: Durable migration checkpoints

- Status: Accepted as bookkeeping only
- Date: 2026-11-26

## Context

The legacy stores can contain years of exam, study, learning, and photo data. A migration must be restartable and must not treat a partially imported database as authoritative.

## Decision

- Store migration progress in SQLDelight's `MigrationCheckpoint` table.
- Identify a migration by a stable `migrationId` and a fingerprint of the exact raw source snapshot.
- Record the target schema version, state, start/completion timestamps, and last recoverable error.
- A state value is diagnostic progress only. The application may mark migration complete only after the importer has independently verified counts, IDs, relationships, aggregates, timer carry, and photo checksums.
- If the source fingerprint changes between attempts, do not resume a partially staged import as though it were the same source; restart or explicitly reconcile it.
- Keep legacy stores untouched until the complete migration transaction and verification succeed.

## Non-goals

This table does not implement the importer, compute source fingerprints, take raw snapshots, or authorize deletion of legacy data. No migration is run by adding this table.
