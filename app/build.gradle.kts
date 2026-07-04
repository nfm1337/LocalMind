plugins {
    id("convention.android.app")
    id("convention.ktlint")
    id("convention.detekt")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "il.nfm.localmind"
    val isModelProbeTask =
        gradle.startParameter.taskNames.any { it.endsWith("connectedModelProbeAndroidTest") }
    val physicalModelProbe =
        providers
            .gradleProperty("physicalModelProbe")
            .orElse(if (isModelProbeTask) "true" else "false")
    val modelProbeAppCommit =
        providers.gradleProperty("modelProbeAppCommit").orElse("androidTest-run")

    defaultConfig {
        applicationId = "il.nfm.localmind"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["physicalModelProbe"] = physicalModelProbe.get()
        testInstrumentationRunnerArguments["modelProbeAppCommit"] = modelProbeAppCommit.get()

        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += "arm64-v8a"
        }
    }
    externalNativeBuild { cmake { path("src/main/cpp/CMakeLists.txt") } }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.litert.lm)
    implementation(libs.onnxruntime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    implementation(libs.hilt.core)
    ksp(libs.hilt.compiler)

    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(kotlin("test"))
}
