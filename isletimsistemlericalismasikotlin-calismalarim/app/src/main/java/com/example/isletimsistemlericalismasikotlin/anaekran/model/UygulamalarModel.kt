package com.example.isletimsistemlericalismasikotlin.anaekran.model

import java.io.Serializable

data class UygulamalarModel(
    var uygulamaAdi:String,
    var uygulamaResimAdi:String,
    var navId:Int,
    var copKutusundaMi:Boolean
):Serializable