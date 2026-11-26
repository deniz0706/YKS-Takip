plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("app.cash.sqldelight")
}

kotlin {
    jvmToolchain(17)
    jvm()
    iosArm64()
    iosSimulatorArm64()
    iosX64()

    sourceSets {
        commonMain.dependencies {
            implementation("app.cash.sqldelight:runtime:2.0.2")
        }
    }
}

sqldelight {
    databases {
        create("YksDatabase") {
            packageName.set("com.deniz0706.ykstakip.database")
        }
    }
}
