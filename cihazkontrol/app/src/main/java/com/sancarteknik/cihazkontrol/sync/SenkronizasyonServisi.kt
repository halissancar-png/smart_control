package com.sancarteknik.cihazkontrol.sync

import android.content.Context
import android.util.Log
import com.sancarteknik.cihazkontrol.api.ApiClient
import com.sancarteknik.cihazkontrol.veritabani.Cihaz
import com.sancarteknik.cihazkontrol.veritabani.Mekan
import com.sancarteknik.cihazkontrol.veritabani.UygulamaVeritabani
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SenkronizasyonServisi(private val context: Context) {

    private val veritabani = UygulamaVeritabani.getir(context)
    private val api = ApiClient.service

    companion object {
        private const val TAG = "Senkronizasyon"
    }

    suspend fun mekanlariSenkronizeEt(kullaniciId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Mekanlar senkronize ediliyor...")
                val response = api.mekanlariGetir(kullaniciId)
                if (response.isSuccessful) {
                    val webMekanlar = response.body() ?: emptyList()
                    veritabani.mekanDao().hepsiniSil()
                    webMekanlar.forEach { webMekan ->
                        val mekan = Mekan(
                            id = 0,
                            isim = webMekan.isim,
                            sira = 0,
                            webId = webMekan.id
                        )
                        veritabani.mekanDao().ekle(mekan)
                    }
                    Log.d(TAG, "${webMekanlar.size} mekan senkronize edildi.")
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Mekan senkronizasyon hatası", e)
                false
            }
        }
    }

    suspend fun cihazlariSenkronizeEt(kullaniciId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Cihazlar senkronize ediliyor...")
                val response = api.cihazlariGetir(kullaniciId)
                if (response.isSuccessful) {
                    val webCihazlar = response.body() ?: emptyList()
                    veritabani.cihazDao().tumunuGetir().forEach { cihaz ->
                        veritabani.cihazDao().sil(cihaz)
                    }
                    webCihazlar.forEach { webCihaz ->
                        val cihaz = Cihaz(
                            teknikId = webCihaz.cihazKodu,
                            cihazAdi = webCihaz.cihazAdi,
                            wifiSSID = "",
                            ip = "",
                            mekanId = webCihaz.mekanId,
                            aktif = webCihaz.aktif == 1,
                            cihazTipi = webCihaz.cihazTipi,
                            kanalSayisi = webCihaz.kanalSayisi,
                            durum = webCihaz.deger ?: "",
                            bekleyenKomut = webCihaz.komut ?: "",
                            sonGorulme = 0,
                            sira = 0,
                            kanalAciklama = "",
                            webId = webCihaz.id
                        )
                        veritabani.cihazDao().ekle(cihaz)
                    }
                    Log.d(TAG, "${webCihazlar.size} cihaz senkronize edildi.")
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Cihaz senkronizasyon hatası", e)
                false
            }
        }
    }

    suspend fun tamSenkronizasyon(kullaniciId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val mekanSonuc = mekanlariSenkronizeEt(kullaniciId)
                val cihazSonuc = cihazlariSenkronizeEt(kullaniciId)
                mekanSonuc && cihazSonuc
            } catch (e: Exception) {
                Log.e(TAG, "Tam senkronizasyon hatası", e)
                false
            }
        }
    }
}