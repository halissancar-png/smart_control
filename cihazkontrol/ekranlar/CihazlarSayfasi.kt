
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
import com.sancarteknik.cihazkontrol.veritabani.Cihaz
import com.sancarteknik.cihazkontrol.veritabani.UygulamaVeritabani
import kotlinx.coroutines.launch

import com.sancarteknik.cihazkontrol.veritabani.Mekan




@Composable
fun CihazlarSayfasi(
    geri: () -> Unit,
    cihazEkle: () -> Unit
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val veritabani =
        remember {
            UygulamaVeritabani.getir(context)
        }

    var cihazlar by remember {
        mutableStateOf<List<Cihaz>>(emptyList())
    }

    var mekanlar by remember {
        mutableStateOf<List<Mekan>>(emptyList())
    }

    // =========================================
    // CIHAZLARI YUKLE
    // =========================================

    LaunchedEffect(Unit) {

        cihazlar =
            veritabani
                .cihazDao()
                .tumunuGetir()

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
                    Text("Cihazlar")
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

                onClick = cihazEkle,

                modifier =
                    Modifier.fillMaxWidth()

            ) {

                Text("+ Cihaz Ekle")
            }

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            Text(
                text = "Kayitli Cihazlar",
                style =
                    MaterialTheme.typography.titleLarge
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            if (cihazlar.isEmpty()) {

                Text(
                    "Henuz kayitli cihaz yok."
                )

            } else {

                LazyColumn(

                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)

                ) {

                    items(
                        items = cihazlar,
                        key = {
                            it.id
                        }
                    ) { cihaz ->

                        Card(
                            modifier =
                                Modifier.fillMaxWidth()
                        ) {

                            Column(
                                modifier =
                                    Modifier.padding(15.dp)
                            ) {

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
                                        Modifier.height(6.dp)
                                )

                                Text(
                                    "Teknik ID: ${cihaz.teknikId}"
                                )

                                val mekanAdi =
                                    mekanlar
                                        .firstOrNull {
                                            it.id == cihaz.mekanId
                                        }
                                        ?.isim
                                        ?: "Mekan bulunamadi"

                                Text(
                                    "Mekan: $mekanAdi"
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(10.dp)
                                )

                                Row {

                                    Button(
                                        onClick = {
                                            // Kontrol sayfasini
                                            // sonraki adimda ekleyecegiz.
                                        }
                                    ) {

                                        Text("Kontrol")
                                    }

                                    Spacer(
                                        modifier =
                                            Modifier.width(8.dp)
                                    )

                                    OutlinedButton(

                                        onClick = {

                                            coroutineScope.launch {

                                                veritabani
                                                    .cihazDao()
                                                    .sil(cihaz)

                                                cihazlar =
                                                    veritabani
                                                        .cihazDao()
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
    }
}
