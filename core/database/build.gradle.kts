plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("app.cash.sqldelight")
}

kotlin {
    androidTarget()
    jvmToolchain(17)
    iosArm64()
    iosSimulatorArm64()
    iosX64()

    sourceSets {
        commonMain.dependencies {
            implementation("app.cash.sqldelight:runtime:2.0.2")
        }
        androidMain.dependencies {
            api("app.cash.sqldelight:android-driver:2.0.2")
        }
        iosMain.dependencies {
            implementation("app.cash.sqldelight:native-driver:2.0.2")
        }
    }
}

android {
    namespace = "com.deniz0706.ykstakip.database"
    compileSdk = 35
    defaultConfig {
        minSdk = 26
    }
}

sqldelight {
    databases {
        create("YksDatabase") {
            packageName.set("com.deniz0706.ykstakip.database")
        }
    }
}
