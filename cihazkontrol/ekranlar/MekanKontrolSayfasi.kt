@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.sancarteknik.cihazkontrol.ekranlar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sancarteknik.cihazkontrol.veritabani.Cihaz
import com.sancarteknik.cihazkontrol.veritabani.Mekan
import com.sancarteknik.cihazkontrol.veritabani.UygulamaVeritabani

import com.sancarteknik.cihazkontrol.wifi.UdpHaberlesme
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope




@Composable
fun MekanKontrolSayfasi(
    mekan: Mekan,
    geri: () -> Unit
) {

    val context = LocalContext.current

    val veritabani =
        remember {
            UygulamaVeritabani.getir(context)
        }

    var cihazlar by remember {
        mutableStateOf<List<Cihaz>>(emptyList())
    }

    // ==================================================
    // MEKANA AIT CIHAZLARI GETIR
    // ==================================================

    LaunchedEffect(mekan.id) {

        cihazlar =
            veritabani
                .cihazDao()
                .tumunuGetir()
                .filter {
                    it.mekanId == mekan.id
                }
    }

    // ==================================================
    // EKRAN
    // ==================================================

    Scaffold(

        topBar = {

            UstBaslik(
                baslik = "Mekan Yönetimi",
                geri = geri
            )
        }

    ) { padding ->

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
        ) {

            Text(

                text =
                    "Kontrol Paneli",

                style =
                    MaterialTheme
                        .typography
                        .headlineSmall
            )

            Spacer(
                modifier =
                    Modifier.padding(6.dp)
            )

            Text(
                text =
                    "Mekan: ${mekan.isim}"
            )

            Spacer(
                modifier =
                    Modifier.padding(8.dp)
            )

            if (cihazlar.isEmpty()) {

                Text(
                    text =
                        "Bu mekana kayitli cihaz yok."
                )

            } else {

                LazyColumn(

                    modifier =
                        Modifier.fillMaxSize(),

                    verticalArrangement =
                        Arrangement.spacedBy(10.dp)
                ) {

                    items(

                        items = cihazlar,

                        key = {
                            it.id
                        }

                    ) { cihaz ->

                        CihazKontrolKarti(
                            cihaz = cihaz
                        )
                    }
                }
            }
        }
    }
}


// ==================================================
// CIHAZ KONTROL KARTI
// ==================================================


@Composable
private fun CihazKontrolKarti(
    cihaz: Cihaz
) {

    val coroutineScope = rememberCoroutineScope()

    Card(

        modifier =
            Modifier.fillMaxWidth()

    ) {

        Column(

            modifier =
                Modifier.padding(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {

            // ==========================================
            // CIHAZ ADI
            // ==========================================

            Text(

                text =
                    cihaz.cihazAdi,

                style =
                    MaterialTheme
                        .typography
                        .titleMedium
            )


            Spacer(
                modifier =
                    Modifier.padding(2.dp)
            )


            // ==========================================
            // AC
            // ==========================================

            Button(

                onClick = {

                    coroutineScope.launch {

                        UdpHaberlesme.gonder(
                            "255.255.255.255",
                            "${cihaz.teknikId}|ON"
                        )
                    }
                },

                modifier =
                    Modifier.fillMaxWidth()

            ) {
                Text("AC")
            }


            // ==========================================
            // KAPAT
            // ==========================================

            Button(

                onClick = {

                    coroutineScope.launch {

                        UdpHaberlesme.gonder(
                            "255.255.255.255",
                            "${cihaz.teknikId}|OFF"
                        )
                    }
                },

                modifier =
                    Modifier.fillMaxWidth()

            ) {
                Text("KAPAT")
            }


            // ==========================================
            // DURUM
            // ==========================================

            Button(

                onClick = {

                    coroutineScope.launch {

                        UdpHaberlesme.gonder(
                            "255.255.255.255",
                            "${cihaz.teknikId}|TEST"
                        )
                    }
                },

                modifier =
                    Modifier.fillMaxWidth()

            ) {

                Text("DURUM")
            }
        }
    }
}
