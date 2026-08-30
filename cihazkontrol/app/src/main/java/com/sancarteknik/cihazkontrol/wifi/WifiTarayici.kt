package com.sancarteknik.cihazkontrol.wifi

import android.content.Context
import android.net.wifi.WifiManager

class WifiTarayici(
    private val context: Context
) {

    private val wifiManager =
        context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager

    fun tara(): List<String> {

        wifiManager.startScan()

        return wifiManager.scanResults
            .map { it.SSID }
            .filter { it.isNotBlank() }
            .distinct()
    }
}