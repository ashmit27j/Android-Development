plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.c013.ashmit_mad_assignment2"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.c013.ashmit_mad_assignment2"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Standard AndroidX libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // 1. Declare the Firebase BOM to manage versions (using the libs catalog is fine)
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))

    // 2. 🚨 CRITICAL FIX: Explicitly implement the Realtime Database (version is omitted, managed by BOM)
    // REMOVE implementation(libs.firebase.database) and use the string literal if the libs catalog is the issue.
    implementation("com.google.firebase:firebase-database")

    // Optional: Keep Analytics
    implementation("com.google.firebase:firebase-analytics")

    // Testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}