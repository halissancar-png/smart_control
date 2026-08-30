package com.sancarteknik.cihazkontrol.model

import com.google.gson.annotations.SerializedName

data class CihazWeb(
    @SerializedName("id") val id: Int,
    @SerializedName("cihaz_adi") val cihazAdi: String,
    @SerializedName("cihaz_kodu") val cihazKodu: String,
    @SerializedName("cihaz_tipi") val cihazTipi: String,
    @SerializedName("kanal_sayisi") val kanalSayisi: Int,
    @SerializedName("mekan_id") val mekanId: Int,
    @SerializedName("aktif") val aktif: Int,
    @SerializedName("deger") val deger: String? = null,
    @SerializedName("komut") val komut: String? = null,
    @SerializedName("son_gorulme") val sonGorulme: String? = null
)

data class MekanWeb(
    @SerializedName("id") val id: Int,
    @SerializedName("isim") val isim: String
)