package com.sancarteknik.cihazkontrol.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPrefs {

    private const val PREF_NAME = "cihazkontrol_prefs"
    private const val KEY_KULLANICI_ID = "kullanici_id"
    private const val KEY_KULLANICI_ADI = "kullanici_adi"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun setKullanici(id: Int, isim: String) {
        prefs.edit().apply {
            putInt(KEY_KULLANICI_ID, id)
            putString(KEY_KULLANICI_ADI, isim)
            apply()
        }
    }

    fun getKullaniciId(): Int {
        return prefs.getInt(KEY_KULLANICI_ID, 0)
    }

    fun getKullaniciAdi(): String {
        return prefs.getString(KEY_KULLANICI_ADI, "") ?: ""
    }

    fun isLogin(): Boolean {
        return getKullaniciId() > 0
    }

    fun cikisYap() {
        prefs.edit().clear().apply()
    }
}
