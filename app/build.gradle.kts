import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Marque de construction, affichée au bas des options pour reconnaître l'exécutable :
// commit d'origine (« + » si le répertoire de travail avait des modifications) et heure
// du build, recalculée à chaque construction.
fun git(vararg args: String) = providers.exec { commandLine("git", *args) }.standardOutput.asText.get().trim()
val buildMark = "${git("rev-parse", "--short", "HEAD")}${if (git("status", "--porcelain").isEmpty()) "" else "+"} · " +
    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))

android {
    namespace = "name.dubourg.mahjong"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "name.dubourg.mahjong"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "0.2"
        buildConfigField("String", "BUILD_MARK", "\"$buildMark\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
}
