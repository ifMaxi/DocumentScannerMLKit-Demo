plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.daggerHilt)
    alias(libs.plugins.kps)
}

android {
    namespace = "com.maxidev.documentscanner"

    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.maxidev.documentscanner"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {

            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    kotlin {
        compileOptions {
            val javaCompatibility = JavaVersion.VERSION_21

            sourceCompatibility = javaCompatibility
            targetCompatibility = javaCompatibility
        }
    }

    buildFeatures {
        compose = true
    }

    buildToolsVersion = "36.1.0"
}

dependencies {

    // Ksp
    ksp(libs.hilt.android.compiler)
    ksp(libs.androidx.room.compiler)

    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Compose
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.ui.text.google.fonts)

    // Material 3
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // ML Kit
    implementation(libs.play.services.mlkit.document.scanner)

    // Data Store
    implementation(libs.androidx.datastore.preferences)

    // Navigation
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    // Hilt
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)

    // Coil
    implementation(libs.coil3.coil.compose)
    implementation(libs.coil3.coil.network.okhttp)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.core.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}