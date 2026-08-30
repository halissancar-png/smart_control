package com.sancarteknik.cihazkontrol.veritabani

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CihazDao {

    @Insert
    suspend fun ekle(cihaz: Cihaz): Long

    @Update
    suspend fun guncelle(cihaz: Cihaz)

    @Delete
    suspend fun sil(cihaz: Cihaz)

    @Query("SELECT * FROM cihazlar ORDER BY cihazAdi")
    suspend fun tumunuGetir(): List<Cihaz>

    @Query("SELECT * FROM cihazlar WHERE mekanId = :mekanId ORDER BY sira ASC, cihazAdi")
    suspend fun mekandakiCihazlar(mekanId: Int): List<Cihaz>

    @Query("SELECT * FROM cihazlar WHERE teknikId = :teknikId LIMIT 1")
    suspend fun teknikIdIleGetir(teknikId: String): Cihaz?

    @Query("SELECT * FROM cihazlar WHERE webId = :webId LIMIT 1")
    suspend fun webIdIleGetir(webId: Int): Cihaz?

    @Query("DELETE FROM cihazlar WHERE mekanId = :mekanId")
    suspend fun mekandakiCihazlariSil(mekanId: Int)

    @Query("UPDATE cihazlar SET durum = :durum, sonGorulme = :zaman WHERE teknikId = :teknikId")
    suspend fun durumGuncelle(teknikId: String, durum: String, zaman: Long)

    @Query("UPDATE cihazlar SET bekleyenKomut = :komut WHERE teknikId = :teknikId")
    suspend fun bekleyenKomutGuncelle(teknikId: String, komut: String)
}