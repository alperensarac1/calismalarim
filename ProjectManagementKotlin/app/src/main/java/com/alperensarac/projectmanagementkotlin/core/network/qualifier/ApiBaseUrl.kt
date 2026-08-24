package com.alperensarac.projectmanagementkotlin.core.network.qualifier

import javax.inject.Qualifier

/**
 * Hilt içerisinde API adresini temsil eden String bağımlılığını,
 * diğer String bağımlılıklarından ayırmak için kullanılır.
 *
 * Örneğin ileride uygulamada birden fazla String dependency bulunabilir:
 *
 * - API base URL
 * - Dosya sunucu adresi
 * - Uygulama adı
 *
 * Hilt, @ApiBaseUrl sayesinde hangi String değerinin Retrofit'e
 * verilmesi gerektiğini anlayacaktır.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiBaseUrl