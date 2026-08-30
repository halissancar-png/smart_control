package com.sancarteknik.cihazkontrol.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log

class WifiTarayici(
    private val context: Context
) {

    private val wifiManager =
        context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val appContext =
        context.applicationContext


    // ==================================================
    // AG TARA
    //
    // DIKKAT:
    //
    // wifiManager.startScan() ASENKRON calisir.
    // Tarama birkac saniye surebilir; cagirir cagirmaz
    // scanResults okumak ESKI/onbellekteki sonuclari
    // dondurur.
    //
    // Bu yuzden SCAN_RESULTS_AVAILABLE_ACTION yayinini
    // dinleyip, sonuc gercekten hazir oldugunda callback'i
    // cagiriyoruz.
    // ==================================================

    fun tara(
        sonuc: (List<String>) -> Unit
    ) {

        var alindi = false

        val receiver =
            object : BroadcastReceiver() {

                override fun onReceive(
                    ctx: Context,
                    intent: Intent
                ) {

                    if (alindi) {
                        return
                    }

                    alindi = true

                    try {
                        appContext.unregisterReceiver(this)
                    } catch (e: Exception) {
                        Log.e("WifiTarayici", "Receiver kaldirilamadi", e)
                    }

                    sonuc(mevcutSonuclariOku())
                }
            }

        val filtre =
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(
                receiver,
                filtre,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            appContext.registerReceiver(receiver, filtre)
        }

        val baslatildi =
            wifiManager.startScan()

        if (!baslatildi) {

            // --------------------------------------
            // TARAMA BASLATILAMADI
            //
            // (Android taramayi kisitlamis olabilir -
            // gunde birkac tarama hakki gibi). Bu
            // durumda elimizdeki en son onbellek
            // sonucuyla devam ediyoruz.
            // --------------------------------------

            if (!alindi) {

                alindi = true

                try {
                    appContext.unregisterReceiver(receiver)
                } catch (e: Exception) {
                    Log.e("WifiTarayici", "Receiver kaldirilamadi", e)
                }

                sonuc(mevcutSonuclariOku())
            }
        }
    }

    private fun mevcutSonuclariOku(): List<String> {

        return wifiManager.scanResults
            .map { it.SSID }
            .filter { it.isNotBlank() }
            .distinct()
    }
}
