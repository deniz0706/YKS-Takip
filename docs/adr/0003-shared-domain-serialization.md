# ADR 0003: Shared domain serialization boundary

- Status: Accepted for the incremental 4.0 migration
- Date: 2026-11-26

## Context

The first shared Kotlin Multiplatform domain slice contains exam aggregates, subject results, exam types, AYT fields, study days, and validation result types. These values must be representable on Android and iOS without depending on Android `org.json` or platform APIs.

The existing Android app also has a public legacy JSON shape. Existing exam backups and SharedPreferences data must remain readable and must not be silently rewritten merely because a new serializer is introduced.

## Decision

- Use Kotlinx Serialization in `:core:domain` for new shared-domain transport and persistence boundaries.
- Keep serialization annotations and generated serializers in `commonMain`; do not add Android/JVM-only JSON dependencies to shared code.
- Keep the existing Android JSON adapter as the compatibility boundary until a separately tested migration changes its implementation.
- Configure readers at explicit boundaries (for example, `ignoreUnknownKeys` only where forward-compatible input is intended); do not globally weaken validation.
- Preserve optional model defaults and enum wire names. Any future rename requires an explicit serial name or versioned migration.
- Add common tests for nested round trips, Turkish text, missing optional fields, and polymorphic validation results.

## Consequences

- Shared domain objects can now be serialized by Android and iOS without duplicating DTO definitions.
- This does not itself migrate SharedPreferences, change backup formats, or make Android Keystore credentials portable.
- Legacy exam JSON remains governed by the existing Android adapter and its regression tests.
- SQLDelight schema design remains a separate decision; serialization must not be mistaken for a transactional database.
