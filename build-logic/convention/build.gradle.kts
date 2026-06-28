plugins {
    `kotlin-dsl`
}

group = "il.nfm.buildlogic"

kotlin {
    jvmToolchain(21)
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    compileOnly(libs.ktlint.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "convention.android.app"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("ktlint") {
            id = "convention.ktlint"
            implementationClass = "KtlintConventionPlugin"
        }
        register("detekt") {
            id = "convention.detekt"
            implementationClass = "DetektConventionPlugin"
        }
        register("models") {
            id = "convention.models"
            implementationClass = "ModelsConventionPlugin"
        }
    }
}
