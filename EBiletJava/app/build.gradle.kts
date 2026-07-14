plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.alperensarac.ebiletjava"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.alperensarac.ebiletjava"
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
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    /*
     Retrofit:
     Android uygulamanın PHP backend ile konuşmasını sağlar.
 */
    implementation("com.squareup.retrofit2:retrofit:2.11.0")

    /*
        Gson Converter:
        JSON cevaplarını Java model class'larına çevirir.
    */
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    /*
        OkHttp log:
        API isteklerini Logcat'te görmek için.
    */
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    /*
        Glide:
        Etkinlik posterlerini URL üzerinden göstermek için.
    */
    implementation("com.github.bumptech.glide:glide:4.16.0")

    /*
        QR kod üretmek için.
    */
    implementation("com.google.zxing:core:3.5.3")

    /*
        QR kod okutmak için.
    */
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
}