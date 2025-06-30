package com.example.qrkodolusturucujetpack.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Urun(
    @SerializedName("id") @Expose var id: Int,
    @SerializedName("urun_ad") @Expose var urunAd: String,
    @SerializedName("urun_resim") @Expose var urunResim: String,
    @SerializedName("urun_aciklama") @Expose var urunAciklama: String,
    @SerializedName("urun_kategori_id") @Expose var urunKategoriId: Int,
    @SerializedName("urun_indirim") @Expose var urunIndirim: Int,
    @SerializedName("urun_fiyat") @Expose var urunFiyat: Double,
    @SerializedName("urun_indirimli_fiyat") @Expose var urunIndirimliFiyat: Double
): Serializable
