// Proje seviyesindeki Gradle eklentileri burada tanımlanır.
//
// "apply false" kullanıldığı için eklentiler kök projeye uygulanmaz.
// İlgili modül, ihtiyaç duyduğu eklentiyi kendi build.gradle.kts
// dosyasında etkinleştirir.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt.android) apply false
}