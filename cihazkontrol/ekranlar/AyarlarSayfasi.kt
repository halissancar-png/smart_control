@file:OptIn(ExperimentalMaterial3Api::class)

package com.sancarteknik.cihazkontrol.ekranlar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AyarlarSayfasi(
    geri: () -> Unit,

    mekanlar: () -> Unit,

    cihazlar: () -> Unit,

    veriTakibi: () -> Unit
) {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Ayarlar")
                },

                navigationIcon = {

                    IconButton(
                        onClick = geri
                    ) {

                        Text("<")
                    }
                }
            )
        }

    ) { padding ->

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {


            // ==================================================
            // YONETIM
            // ==================================================

            Text(

                text = "Yonetim",

                style =
                    MaterialTheme
                        .typography
                        .titleLarge
            )


            // --------------------------------------------------
            // MEKAN YONETIMI
            // --------------------------------------------------

            Button(

                onClick = mekanlar,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text("Mekan Yonetimi")
            }


            // --------------------------------------------------
            // CIHAZ YONETIMI
            // --------------------------------------------------

            Button(

                onClick = cihazlar,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text("Cihaz Yonetimi")
            }


            // --------------------------------------------------
            // VERI TAKIBI
            // --------------------------------------------------

            Button(

                onClick = veriTakibi,

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text("Veri Takibi")
            }


            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )


            // ==================================================
            // SISTEM
            // ==================================================

            Text(

                text = "Sistem",

                style =
                    MaterialTheme
                        .typography
                        .titleLarge
            )


            // --------------------------------------------------
            // BAGLANTI
            // --------------------------------------------------

            Button(

                onClick = { },

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text("Baglanti Ayarlari")
            }


            // --------------------------------------------------
            // VERI
            // --------------------------------------------------

            Button(

                onClick = { },

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text("Veri Ayarlari")
            }


            // --------------------------------------------------
            // HAKKINDA
            // --------------------------------------------------

            Button(

                onClick = { },

                modifier =
                    Modifier.fillMaxWidth()
            ) {

                Text("Hakkinda")
            }
        }
    }
}

