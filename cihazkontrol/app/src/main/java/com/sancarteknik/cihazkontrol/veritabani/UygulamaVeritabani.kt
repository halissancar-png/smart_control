package com.sancarteknik.cihazkontrol.veritabani

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Mekan::class, Cihaz::class],
    version = 2,
    exportSchema = false
)
abstract class UygulamaVeritabani : RoomDatabase() {

    abstract fun mekanDao(): MekanDao
    abstract fun cihazDao(): CihazDao

    companion object {

        @Volatile
        private var INSTANCE: UygulamaVeritabani? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE mekanlar ADD COLUMN sira INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE mekanlar ADD COLUMN webId INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE cihazlar ADD COLUMN cihazTipi TEXT NOT NULL DEFAULT 'O'")
                database.execSQL("ALTER TABLE cihazlar ADD COLUMN kanalSayisi INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE cihazlar ADD COLUMN durum TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE cihazlar ADD COLUMN bekleyenKomut TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE cihazlar ADD COLUMN sonGorulme INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE cihazlar ADD COLUMN sira INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE cihazlar ADD COLUMN kanalAciklama TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE cihazlar ADD COLUMN webId INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getir(context: Context): UygulamaVeritabani {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UygulamaVeritabani::class.java,
                    "cihazkontrol.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}