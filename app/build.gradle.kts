plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.clipforge.videoeditor"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.clipforge.videoeditor"
        minSdk = 24
        targetSdk = 35

        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    // AndroidX Core
    implementation("androidx.core:core-ktx:1.15.0")

    // AppCompat
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Material Components
    implementation("com.google.android.material:material:1.12.0")

    // Media3 ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.6.1")

    // Media3 UI - PlayerView
    implementation("androidx.media3:media3-ui:1.6.1")
}
