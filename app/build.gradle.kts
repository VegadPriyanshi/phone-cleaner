
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.phonecleaner"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.phonecleaner"
        minSdk = 26
        targetSdk = 35

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            resValue(
                "string",
                "native_language_ad_id",
                "ca-app-pub-3940256099942544/2247696110"
            )
            resValue(
                "string",
                "native_language_ad_2_id",
                "ca-app-pub-3940256099942544/2247696110"
            )
            resValue(
                "string",
                "native_onboarding_ad_id",
                "ca-app-pub-3940256099942544/2247696110"
            )
            resValue(
                "string",
                "native_onboarding_full_ad_id",
                "ca-app-pub-3940256099942544/2247696110"
            )
            resValue(
                "string",
                "native_onboarding_last_ad_id",
                "ca-app-pub-3940256099942544/2247696110"
            )
        }

        release {
            isMinifyEnabled = false

            resValue(
                "string",
                "native_language_ad_id",
                "ca-app-pub-3940256099942544/2247696110"
            )
            resValue(
                "string",
                "native_language_ad_2_id",
                "ca-app-pub-3940256099942544/2247696110"
            )
            resValue(
                "string",
                "native_onboarding_ad_id",
                "ca-app-pub-3940256099942544/2247696110"
            )
            resValue(
                "string",
                "native_onboarding_full_ad_id",
                "ca-app-pub-3940256099942544/2247696110"
            )
            resValue(
                "string",
                "native_onboarding_last_ad_id",
                "ca-app-pub-3940256099942544/2247696110"
            )

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
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
        viewBinding = true
        resValues = true
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)

    // UI
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.viewpager2)

    // Fragment
    implementation(libs.androidx.fragment.ktx)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // ML Kit Translation
    implementation(libs.mlkit.translate)

    // Onboarding indicator
    implementation(libs.dotsindicator)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Google Mobile Ads
    implementation(libs.play.services.ads)

    // Firebase BOM and Remote Config
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)

    // Tests
    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
