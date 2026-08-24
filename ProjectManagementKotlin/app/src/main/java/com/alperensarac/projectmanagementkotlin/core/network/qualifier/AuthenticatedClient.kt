package com.alperensarac.projectmanagementkotlin.core.network.qualifier

import javax.inject.Qualifier

/**
 * JWT access token interceptor ve TokenAuthenticator içeren
 * OkHttpClient bağımlılığını işaretler.
 *
 * Korunan backend endpointleri bu istemci üzerinden çağrılır.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedClient