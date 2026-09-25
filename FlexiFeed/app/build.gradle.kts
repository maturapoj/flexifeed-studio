plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = false
            binaryOption("bundleId", "com.flexifeed.app.ComposeApp")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.coil3.compose)
            implementation(libs.coil3.network.ktor3)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.navigation.compose.multiplatform)
            implementation(libs.lifecycle.viewmodel.compose.multiplatform)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }

        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(libs.ktor.client.okhttp)

        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    debugImplementation(libs.chucker)
    releaseImplementation(libs.chucker.noop)
}

android {
    namespace = "com.flexifeed.app"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.flexifeed.app"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resValue("string", "app_name", "FlexiFeed")
        buildConfigField("String", "ENVIRONMENT", "\"DEFAULT\"")
        buildConfigField("String", "SDUI_BASE_URL", "\"http://10.0.2.2:8080/\"")
    }

    sourceSets["main"].manifest.srcFile("src/main/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/main/res")

    flavorDimensions += listOf("environment")
    productFlavors {
        create("local") {
            dimension = "environment"
            applicationIdSuffix = ".local"
            versionNameSuffix = "-local"
            resValue("string", "app_name", "FlexiFeed Local")
            buildConfigField("String", "ENVIRONMENT", "\"LOCAL\"")
            buildConfigField("String", "SDUI_BASE_URL", "\"http://10.0.2.2:8080/\"")
        }
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "FlexiFeed Dev")
            buildConfigField("String", "ENVIRONMENT", "\"DEV\"")
            buildConfigField("String", "SDUI_BASE_URL", "\"https://flexifeed-studio.onrender.com/\"")
        }
        create("sit") {
            dimension = "environment"
            applicationIdSuffix = ".sit"
            versionNameSuffix = "-sit"
            resValue("string", "app_name", "FlexiFeed SIT")
            buildConfigField("String", "ENVIRONMENT", "\"SIT\"")
            buildConfigField("String", "SDUI_BASE_URL", "\"https://sit-api.flexifeed.com/\"")
        }
        create("prod") {
            dimension = "environment"
            resValue("string", "app_name", "FlexiFeed")
            buildConfigField("String", "ENVIRONMENT", "\"PROD\"")
            buildConfigField("String", "SDUI_BASE_URL", "\"https://api.flexifeed.com/\"")
        }
    }

    buildTypes {
        debug {
            // Debug configs
        }
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}