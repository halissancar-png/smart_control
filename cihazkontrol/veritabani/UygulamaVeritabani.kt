package com.sancarteknik.cihazkontrol.veritabani

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Mekan::class,
        Cihaz::class
    ],
    version = 1,
    exportSchema = false
)
abstract class UygulamaVeritabani : RoomDatabase() {

    abstract fun mekanDao(): MekanDao

    abstract fun cihazDao(): CihazDao

    companion object {

        @Volatile
        private var INSTANCE: UygulamaVeritabani? = null

        fun getir(
            context: Context
        ): UygulamaVeritabani {

            return INSTANCE
                ?: synchronized(this) {

                    val instance =
                        Room.databaseBuilder(
                            context.applicationContext,
                            UygulamaVeritabani::class.java,
                            "cihazkontrol.db"
                        )
                            .build()

                    INSTANCE = instance

                    instance
                }
        }
    }
}