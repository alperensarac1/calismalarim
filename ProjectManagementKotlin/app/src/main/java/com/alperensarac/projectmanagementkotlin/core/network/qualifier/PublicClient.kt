package com.alperensarac.projectmanagementkotlin.core.network.qualifier

import javax.inject.Qualifier

/**
 * Authorization interceptor ve TokenAuthenticator içermeyen
 * OkHttpClient bağımlılığını işaretler.
 *
 * Refresh isteğinin normal authenticated client üzerinden gönderilmesi,
 * refresh endpoint'i de 401 döndürdüğünde sonsuz authenticator döngüsüne
 * yol açabilir.
 *
 * Bu nedenle refresh işlemi tamamen ayrı bir public client kullanır.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PublicClient