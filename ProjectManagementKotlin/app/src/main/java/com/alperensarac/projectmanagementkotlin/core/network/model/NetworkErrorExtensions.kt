package com.alperensarac.projectmanagementkotlin.core.network.model

/**
 * NetworkError modelini kullanıcıya gösterilebilecek mesaja dönüştürür.
 */
fun NetworkError.toUserMessage(): String {
    return when (this) {
        is NetworkError.Validation -> message
        is NetworkError.Unauthorized -> message
        is NetworkError.Forbidden -> message
        is NetworkError.NotFound -> message
        is NetworkError.Conflict -> message
        is NetworkError.PayloadTooLarge -> message
        is NetworkError.UnsupportedMediaType -> message
        is NetworkError.TooManyRequests -> message
        is NetworkError.Server -> message
        is NetworkError.NoConnection -> message
        is NetworkError.Timeout -> message
        is NetworkError.Serialization -> message
        is NetworkError.Unknown -> message
    }
}