plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.alperensarac.projectmanagementauthenticator"

    compileSdk = 35

    defaultConfig {
        applicationId =
            "com.alperensarac.projectmanagementauthenticator"

        minSdk = 24
        targetSdk = 35

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        /*
  * =================================================
  * BACKEND ADRESLERİ
  * =================================================
  *
  * Android uygulaması gerçek telefon veya aynı
  * ağdaki bir cihaz üzerinde çalışacağı için
  * localhost kullanmıyoruz.
  *
  * 10.159.57.58:
  * Backend servislerinin çalıştığı bilgisayarın
  * güncel yerel ağ IP adresidir.
  *
  * .NET API:
  * http://10.159.57.58:8080
  *
  * Python Authenticator API:
  * http://10.159.57.58:8090
  *
  * BuildConfig sayesinde bu adresleri Kotlin
  * dosyalarında tek merkezden kullanabileceğiz.
  */
        buildConfigField(
            "String",
            "MAIN_BACKEND_BASE_URL",
            "\"http://10.159.57.58:8080/\"",
        )

        buildConfigField(
            "String",
            "AUTHENTICATOR_BASE_URL",
            "\"http://10.159.57.58:8090/\"",
        )

        /*
         * WebSocket adresi HTTP yerine ws protokolü
         * kullanır.
         *
         * Python Authenticator servisi aynı bilgisayarda
         * 8090 portunda çalıştığı için WebSocket bağlantısı
         * da bilgisayarın güncel yerel IP adresini kullanır.
         */
        buildConfigField(
            "String",
            "AUTHENTICATOR_WEBSOCKET_BASE_URL",
            "\"ws://10.159.57.58:8090\"",
        )
    }

    buildTypes {
        debug {
            /*
             * Geliştirme sürümünde ağ loglarının açık
             * olup olmadığını Kotlin tarafından kontrol
             * edebilmek için özel alan.
             */
            buildConfigField(
                "Boolean",
                "ENABLE_HTTP_LOGGING",
                "true",
            )
        }

        release {
            isMinifyEnabled = false

            /*
             * Release sürümünde hassas HTTP gövdelerini
             * Logcat üzerinde göstermiyoruz.
             */
            buildConfigField(
                "Boolean",
                "ENABLE_HTTP_LOGGING",
                "false",
            )

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt",
                ),
                "proguard-rules.pro",
            )
        }
    }

    /*
     * BuildConfig sınıfının oluşturulmasını açar.
     *
     * Backend adreslerini BuildConfig üzerinden
     * kullandığımız için bu özellik gereklidir.
     */
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility =
            JavaVersion.VERSION_11

        targetCompatibility =
            JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    /*
     * =====================================================
     * TEMEL ANDROID BAĞIMLILIKLARI
     * =====================================================
     */

    implementation(
        libs.androidx.core.ktx,
    )

    implementation(
        libs.androidx.appcompat,
    )

    implementation(
        libs.material,
    )

    implementation(
        libs.androidx.activity,
    )

    implementation(
        libs.androidx.constraintlayout,
    )


    /*
     * =====================================================
     * RETROFIT
     * =====================================================
     *
     * .NET ve Python REST API endpointlerine istek
     * göndermek için kullanılacaktır.
     */

    implementation(
        "com.squareup.retrofit2:retrofit:2.11.0",
    )

    /*
     * Retrofit cevaplarını Gson ile Kotlin veri
     * sınıflarına dönüştürür.
     */
    implementation(
        "com.squareup.retrofit2:converter-gson:2.11.0",
    )


    /*
     * =====================================================
     * OKHTTP VE WEBSOCKET
     * =====================================================
     *
     * Retrofit zaten OkHttp kullanır. Bu bağımlılığı
     * ayrıca ekleyerek doğrudan WebSocket istemcisi
     * oluşturabileceğiz.
     */

    implementation(
        "com.squareup.okhttp3:okhttp:4.12.0",
    )

    /*
     * Geliştirme sırasında gönderilen HTTP isteklerini,
     * response kodlarını ve JSON gövdelerini Logcat
     * üzerinden görebilmemizi sağlar.
     */
    implementation(
        "com.squareup.okhttp3:logging-interceptor:4.12.0",
    )


    /*
     * =====================================================
     * KOTLIN COROUTINES
     * =====================================================
     *
     * Ağ isteklerini ana ekranı dondurmadan çalıştırmak
     * için kullanılır.
     */

    implementation(
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0",
    )


    /*
     * =====================================================
     * LIFECYCLE VE VIEWMODEL
     * =====================================================
     *
     * Login, cihaz kaydı ve challenge ekranlarının
     * durumlarını ViewModel üzerinden yöneteceğiz.
     */

    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7",
    )

    implementation(
        "androidx.lifecycle:lifecycle-livedata-ktx:2.8.7",
    )

    implementation(
        "androidx.lifecycle:lifecycle-runtime-ktx:2.8.7",
    )

    /*
     * Activity içerisinde lifecycleScope kullanmayı
     * destekler.
     */
    implementation(
        "androidx.activity:activity-ktx:1.10.1",
    )


    /*
     * =====================================================
     * DATASTORE
     * =====================================================
     *
     * Şu bilgileri cihazda saklamak için kullanacağız:
     *
     * - .NET access token
     * - Python device access token
     * - installation ID
     * - device public ID
     * - kullanıcı bilgileri
     */

    implementation(
        "androidx.datastore:datastore-preferences:1.1.2",
    )


    /*
     * =====================================================
     * ANDROID SECURITY CRYPTO
     * =====================================================
     *
     * Cihaz tokenı gibi hassas bilgilerin şifrelenmiş
     * şekilde saklanabilmesi için kullanılır.
     *
     * İleride Android Keystore ile private key
     * oluşturma işleminde de güvenlik altyapısından
     * yararlanacağız.
     */
    implementation(
        "androidx.security:security-crypto:1.1.0-alpha06",
    )


    /*
     * =====================================================
     * TEST BAĞIMLILIKLARI
     * =====================================================
     */

    testImplementation(
        libs.junit,
    )

    androidTestImplementation(
        libs.androidx.junit,
    )

    androidTestImplementation(
        libs.androidx.espresso.core,
    )
}