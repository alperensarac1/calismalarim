package com.alperensarac.projectmanagementkotlin

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Uygulamanın ana Application sınıfıdır.
 *
 * @HiltAndroidApp anotasyonu:
 *
 * 1. Hilt'in uygulama seviyesindeki dependency injection altyapısını başlatır.
 * 2. Uygulama yaşam döngüsü boyunca kullanılacak bağımlılık grafiğini oluşturur.
 * 3. Activity, Fragment, ViewModel ve diğer Android bileşenlerinde
 *    Hilt kullanabilmemizi sağlar.
 *
 * Bu sınıf AndroidManifest.xml içerisinde android:name alanına
 * mutlaka tanımlanmalıdır.
 */
@HiltAndroidApp
class ProjectManagementApplication : Application()