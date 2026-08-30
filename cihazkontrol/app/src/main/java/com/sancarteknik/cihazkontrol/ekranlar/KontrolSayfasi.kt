@file:OptIn(ExperimentalMaterial3Api::class)

package com.sancarteknik.cihazkontrol.ekranlar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.sancarteknik.cihazkontrol.veritabani.Mekan
import com.sancarteknik.cihazkontrol.veritabani.UygulamaVeritabani

@Composable
fun KontrolSayfasi(
    geri: () -> Unit,
    mekanKontrol: (Mekan) -> Unit
) {

    val context = LocalContext.current

    val veritabani =
        remember {
            UygulamaVeritabani.getir(context)
        }

    var mekanlar by remember {
        mutableStateOf<List<Mekan>>(emptyList())
    }

    LaunchedEffect(Unit) {

        mekanlar =
            veritabani
                .mekanDao()
                .tumunuGetir()
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Kontrol")
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

        LazyColumn(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            items(
                items = mekanlar,
                key = {
                    it.id
                }
            ) { mekan ->

                Button(

                    onClick = {
                        mekanKontrol(mekan)
                    },

                    modifier =
                        Modifier.fillMaxWidth()

                ) {

                    Text(
                        mekan.isim
                    )
                }
            }

            if (mekanlar.isEmpty()) {

                item {

                    Text(
                        "Kayitli mekan yok."
                    )
                }
            }
        }
    }
}