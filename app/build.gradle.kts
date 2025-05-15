plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.ksp)
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.vn.elsanobooking"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.vn.elsanobooking"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            resources.excludes += listOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/INDEX.LIST",
                "META-INF/DEPENDENCIES",
                "META-INF/io.netty.versions.properties"
            )
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
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.firebase.appdistribution.gradle)
    implementation(libs.androidx.benchmark.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
// Retrofit and Gson
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("org.json:json:20231013")
    // OkHttp
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ViewModel and LiveData
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.8.0")

    // Coil for image loading
    implementation ("io.coil-kt:coil-compose:2.4.0")

    // Compose dependencies (add as needed)
    implementation ("androidx.activity:activity-compose:1.9.0")
    implementation ("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    
    // Add Compose Material for pull-to-refresh
    implementation("androidx.compose.material:material:1.6.2")

    // Microsoft SignalR with RxJava 3 explicit dependencies
    implementation("com.microsoft.signalr:signalr:7.0.5")
    implementation("io.reactivex.rxjava3:rxjava:3.1.5")

    // Force consistent OkHttp version
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Configuration to ensure version compatibility
    configurations.all {
        resolutionStrategy {
            // Force RxJava 3 version
            force("io.reactivex.rxjava3:rxjava:3.1.5")
            // Force OkHttp version
            force("com.squareup.okhttp3:okhttp:4.12.0")
        }
    }

    // Hilt dependencies
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.squareup.retrofit2:converter-scalars:2.9.0")

}



