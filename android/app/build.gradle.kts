import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}
android {
    namespace = "com.spotlyric.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.spotlyric.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.spotlyric.app.HiltTestRunner"

        // Deep link scheme for Spotify PKCE callback
        manifestPlaceholders["appAuthRedirectScheme"] = "spotlyric"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
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
        buildConfig = true
    }
}

dependencies {
    // Compose BOM — pins all Compose library versions together
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.activity.compose)

    // Lifecycle + ViewModel
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    // Navigation
    implementation(libs.navigation.compose)

    // Hilt DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room — local SQLite cache
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore — token persistence
    implementation(libs.datastore.preferences)

    // Retrofit + OkHttp — Spotify API + SerpAPI
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // AppAuth — Spotify PKCE (no Spotify Android SDK needed)
    implementation(libs.appauth)

    // Google AI SDK — Gemini (lyrics extraction + translation)
    implementation(libs.generativeai)

    // Jsoup — on-device HTML parsing (replaces BeautifulSoup + Trafilatura)
    implementation(libs.jsoup)

    // Coil — album art image loading
    implementation(libs.coil.compose)

    // Coroutines
    implementation(libs.coroutines.android)

    // --- Unit Tests ---
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.core.testing)
    testImplementation(libs.room.testing)
    testImplementation(libs.mockwebserver)

    // --- Instrumented / UI Tests ---
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.coroutines.test)

    // Debug only
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
