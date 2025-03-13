plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.danielkern.relswitcher"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.danielkern.relswitcher"
        minSdk = 33
        versionCode =  11
        versionName = "5.0.0"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.google.code.gson:gson:2.12.1")
    implementation("com.github.karanchuri:PermissionManager:0.1.0") //Perm Manager
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
