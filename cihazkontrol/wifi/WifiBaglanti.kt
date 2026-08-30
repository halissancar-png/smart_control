package com.sancarteknik.cihazkontrol.wifi

import android.content.Context
import android.content.Intent
import android.provider.Settings

class WifiBaglanti(
    private val context: Context
) {

    fun baglan(
        ssid: String,
        sonuc: (Boolean) -> Unit
    ) {

        try {

            val intent =
                Intent(Settings.ACTION_WIFI_SETTINGS)

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK

            context.startActivity(intent)

            sonuc(true)

        } catch (e: Exception) {

            sonuc(false)
        }
    }
}