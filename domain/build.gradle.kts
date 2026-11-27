plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
}

kotlin {
    androidTarget {
        publishLibraryAt("domain")
    }
    jvm {
        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget = "17"
                freeCompilerArgs = freeCompilerArgs.filter { it != "-Xopt-in=kotlin.RequiresOptIn" }
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
            }
        }
        val androidMain by getting {
            dependsOn(commonMain)
        }
        val jvmMain by getting {
            dependsOn(commonMain)
        }
    }
}

android {
    namespace = "com.deniz0706.ykstakip.domain"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
