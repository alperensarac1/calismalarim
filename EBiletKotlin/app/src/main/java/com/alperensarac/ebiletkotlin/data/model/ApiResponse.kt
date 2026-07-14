package com.alperensarac.ebiletkotlin.data.model
/*
    ApiResponse

    PHP backend tarafında bütün cevapları aynı formatta döndürmüştük.

    Örnek başarılı cevap:

    {
        "success": true,
        "message": "Giriş başarılı",
        "data": {
            ...
        }
    }

    Örnek hatalı cevap:

    {
        "success": false,
        "message": "E-posta veya şifre hatalı."
    }

    T generic yapı demektir.

    Yani data bazen User olabilir,
    bazen List<City>,
    bazen Event,
    bazen Ticket olabilir.
*/
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,

    /*
        Backend error_response içinde extra döndürebiliyor.
        Şimdilik Any? olarak tutuyoruz.

        İleride daha profesyonel hale getirip ayrı model yapabiliriz.
    */
    val extra: Any? = null
)