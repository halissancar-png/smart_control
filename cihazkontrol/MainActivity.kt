
@file:OptIn(ExperimentalMaterial3Api::class)

package com.sancarteknik.cihazkontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import com.sancarteknik.cihazkontrol.ekranlar.*
import com.sancarteknik.cihazkontrol.veritabani.Mekan
import com.sancarteknik.cihazkontrol.wifi.UdpHaberlesme


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // UDP haberlesme merkezini uygulama baslarken baslat
        UdpHaberlesme.baslat()

        setContent {
            Uygulama()
        }
    }

    override fun onDestroy() {
        UdpHaberlesme.durdur()
        super.onDestroy()
    }
}


@Composable
fun Uygulama() {

    var sayfa by remember {
        mutableStateOf("ana")
    }

    var secilenMekan by remember {
        mutableStateOf<Mekan?>(null)
    }

    // ==================================================
    // MEKANLAR SAYFASININ GERI DONUS HEDEFI
    //
    // Ana sayfadan acildiysa -> ana
    // Ayarlardan acildiysa -> ayarlar
    // ==================================================

    var mekanlarGeriSayfasi by remember {
        mutableStateOf("ana")
    }


    // ==================================================
    // TELEFON GERI TUSU
    // ==================================================

    BackHandler(
        enabled = sayfa != "ana"
    ) {

        when (sayfa) {

            // ------------------------------------------
            // AYARLAR
            // ------------------------------------------

            "ayarlar" -> {
                sayfa = "ana"
            }


            // ------------------------------------------
            // MEKANLAR
            // ------------------------------------------

            "mekanlar" -> {
                sayfa = mekanlarGeriSayfasi
            }


            // ------------------------------------------
            // CIHAZLAR
            // ------------------------------------------

            "cihazlar" -> {
                sayfa = "ayarlar"
            }


            // ------------------------------------------
            // CIHAZ EKLE
            // ------------------------------------------

            "cihaz_ekle" -> {
                sayfa = "cihazlar"
            }


            // ------------------------------------------
            // VERI TAKIBI
            // ------------------------------------------

            "veri_takibi" -> {
                sayfa = "ayarlar"
            }


            // ------------------------------------------
            // MEKAN KONTROL
            // ------------------------------------------

            "mekan_kontrol" -> {
                sayfa = "ana"
            }
        }
    }


    // ==================================================
    // SAYFALAR
    // ==================================================

    when (sayfa) {

        // ==================================================
        // ANA SAYFA
        // ==================================================

        "ana" -> AnaSayfa(

            mekanlar = {

                // Ana sayfadan mekanlara giriyoruz
                mekanlarGeriSayfasi = "ana"

                sayfa = "mekanlar"
            },

            mekanKontrol = { mekan ->

                secilenMekan = mekan

                sayfa = "mekan_kontrol"
            },

            cihazlar = {
                sayfa = "cihazlar"
            },

            veriTakibi = {
                sayfa = "veri_takibi"
            },

            ayarlar = {
                sayfa = "ayarlar"
            }
        )


        // ==================================================
        // MEKANLAR
        // ==================================================

        "mekanlar" -> MekanlarSayfasi(

            geri = {

                // Mekanlar nereden acildiysa oraya don
                sayfa = mekanlarGeriSayfasi
            },

            mekanKontrol = { mekan ->

                secilenMekan = mekan

                sayfa = "mekan_kontrol"
            }
        )


        // ==================================================
        // CIHAZLAR
        // ==================================================

        "cihazlar" -> CihazlarSayfasi(

            geri = {
                sayfa = "ayarlar"
            },

            cihazEkle = {
                sayfa = "cihaz_ekle"
            }
        )


        // ==================================================
        // CIHAZ EKLE
        // ==================================================

        "cihaz_ekle" -> CihazEkleSayfasi(

            geri = {
                sayfa = "cihazlar"
            }
        )


        // ==================================================
        // VERI TAKIBI
        // ==================================================

        "veri_takibi" -> UdpMesajlasmaSayfasi(

            geri = {
                sayfa = "ayarlar"
            }
        )


        // ==================================================
        // AYARLAR
        // ==================================================

        "ayarlar" -> AyarlarSayfasi(

            geri = {
                sayfa = "ana"
            },

            mekanlar = {

                // Ayarlardan mekan yonetimine giriyoruz
                mekanlarGeriSayfasi = "ayarlar"

                sayfa = "mekanlar"
            },

            cihazlar = {
                sayfa = "cihazlar"
            },

            veriTakibi = {
                sayfa = "veri_takibi"
            }
        )


        // ==================================================
        // MEKAN KONTROL
        // ==================================================

        "mekan_kontrol" -> {

            secilenMekan?.let { mekan ->

                MekanKontrolSayfasi(

                    mekan = mekan,

                    geri = {
                        sayfa = "ana"
                    }
                )
            }
        }
    }
}
