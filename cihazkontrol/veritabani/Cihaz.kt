package com.sancarteknik.cihazkontrol.veritabani

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cihazlar")
data class Cihaz(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val teknikId: String,

    val cihazAdi: String,

    val wifiSSID: String,

    val ip: String,

    val mekanId: Int,

    val aktif: Boolean = true
)

