# ADR 0007 — Language Selection and XML Exit Policy

- Status: Accepted
- Date: 2026-11-26
- Scope: YKS Takip 4.0 and later

## Decision

Choose each technology by the responsibility it serves best. The project is not required to use a language merely to increase the language count. Kotlin remains the default application and shared-domain language; specialist languages are introduced only when they provide a measurable advantage.

The product UI remains Turkish-only.

## Technology boundaries

| Technology | Primary responsibility | Adoption rule |
|---|---|---|
| Kotlin | Android application, shared domain, application orchestration, business rules | Default implementation language |
| Compose Multiplatform | Declarative UI for Android and, where practical, iOS | All newly built screens use Compose; no new XML layouts |
| Swift / SwiftUI | iOS app shell, Apple platform APIs, native integrations, and UI only where native behavior materially improves the product | Add when implementing the real iOS client; keep shared rules in Kotlin |
| Python | Statistical research, data pipelines, offline model training/evaluation, experimentation, and a backend inference service when justified | Do not embed a Python runtime in the Android app for ordinary application logic |
| TypeScript | A web client, admin console, or web-specific service | Not part of the default mobile architecture; introduce only after a concrete web requirement exists |
| SQL | Relational schema, queries, migrations, and data integrity | SQLDelight owns shared database definitions where supported |
| Gradle Kotlin DSL | Build configuration and automation | Keep build logic declarative and reproducible |

## XML exit policy

The target is to eliminate XML from application UI implementation:

- Replace XML layouts and view hierarchies with Compose.
- Replace XML drawable-based visual components with Compose drawing, shapes, and vector/image assets where appropriate.
- Replace view binding and findViewById dependencies with Compose state and typed interfaces.
- Do not add new XML UI resources.
- Migrate existing screens incrementally, preserving navigation, state, accessibility, theming, and user data.

Android platform packaging still has XML-based contracts such as the manifest and certain system metadata/configuration resources (for example backup rules and app-widget provider declarations). These are not UI implementations. Keep only those platform-required XML files unless an equally reliable supported alternative exists. The goal is zero XML-based screen/layout UI, not an unsafe attempt to remove Android's required platform descriptors.

## Architecture rules

1. Shared domain rules must not depend on Android, UIKit, Compose UI, Python, or web frameworks.
2. Keep platform adapters thin. Avoid duplicating ranking calculations, net calculations, exam validation, and study analytics across platforms.
3. Preserve the existing ranking engine and its calibration fixtures unless a separately reviewed change explicitly authorizes work on it.
4. Python services must communicate through documented, versioned interfaces; no hidden runtime dependency from the mobile app.
5. TypeScript is optional and should not be introduced before a real web product requirement is approved.
6. No language migration may change persisted formats, application ID, signing identity, or user data without compatibility tests and a rollback path.
7. CI must validate each new language/toolchain before it becomes a required part of the product.

## Rollout order

1. Stabilize Kotlin Multiplatform domain and database foundations.
2. Finish data inventory and safe legacy migration.
3. Move Android screen composition from Fragments/XML views to Compose incrementally.
4. Remove obsolete XML layouts, drawable UI resources, view binding, and legacy view dependencies after parity checks.
5. Build the iOS client with shared Kotlin domain/data rules and Swift platform integration.
6. Evaluate Python for concrete analytics/ML workloads using reproducible datasets and offline evaluation.
7. Add TypeScript only if a web client or admin console becomes a committed product requirement.

## Non-goals

- Adding languages for marketing or novelty.
- Rewriting stable code without measurable benefit.
- Moving the YKS ranking engine into Python just because Python is popular.
- Removing Android-required manifest or system metadata at the cost of app behavior.
- Introducing a web application by default.