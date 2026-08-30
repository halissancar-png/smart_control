package com.sancarteknik.cihazkontrol.veritabani

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CihazDao {

    @Insert
    suspend fun ekle(
        cihaz: Cihaz
    ): Long

    @Update
    suspend fun guncelle(
        cihaz: Cihaz
    )

    @Delete
    suspend fun sil(
        cihaz: Cihaz
    )

    @Query(
        "SELECT * FROM cihazlar ORDER BY cihazAdi"
    )
    suspend fun tumunuGetir(): List<Cihaz>

    @Query(
        """
        SELECT * FROM cihazlar
        WHERE mekanId = :mekanId
        ORDER BY cihazAdi
        """
    )
    suspend fun mekandakiCihazlar(
        mekanId: Int
    ): List<Cihaz>

    @Query(
        """
        SELECT * FROM cihazlar
        WHERE teknikId = :teknikId
        LIMIT 1
        """
    )
    suspend fun teknikIdIleGetir(
        teknikId: String
    ): Cihaz?
}