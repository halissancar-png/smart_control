package com.sancarteknik.cihazkontrol.api

import com.sancarteknik.cihazkontrol.model.CihazWeb
import com.sancarteknik.cihazkontrol.model.MekanWeb
import retrofit2.Response
import retrofit2.http.*

interface WebApiService {

    @FormUrlEncoded
    @POST("api/android/login.php")
    suspend fun login(
        @Field("isim") isim: String,
        @Field("sifre") sifre: String
    ): Response<String>

    @GET("api/android/mekanlar.php")
    suspend fun mekanlariGetir(
        @Query("kullanici_id") kullaniciId: Int
    ): Response<List<MekanWeb>>

    @FormUrlEncoded
    @POST("api/android/mekan_ekle.php")
    suspend fun mekanEkle(
        @Field("kullanici_id") kullaniciId: Int,
        @Field("isim") isim: String
    ): Response<String>

    @FormUrlEncoded
    @POST("api/android/mekan_sil.php")
    suspend fun mekanSil(
        @Field("kullanici_id") kullaniciId: Int,
        @Field("mekan_id") mekanId: Int
    ): Response<String>

    @GET("api/android/cihazlar.php")
    suspend fun cihazlariGetir(
        @Query("kullanici_id") kullaniciId: Int
    ): Response<List<CihazWeb>>

    @FormUrlEncoded
    @POST("api/android/cihaz_ekle.php")
    suspend fun cihazEkle(
        @Field("kullanici_id") kullaniciId: Int,
        @Field("mekan_id") mekanId: Int,
        @Field("cihaz_adi") cihazAdi: String,
        @Field("cihaz_kodu") cihazKodu: String,
        @Field("cihaz_tipi") cihazTipi: String,
        @Field("kanal_sayisi") kanalSayisi: Int
    ): Response<String>

    @FormUrlEncoded
    @POST("api/android/cihaz_sil.php")
    suspend fun cihazSil(
        @Field("kullanici_id") kullaniciId: Int,
        @Field("cihaz_id") cihazId: Int
    ): Response<String>

    @FormUrlEncoded
    @POST("api/android/komut.php")
    suspend fun komutGonder(
        @Field("id") cihazId: Int,
        @Field("kanal") kanal: Int,
        @Field("deger") deger: Int,
        @Field("kullanici_id") kullaniciId: Int
    ): Response<String>
}