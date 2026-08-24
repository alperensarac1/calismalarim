package com.alperensarac.projectmanagementkotlin.core.network.qualifier

import javax.inject.Qualifier

/**
 * Authenticator içermeyen public OkHttpClient ile oluşturulmuş
 * Retrofit nesnesini işaretler.
 *
 * Özellikle token refresh isteği için kullanılacaktır.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PublicRetrofit