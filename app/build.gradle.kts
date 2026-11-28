plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.squareup.sqldelight") version "1.5.5"
}

val ciRunNumber = providers.environmentVariable("GITHUB_RUN_NUMBER").orNull?.toIntOrNull()
val ciBranchName = providers.environmentVariable("GITHUB_REF_NAME").orNull
val appVersionFamily = if (ciBranchName == "2.0") "2.0" else "4.0"
val appVersionCode = 1000 + (ciRunNumber ?: 0)
val appVersionName = ciRunNumber?.let { "$appVersionFamily.$it" } ?: "4.0-dev"

android {
    namespace = "com.deniz0706.ykstakip"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.deniz0706.ykstakip"
        minSdk = 26
        targetSdk = 35
        versionCode = appVersionCode
        versionName = appVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            val ciPreviewKeystore = System.getenv("YKS_PREVIEW_DEBUG_KEYSTORE")
            if (!ciPreviewKeystore.isNullOrBlank()) {
                storeFile = file(ciPreviewKeystore)
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = false
        compose = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    sourceSets {
        named("main") {
            java.srcDirs("src/main/kotlin")
        }
    }
}

sqldelight {
    database("YksDatabase") {
        packageName = "com.deniz0706.ykstakip.data.local"
    }
}

configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "androidx.lifecycle") {
            useVersion("2.9.4")
            because("Align Lifecycle artifacts with the Kotlin 2.2 / AGP 8.10 toolchain.")
        }
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core:domain"))
    implementation(project(":core:database"))
    implementation(project(":composeApp"))

    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.fragment:fragment-ktx:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.4")
    implementation("androidx.work:work-runtime-ktx:2.9.1")
    implementation("androidx.activity:activity-ktx:1.9.1")

    implementation("androidx.navigation:navigation-compose:2.8.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.2")

    implementation("androidx.sqlite:sqlite:2.4.0")
    implementation("com.squareup.sqldelight:runtime:1.5.5")
    implementation("com.squareup.sqldelight:android-driver:1.5.5")
    implementation("com.squareup.sqldelight:coroutines-extensions:1.5.5")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("androidx.test:core:1.6.0")
    testImplementation("org.json:json:20260814")
    testImplementation("com.squareup.sqldelight:sqlite-driver:1.5.5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("androidx.navigation:navigation-testing:2.8.2")

    debugImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}
