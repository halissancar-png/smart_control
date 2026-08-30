package com.sancarteknik.cihazkontrol.veritabani

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MekanDao {

    @Insert
    suspend fun ekle(
        mekan: Mekan
    ): Long

    @Update
    suspend fun guncelle(
        mekan: Mekan
    )

    @Delete
    suspend fun sil(
        mekan: Mekan
    )

    @Query(
        "SELECT * FROM mekanlar ORDER BY isim"
    )
    suspend fun tumunuGetir(): List<Mekan>

    @Query(
        "SELECT * FROM mekanlar WHERE id = :id LIMIT 1"
    )
    suspend fun getir(
        id: Int
    ): Mekan?
}