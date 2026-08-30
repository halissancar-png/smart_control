
@file:OptIn(ExperimentalMaterial3Api::class)

package com.sancarteknik.cihazkontrol.ekranlar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sancarteknik.cihazkontrol.veritabani.Mekan
import com.sancarteknik.cihazkontrol.veritabani.UygulamaVeritabani
import kotlinx.coroutines.launch

@Composable
fun MekanlarSayfasi(
    geri: () -> Unit,
    mekanKontrol: (Mekan) -> Unit
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val veritabani =
        remember {
            UygulamaVeritabani.getir(context)
        }

    var mekanlar by remember {
        mutableStateOf<List<Mekan>>(emptyList())
    }

    var mekanAdi by remember {
        mutableStateOf("")
    }

    var pencereAcik by remember {
        mutableStateOf(false)
    }



    // =========================================
    // MEKANLARI YUKLE
    // =========================================

    LaunchedEffect(Unit) {

        mekanlar =
            veritabani
                .mekanDao()
                .tumunuGetir()
    }

    // =========================================
    // EKRAN
    // =========================================

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Mekanlar")
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
                    .padding(16.dp)
        ) {

            Button(

                onClick = {
                    mekanAdi = ""
                    pencereAcik = true
                },

                modifier =
                    Modifier.fillMaxWidth()

            ) {

                Text("+ Mekan Ekle")
            }

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            Text(
                text = "Kayitli Mekanlar",

                style =
                    MaterialTheme
                        .typography
                        .titleLarge
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            if (mekanlar.isEmpty()) {

                Text(
                    "Henuz kayitli mekan yok."
                )

            } else {

                LazyColumn(

                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)

                ) {

                    items(
                        items = mekanlar,
                        key = {
                            it.id
                        }
                    ) { mekan ->

                        Card(
                            modifier =
                                Modifier.fillMaxWidth()
                        ) {

                            Row(

                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(15.dp),

                                horizontalArrangement =
                                    Arrangement.SpaceBetween

                            ) {

                                Text(
                                    text =
                                        mekan.isim,

                                    style =
                                        MaterialTheme
                                            .typography
                                            .titleMedium
                                )




                                TextButton(

                                    onClick = {
                                        mekanKontrol(mekan)
                                    }

                                ) {
                                    Text("Kontrol")
                                }



                                TextButton(

                                    onClick = {

                                        coroutineScope.launch {

                                            veritabani
                                                .mekanDao()
                                                .sil(mekan)

                                            mekanlar =
                                                veritabani
                                                    .mekanDao()
                                                    .tumunuGetir()
                                        }
                                    }

                                ) {

                                    Text("Sil")
                                }



                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================
    // MEKAN EKLE PENCERESI
    // =========================================

    if (pencereAcik) {

        AlertDialog(

            onDismissRequest = {
                pencereAcik = false
            },

            title = {
                Text("Mekan Ekle")
            },

            text = {

                OutlinedTextField(

                    value = mekanAdi,

                    onValueChange = {
                        mekanAdi = it
                    },

                    label = {
                        Text("Mekan Adi")
                    },

                    singleLine = true,

                    modifier =
                        Modifier.fillMaxWidth()
                )
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        val isim =
                            mekanAdi.trim()

                        if (isim.isEmpty()) {
                            return@TextButton
                        }

                        coroutineScope.launch {

                            veritabani
                                .mekanDao()
                                .ekle(
                                    Mekan(
                                        isim = isim
                                    )
                                )

                            mekanlar =
                                veritabani
                                    .mekanDao()
                                    .tumunuGetir()

                            mekanAdi = ""

                            pencereAcik = false
                        }
                    }

                ) {

                    Text("Kaydet")
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {
                        pencereAcik = false
                    }

                ) {

                    Text("Iptal")
                }
            }
        )
    }
}
