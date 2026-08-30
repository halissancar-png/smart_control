package com.sancarteknik.cihazkontrol.veritabani

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mekanlar")
data class Mekan(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val isim: String
)

