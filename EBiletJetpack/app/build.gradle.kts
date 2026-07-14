plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.alperensarac.ebiletjetpack"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.alperensarac.ebiletjetpack"
        minSdk = 24
        targetSdk = 35
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    /*
    Navigation Compose:
    Ekranlar arası geçiş için.
*/
    implementation("androidx.navigation:navigation-compose:2.8.5")

    /*
        Retrofit:
        PHP backend ile HTTP istekleri.
    */
    implementation("com.squareup.retrofit2:retrofit:2.11.0")

    /*
        Gson Converter:
        JSON cevaplarını Kotlin data class'larına çevirir.
    */
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    /*
        OkHttp Logging:
        API isteklerini Logcat'te görmemizi sağlar.
    */
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    /*
        Coil Compose:
        Compose içinde internetten görsel yüklemek için.
        Etkinlik posterlerinde kullanacağız.
    */
    implementation("io.coil-kt:coil-compose:2.7.0")

    /*
        QR kod üretmek için ZXing core.
    */
    implementation("com.google.zxing:core:3.5.3")

    /*
        QR kod okutmak için JourneyApps ZXing.
    */
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
}