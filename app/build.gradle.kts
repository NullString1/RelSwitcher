plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.danielkern.relswitcher"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.danielkern.relswitcher"
        minSdk = 31
        targetSdk = 31
        versionCode =  10
        versionName = "4.0.0"
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
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("com.google.code.gson:gson:2.12.1")
    implementation("com.github.karanchuri:PermissionManager:0.1.0") //Perm Manager
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
