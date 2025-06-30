package com.example.isletimsistemlericalismasikotlin.uygulamalar.rehber.model

import java.io.Serializable

data class RehberKisiler(
    var kisi_id:Int,
    var kisi_isim:String,
    var kisi_numara:String
):Serializable