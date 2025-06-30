package com.example.isletimsistemlericalismasikotlin.uygulamalar.notdefteri.model

import java.io.Serializable

data class Not(
    var id:Int,
    var baslik:String,
    var notMetin:String,
    var listName:String,
    var imageUrl:String,
    var tarih:String,
    var renk:String
):Serializable
