# YKS Takip — Multi-Platform Architecture Audit

**Branch:** `feat/4.0-architecture-audit`
**Purpose:** Technical architecture audit before Stage 4 begins
**Status:** Preparatory changes only — NOT merged into main

---

## A. Actual Current Architecture

### Repository Layout

```
YKS-Takip/
├── build.gradle.kts                  # Root: Android + Kotlin MPP plugins declared
├── domain/                           # Kotlin Multiplatform module
│   ├── build.gradle.kts              # androidTarget() + jvm() targets only
│   └── src/commonMain/kotlin/
│       ├── domain/model/             # 10 @Serializable data models
│       ├── domain/repository/        # 2 interfaces (ExamRepository, LearningRepository)
│       └── domain/usecase/           # CalculateRankingUseCase + RankingEstimator interface
└── app/                              # Android-only application module
    ├── src/main/java/...             # Android-only code (Context, SharedPreferences)
    └── src/main/kotlin/...           # SQLDelight repositories
```

### Gradle Configuration Evidence

**Root `build.gradle.kts`** declares these plugins (none applied yet):
```kotlin
id("com.android.application") version "8.5.0" apply false
id("com.android.library") version "8.5.0" apply false
id("org.jetbrains.kotlin.android") version "2.0.21" apply false
id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
id("org.jetbrains.kotlin.multiplatform") version "2.0.21" apply false
id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
```

**`domain/build.gradle.kts`** — Genuine Kotlin Multiplatform module:
```kotlin
plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
    id("com.android.library")          # Applied but only for androidTarget compilation
}

kotlin {
    androidTarget()     # ← Only Android target configured
    jvm()               # ← JVM target configured
    // NO iosX64(), iosArm64(), iosSimulatorArm64(), macosX64(), etc.
}
```

Source sets:
- `commonMain` — kotlinx-serialization-json, kotlinx-coroutines-core
- `androidMain` → dependsOn(commonMain)
- `jvmMain` → dependsOn(commonMain)

**`app/build.gradle.kts`** — Pure Android application:
- Plugins: `com.android.application`, `kotlin-android`, `kotlin-compose`, `sqldelight`
- compileSdk 34, minSdk 26, targetSdk 35
- Depends on `:domain` project
- Full Android UI stack (Compose, Navigation Compose, ViewModel, LiveData)
- SQLDelight database (`YksDatabase`)
- No iOS, Web, or backend configuration whatsoever

---

### Domain Layer (commonMain)

**Models (all `@Serializable`):**
| File | Contents |
|------|----------|
| `Exam.kt` | Exam data class with totalCorrect/Wrong/Blank/Questions/Net |
| `SubjectResult.kt` | Per-subject result with net calculation |
| `ExamType.kt` | Enum: TYT(165min), AYT(180min) |
| `AytField.kt` | Enum: SAYISAL, ESIT_AGIRLIK, SOZEL |
| `StudyDay.kt` | Study history day |
| `StudyPlanItem.kt` | Study plan item |
| `SubjectConfig.kt` | Subject configuration |
| `LearningOutcome.kt` | Learning outcome tracking |
| `QuestionCard.kt` | Question/flashcard model |
| `AytField.kt` | AYT field enum |

**Repositories (interfaces):**
| File | Contents |
|------|----------|
| `ExamRepository.kt` | `getAllExams()`, `saveExam()`, `deleteExam()` etc. |
| `LearningRepository.kt` | `getOutcomes()`, `getQuestions()`, `saveOutcome()` etc. |

**Use cases:**
| File | Contents |
|------|----------|
| `CalculateRankingUseCase.kt` | Ranking orchestration, delegates to `RankingEstimator` interface |

---

### Android App Layer (Platform-Specific)

**Android-only model duplicates** (NOT `@Serializable`, use `org.json`):
| File | Package | Key Difference |
|------|---------|----------------|
| `app/.../model/Exam.kt` | `com.deniz0706.ykstakip.model` | Uses `org.json.JSONObject`, no `@Serializable`, custom `round2()` logic |
| `app/.../model/SubjectResult.kt` | `com.deniz0706.ykstakip.model` | Uses `org.json`, `questionCount` as constructor param (computed in domain) |
| `app/.../model/ExamType.kt` | `com.deniz0706.ykstakip.model` | Duplicate enum |
| `app/.../model/AytField.kt` | `com.deniz0706.ykstakip.model` | Duplicate enum |
| `app/.../model/StudyDay.kt` | `com.deniz0706.ykstakip.model` | Duplicate (no `@Serializable`) |
| `app/.../model/StudyPlanItem.kt` | `com.deniz0706.ykstakip.model` | Duplicate |
| `app/.../model/SubjectConfig.kt` | `com.deniz0706.ykstakip.model` | Duplicate |
| `app/.../model/LearningModels.kt` | `com.deniz0706.ykstakip.model` | No domain equivalent |

**Android-only repository/data classes** (all import `android.content.Context`):
| Class | Android Dependency |
|-------|-------------------|
| `AppSettings.kt` | `Context`, `SharedPreferences`, `StudySummaryWidgetProvider` |
| `ExamRepository.kt` | `Context`, `SharedPreferences` |
| `ExamSessionRepository.kt` | `Context` |
| `FocusSessionRepository.kt` | `Context` |
| `LearningRepository.kt` | `Context` |
| `AiMemoryRepository.kt` | `Context` |
| `AiSecretStore.kt` | `Context` |
| `AiCoachCache.kt` | `Context` |
| `AiContextBuilder.kt` | `Context` |
| `AiImageEncoder.kt` | `Context` |
| `ReviewReminderScheduler.kt` | `Context`, `NotificationCompat`, `WorkManager`, `NotificationChannel` |
| `FullBackupManager.kt` | `Context`, `ContentResolver`, `Uri` |
| `SettingsBackupManager.kt` | `Context` |
| `SqlStudyRepository.kt` | SQLDelight `AndroidSqliteDriver` |
| `SqlExamRepository.kt` | SQLDelight `AndroidSqliteDriver` |
| `SqlLearningRepository.kt` | SQLDelight `AndroidSqliteDriver` |

**Android-only ranking engine** (uses Android models, NOT domain models):
| File | Package | Issue |
|------|---------|-------|
| `YksRankingEstimator.kt` | `com.deniz0706.ykstakip.util` | Imports `com.deniz0706.ykstakip.model.*` (Android), not domain |
| `YksRankingCalculator.kt` | `com.deniz0706.ykstakip.util` | Same |
| `YksOfficialRankingLookup.kt` | `com.deniz0706.ykstakip.util` | Same, contains ÖSYM placement-score lookup tables |

**Domain ranking use case** (separate, uses domain models):
| File | Package | Status |
|------|---------|--------|
| `CalculateRankingUseCase.kt` | `com.deniz0706.ykstakip.domain.usecase` | Uses domain models, interface-based estimator |

**Problem:** Two parallel ranking engines exist with identical business logic but different model types. The Android `YksRankingEstimator`/`YksRankingCalculator` are Android-only and cannot compile for iOS.

---

## B. What Is Already Reusable for iOS

### Domain Layer — Fully Portable

All files in `domain/src/commonMain/` compile as pure Kotlin and are platform-agnostic:

1. **All 10 data models** (`@Serializable`): Exam, SubjectResult, ExamType, AytField, StudyDay, StudyPlanItem, SubjectConfig, LearningOutcome, QuestionCard, AytField
2. **Repository interfaces**: ExamRepository, LearningRepository (pure Kotlin interfaces)
3. **CalculateRankingUseCase**: Pure Kotlin, delegates to `RankingEstimator` interface — no Android dependencies

### Ranking Logic — Partially Portable

The `CalculateRankingUseCase` in domain is portable, BUT:
- Its `RankingEstimator` interface has no implementation in commonMain
- The actual `YksRankingEstimator` implementation lives in Android app layer using Android models
- The ÖSYM lookup tables in `YksOfficialRankingLookup` are Android-only

### What Is NOT Portable

| Component | Blocker |
|-----------|---------|
| All Android `model.*` classes | Use `org.json`, `Context`, `SharedPreferences` |
| All data/repository implementations | Use `Context`, `SharedPreferences`, SQLDelight `AndroidSqliteDriver` |
| `ReviewReminderScheduler` | Uses `WorkManager`, `NotificationCompat`, Android APIs |
| `FullBackupManager` + all `*BackupManager` | Use `Context`, `ContentResolver`, `Uri`, file system |
| `AppSettings` | Uses `SharedPreferences`, `Context` |
| AI service classes | Use `Context` |
| Compose UI screens | Android-only Compose |
| `StudySummaryWidgetProvider` | Android RemoteViews widget |

---

## C. What Prevents iOS Compilation Today

1. **No iOS targets in Kotlin MPP:** `domain/build.gradle.kts` only configures `androidTarget()` and `jvm()`. No `iosX64()`, `iosArm64()`, `iosSimulatorArm64()`.

2. **No CocoaPods export:** No `cocoapods` configuration to produce an iOS framework.

3. **Android-only ranking engine:** `YksRankingEstimator`/`YksRankingCalculator`/`YksOfficialRankingLookup` use Android models (`com.deniz0706.ykstakip.model.*`) and cannot be shared.

4. **Duplicate model layer:** Two parallel model systems (domain `@Serializable` vs Android `org.json`-based) with subtly different calculation logic:
   - Domain `Exam.totalNet`: `subjects.sumOf { it.net }.roundToInt().toDouble() / 100.0 * 100.0`
   - Android `Exam.totalNet`: `round2(subjects.sumOf { it.net })` where `round2(v) = (v * 100).roundToInt() / 100.0`
   - Domain `SubjectResult.net`: `correct - wrong * 0.25`
   - Android `SubjectResult.net`: `round2(correct - wrong / 4.0)`
   - These produce different results for fractional values!

5. **No serialization for iOS interop:** Even though models are `@Serializable`, there's no JSON schema or API contract defined for cross-platform communication.

6. **No shared networking:** No Ktor, no HTTP client, no API interface in commonMain.

---

## D. Target Architecture: Android + iOS + Web + Python AI

### Platform Breakdown

| Platform | UI Framework | Language | Responsibilities |
|----------|-------------|----------|-----------------|
| Android | Jetpack Compose | Kotlin | Native Android UI, widgets, notifications |
| iOS | SwiftUI | Swift | Native iOS UI, widgets, notifications |
| Web | React / TypeScript | TypeScript | Responsive web dashboard |
| Python AI Service | FastAPI | Python | AI coach, NLP, recommendation engine |

### Shared Layer (Kotlin Multiplatform)

```
domain/ (Kotlin MPP)
├── commonMain/
│   ├── model/          # @Serializable data models (single source of truth)
│   ├── repository/     # Repository interfaces
│   ├── usecase/        # CalculateRankingUseCase, OBP, study planning
│   ├── networking/     # Ktor client interface, API contracts
│   └── serialization/  # JSON schema for API + offline storage
├── androidMain/        # SQLDelight implementations, SharedPreferences
├── iosMain/            # (future) Core Data / Swift interop
└── jvmMain/            # (future) JVM tests, backend shared logic
```

### API Contract Layer

```
api/
├── openapi.yaml        # REST API specification (shared by Android, iOS, Web)
├── contracts/          # Kotlin/TypeScript/Python shared DTOs
└── versioning/         # API version strategy
```

### Python AI Service (Independent Backend)

```
ai-service/
├── src/
│   ├── main.py          # FastAPI application
│   ├── coach.py         # AI Coach conversation logic
│   ├── nlp.py           # Question text analysis
│   └── recommendations.py # Study plan recommendations
├── requirements.txt
├── config/              # API keys, secrets (server-side only)
└── tests/
```

**Secrets:** API keys, LLM provider credentials stored server-side only (environment variables, never in client code).

### Authentication & Synchronization

| Concern | Design |
|---------|--------|
| Auth | JWT-based auth via backend API (Android/iOS/Web share same auth client) |
| Sync | REST API + offline-first with local database sync on reconnect |
| Storage | Local: SQLDelight (Android), Core Data/SQLite (iOS), IndexedDB (Web); Remote: PostgreSQL |
| Deployment | Android: Google Play; iOS: App Store; Web: Vercel/Netlify; AI: Docker container |

---

## E. Staged Migration Plan, Costs & Risks

### Stage A: Domain Layer Unification (Now — Before Stage 4)

**Goal:** Eliminate duplicate model layer, unify ranking engine in domain, make domain iOS-compilable.

**Changes:**
1. Remove Android `model.*` duplicate classes; make all code use `domain.model.*`
2. Add `@Serializable` to all domain models (already done for most)
3. Move ranking logic (`YksRankingEstimator`, `YksRankingCalculator`, `YksOfficialRankingLookup`) into domain commonMain using domain models
4. Add iOS targets to `domain/build.gradle.kts`: `iosX64()`, `iosArm64()`, `iosSimulatorArm64()`
5. Add CocoaPods export configuration

**Cost:** ~1-2 weeks (one engineer)

**Risk:** LOW — calculation logic must be verified to produce identical results. The `round2()` vs `.roundToInt().toDouble() / 100.0 * 100.0` difference needs testing.

### Stage B: Shared Networking & API Contracts

**Goal:** Define REST API contracts, add Ktor client to domain, create Python AI service skeleton.

**Changes:**
1. Add `ktor-client-core`, `ktor-client-content-negotiation` to domain commonMain
2. Define `openapi.yaml` for exam tracking API
3. Create Python FastAPI service skeleton with auth endpoints
4. Implement repository interfaces with Ktor backend + local fallback

**Cost:** ~2-3 weeks

**Risk:** MEDIUM — need to design sync conflict resolution strategy

### Stage C: iOS Native App (SwiftUI)

**Goal:** Build iOS app sharing domain logic via Kotlin/Native framework.

**Changes:**
1. Configure CocoaPods export from domain module
2. Create SwiftUI iOS app consuming Kotlin framework
3. Implement iOS-specific storage (Core Data or SQLite)
4. Platform-specific UI (not shared Compose UI)

**Cost:** ~4-6 weeks (iOS engineer)

**Risk:** MEDIUM — Kotlin/Native interop with Swift, memory management

### Stage D: Web Client (TypeScript/React)

**Goal:** Web dashboard sharing API contracts.

**Changes:**
1. Generate TypeScript types from OpenAPI spec
2. Build React web dashboard
3. Share validation logic via shared contracts

**Cost:** ~3-4 weeks

**Risk:** LOW

### Stage E: Python AI Service

**Goal:** Independent AI backend service.

**Changes:**
1. FastAPI service with protected endpoints
2. Server-side secret management (API keys)
3. AI Coach conversation, NLP analysis, recommendations
4. Auth integration with main backend

**Cost:** ~3-4 weeks

**Risk:** LOW — independent service, no impact on existing clients

---

## F. Specific Architectural Changes to Make Now (Before Stage 4)

### Change 1: Eliminate Duplicate Model Layer

**Problem:** Android `com.deniz0706.ykstakip.model.*` duplicates domain models with different JSON serialization and calculation logic.

**Fix:** Delete Android-only model classes; update all imports to use `com.deniz0706.ykstakip.domain.model.*`.

**Files affected:**
- `app/src/main/java/com/deniz0706/ykstakip/model/Exam.kt` → DELETE
- `app/src/main/java/com/deniz0706/ykstakip/model/SubjectResult.kt` → DELETE
- `app/src/main/java/com/deniz0706/ykstakip/model/ExamType.kt` → DELETE
- `app/src/main/java/com/deniz0706/ykstakip/model/AytField.kt` → DELETE
- `app/src/main/java/com/deniz0706/ykstakip/model/StudyDay.kt` → DELETE
- `app/src/main/java/com/deniz0706/ykstakip/model/StudyPlanItem.kt` → DELETE
- `app/src/main/java/com/deniz0706/ykstakip/model/SubjectConfig.kt` → DELETE
- `app/src/main/java/com/deniz0706/ykstakip/model/LearningModels.kt` → DELETE (no domain equivalent)

**Imports to update:** `YksNavGraph.kt`, `YksRankingEstimator.kt`, `YksRankingCalculator.kt`, `YksOfficialRankingLookup.kt`

### Change 2: Unify Ranking Engine in Domain

**Problem:** Ranking logic exists in both Android app layer (Android models) and domain layer (domain models, but no estimator implementation).

**Fix:** Move `YksRankingEstimator`, `YksRankingCalculator`, `YksOfficialRankingLookup` into `domain/src/commonMain/kotlin/com/deniz0706/ykstakip/domain/usecase/` using domain models.

**Cost:** Ensures ranking logic is identical across all platforms.

### Change 3: Add iOS Targets to Domain Module

**Problem:** `domain/build.gradle.kts` only has `androidTarget()` and `jvm()`.

**Fix:**
```kotlin
kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvm()

    sourceSets {
        val iosMain by getting {
            dependsOn(commonMain)
        }
        val iosArm64Main by getting {
            dependsOn(iosMain)
        }
        val iosX64Main by getting {
            dependsOn(iosMain)
        }
        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
        }
    }
}
```

### Change 4: Add CocoaPods Export

**Fix:** Add to `domain/build.gradle.kts`:
```kotlin
kotlin {
    // ... targets ...
    cocoapods {
        summary = "YKS Takip shared domain logic"
        homepage = "https://github.com/deniz0706/YKS-Takip"
        ios.deploymentTarget = "15.0"
        framework {
            baseName = "YKS_Domain"
        }
        plugin {
            // CocoaPods plugin configuration
        }
    }
}
```

### Change 5: Add Serialization Schema for API

**Fix:** Add explicit JSON schema/serialization configuration to domain models for cross-platform API contracts.

---

## Summary

| Question | Answer |
|----------|--------|
| Is `commonMain` a genuine KMP module? | Yes, but only targets Android + JVM |
| Can domain compile for iOS today? | No — no iOS targets, no CocoaPods export |
| What's portable? | All domain models, repository interfaces, CalculateRankingUseCase |
| What blocks iOS? | No iOS targets, Android-only ranking engine, duplicate model layer, no networking |
| Target architecture? | Android (Compose) + iOS (SwiftUI) + Web (TypeScript) + Python AI service |
| Changes needed now? | Unify models, move ranking to domain, add iOS targets, add CocoaPods export |

**Recommendation:** Complete Changes 1-5 on branch `feat/4.0-architecture-audit` before starting Stage 4. These are safe, preparatory changes that do not modify the Android app's behavior or ranking engine output.
