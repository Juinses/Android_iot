plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.practica"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.practica"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
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
    implementation(platform("androidx.compose:compose-bom:2025.01.00"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation(libs.androidx.navigation.runtime.ktx)
    // SplashScreen API (compatibilidad hacia atrás)
    implementation("androidx.core:core-splashscreen:1.0.1")
    // Para manejar delay/cargas en Activity
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    // (Opcional) Lottie para Compose
    implementation("com.airbnb.android:lottie-compose:6.4.0")
    implementation(libs.ui)
    // Retrofit para llamadas HTTP
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // Convertidor JSON (usa Moshi o Gson, aquí Moshi)
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    // OkHttp logging (ver requests/responses en Logcat)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation(libs.firebase.crashlytics.buildtools)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}