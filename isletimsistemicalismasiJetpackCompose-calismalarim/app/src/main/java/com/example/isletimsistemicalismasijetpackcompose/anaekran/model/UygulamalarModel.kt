package com.example.isletimsistemicalismasijetpackcompose.anaekran.model

import java.io.Serializable

data class UygulamalarModel(
    var uygulamaAdi:String,
    var uygulamaResimAdi:String,
    var gecisId:String,
    var copKutusundaMi:Boolean
): Serializable