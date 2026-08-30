@file:OptIn(ExperimentalMaterial3Api::class)

package com.sancarteknik.cihazkontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import com.sancarteknik.cihazkontrol.ekranlar.*
import com.sancarteknik.cihazkontrol.utils.SharedPrefs
import com.sancarteknik.cihazkontrol.veritabani.Mekan
import com.sancarteknik.cihazkontrol.wifi.UdpHaberlesme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPrefs.init(applicationContext)
        UdpHaberlesme.baslat()
        setContent { Uygulama() }
    }

    override fun onDestroy() {
        UdpHaberlesme.durdur()
        super.onDestroy()
    }
}

@Composable
fun Uygulama() {

    var sayfa by remember { mutableStateOf("ana") }
    var secilenMekan by remember { mutableStateOf<Mekan?>(null) }
    var mekanlarGeriSayfasi by remember { mutableStateOf("ana") }

    BackHandler(enabled = sayfa != "ana") {
        when (sayfa) {
            "ayarlar" -> sayfa = "ana"
            "mekanlar" -> sayfa = mekanlarGeriSayfasi
            "cihazlar" -> sayfa = "ayarlar"
            "cihaz_ekle" -> sayfa = "cihazlar"
            "veri_takibi" -> sayfa = "ayarlar"
            "mekan_kontrol" -> sayfa = "ana"
            "giris" -> sayfa = "ana"
            "log" -> sayfa = "ayarlar"
            "senkronizasyon" -> sayfa = "ayarlar"
        }
    }

    // ============================================
    // SAYFA KONTROLÜ
    // ============================================

    // ✅ Kullanıcı giriş yapmamışsa direkt giris sayfasına git
    if (!SharedPrefs.isLogin() && sayfa != "giris") {
        sayfa = "giris"
    }

    // ============================================
    // SAYFALAR
    // ============================================

    when (sayfa) {

        "giris" -> {
            GirisSayfasi(
                onLoginSuccess = {
                    sayfa = "ana"
                }
            )
        }

        "ana" -> {
            AnaSayfa(
                mekanlar = {
                    mekanlarGeriSayfasi = "ana"
                    sayfa = "mekanlar"
                },
                mekanKontrol = { mekan ->
                    secilenMekan = mekan
                    sayfa = "mekan_kontrol"
                },
                cihazlar = { sayfa = "cihazlar" },
                veriTakibi = { sayfa = "veri_takibi" },
                ayarlar = { sayfa = "ayarlar" }
            )
        }

        "mekanlar" -> {
            MekanlarSayfasi(
                geri = { sayfa = mekanlarGeriSayfasi },
                mekanKontrol = { mekan ->
                    secilenMekan = mekan
                    sayfa = "mekan_kontrol"
                }
            )
        }

        "cihazlar" -> {
            CihazlarSayfasi(
                geri = { sayfa = "ayarlar" },
                cihazEkle = { sayfa = "cihaz_ekle" }
            )
        }

        "cihaz_ekle" -> {
            CihazEkleSayfasi(geri = { sayfa = "cihazlar" })
        }

        "veri_takibi" -> {
            UdpMesajlasmaSayfasi(geri = { sayfa = "ayarlar" })
        }

        "ayarlar" -> {
            AyarlarSayfasi(
                geri = { sayfa = "ana" },
                mekanlar = {
                    mekanlarGeriSayfasi = "ayarlar"
                    sayfa = "mekanlar"
                },
                cihazlar = { sayfa = "cihazlar" },
                veriTakibi = { sayfa = "veri_takibi" },
                log = { sayfa = "log" },
                senkronizasyon = { sayfa = "senkronizasyon" }
            )
        }

        "mekan_kontrol" -> {
            secilenMekan?.let { mekan ->
                MekanKontrolSayfasi(
                    mekan = mekan,
                    geri = { sayfa = "ana" }
                )
            }
        }

        "log" -> {
            LogSayfasi(geri = { sayfa = "ayarlar" })
        }

        "senkronizasyon" -> {
            SenkronizasyonSayfasi(geri = { sayfa = "ayarlar" })
        }
    }
}