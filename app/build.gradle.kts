import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    id("kotlin-parcelize")
}

android {
    namespace = "net.runner.show"
    compileSdk = 35
    buildFeatures{
        viewBinding= true
        buildConfig =true
    }

    defaultConfig {
        applicationId = "net.runner.show"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        val properties =  Properties()
        properties.load(project.rootProject.file("apikeys.properties").inputStream())
        buildConfigField("String","GENERATIVE_API_KEY","\"${properties.getProperty("GENERATIVE_API_KEY","")}\"")
        buildConfigField("String","ONESIGNAL_APP_ID","\"${properties.getProperty("ONESIGNAL_APP_ID","")}\"")

    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val properties =  Properties()
            properties.load(project.rootProject.file("apikeys.properties").inputStream())
            buildConfigField("String","GENERATIVE_API_KEY","\"${properties.getProperty("GENERATIVE_API_KEY","")}\"")
            buildConfigField("String","ONESIGNAL_APP_ID","\"${properties.getProperty("ONESIGNAL_APP_ID","")}\"")

        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.google.generativeai)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.crashlytics)
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.activity.ktx)

    implementation("com.onesignal:OneSignal:[5.0.0, 5.99.99]")
    implementation(libs.androidx.work.runtime.ktx)
}