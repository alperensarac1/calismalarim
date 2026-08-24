import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
}

/*
 * local.properties dosyasını okuyoruz.
 *
 * Bu dosya genellikle Git'e gönderilmez. Böylece her geliştirici
 * kendi bilgisayarına veya cihazına uygun backend adresini kullanabilir.
 */
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")

    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { inputStream ->
            load(inputStream)
        }
    }
}

/*
 * local.properties içerisinde PM_API_BASE_URL bulunamazsa
 * gerçek Android cihaz için yerel ağ adresi varsayılan olarak kullanılır.
 */
val debugApiBaseUrl = localProperties.getProperty(
    "PM_API_BASE_URL",
    "http://10.159.57.58:8080/"
)

android {
    namespace = "com.alperensarac.projectmanagementkotlin"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.alperensarac.projectmanagementkotlin"

        minSdk = 24
        targetSdk = 35

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isDebuggable = true

            /*
             * BuildConfig yerine Android string resource üretiyoruz.
             *
             * Kod tarafında bu değer:
             *
             * context.getString(R.string.api_base_url)
             *
             * şeklinde okunacaktır.
             */
            resValue(
                type = "string",
                name = "api_base_url",
                value = debugApiBaseUrl
            )
        }

        release {
            isMinifyEnabled = false

            /*
             * Üretim backend adresi hazır olduğunda bu değer
             * gerçek HTTPS adresiyle değiştirilecektir.
             */
            resValue(
                type = "string",
                name = "api_base_url",
                value = "https://api.example.com/"
            )

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        /*
         * XML layout dosyalarından ViewBinding sınıfları üretilir.
         */
        viewBinding = true

        /*
         * Artık BuildConfig kullanmadığımız için buildConfig ayarını
         * etkinleştirmemize gerek yoktur.
         */
        buildConfig = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
                "META-INF/DEPENDENCIES"
            )
        }
    }

    lint {
        abortOnError = true
        checkReleaseBuilds = true
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    // AndroidX temel bileşenleri
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefreshlayout)

    // Lifecycle ve ViewModel
    implementation(libs.bundles.lifecycle)

    // Navigation Component
    implementation(libs.bundles.navigation)

    // Retrofit ve OkHttp
    implementation(libs.bundles.network)
    implementation(libs.kotlinx.serialization.json)

    // Kotlin Coroutines
    implementation(libs.bundles.coroutines)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Paging 3
    implementation(libs.androidx.paging.runtime.ktx)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Coil
    implementation(libs.coil)

    // Unit test
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.arch.core.testing)

    // Android instrumented test
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}