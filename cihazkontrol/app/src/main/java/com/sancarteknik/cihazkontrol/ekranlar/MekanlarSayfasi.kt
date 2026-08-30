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
import com.sancarteknik.cihazkontrol.api.ApiClient
import com.sancarteknik.cihazkontrol.utils.SharedPrefs
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
    val veritabani = remember { UygulamaVeritabani.getir(context) }

    var mekanlar by remember { mutableStateOf<List<Mekan>>(emptyList()) }
    var mekanAdi by remember { mutableStateOf("") }
    var pencereAcik by remember { mutableStateOf(false) }
    var yukleniyor by remember { mutableStateOf(false) }

    suspend fun mekanlariYukle() {
        mekanlar = veritabani.mekanDao().tumunuGetir()
    }

    suspend fun mekanEkle(isim: String): Boolean {
        val kullaniciId = SharedPrefs.getKullaniciId()
        if (kullaniciId == 0) return false
        try {
            val response = ApiClient.service.mekanEkle(kullaniciId, isim)
            if (response.isSuccessful && response.body() == "OK") {
                val mekan = Mekan(isim = isim)
                veritabani.mekanDao().ekle(mekan)
                mekanlariYukle()
                return true
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }

    suspend fun mekanSil(mekan: Mekan): Boolean {
        val kullaniciId = SharedPrefs.getKullaniciId()
        if (kullaniciId == 0) return false
        try {
            val webMekanId = if (mekan.webId > 0) mekan.webId else mekan.id
            val response = ApiClient.service.mekanSil(kullaniciId, webMekanId)
            if (response.isSuccessful && response.body() == "OK") {
                veritabani.mekanDao().sil(mekan)
                mekanlariYukle()
                return true
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }

    LaunchedEffect(Unit) {
        mekanlariYukle()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mekanlar") },
                navigationIcon = {
                    IconButton(onClick = geri) { Text("<") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Button(
                onClick = { mekanAdi = ""; pencereAcik = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = !yukleniyor
            ) {
                Text(if (yukleniyor) "Ekleniyor..." else "+ Mekan Ekle")
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Kayitli Mekanlar", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(10.dp))

            if (mekanlar.isEmpty()) {
                Text("Henüz kayitli mekan yok.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items = mekanlar, key = { it.id }) { mekan ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(15.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = mekan.isim, style = MaterialTheme.typography.titleMedium)
                                Row {
                                    TextButton(onClick = { mekanKontrol(mekan) }) { Text("Kontrol") }
                                    TextButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                yukleniyor = true
                                                mekanSil(mekan)
                                                yukleniyor = false
                                            }
                                        }
                                    ) { Text("Sil") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (pencereAcik) {
        AlertDialog(
            onDismissRequest = { pencereAcik = false },
            title = { Text("Mekan Ekle") },
            text = {
                OutlinedTextField(
                    value = mekanAdi,
                    onValueChange = { mekanAdi = it },
                    label = { Text("Mekan Adi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val isim = mekanAdi.trim()
                        if (isim.isEmpty()) return@TextButton
                        coroutineScope.launch {
                            yukleniyor = true
                            val basarili = mekanEkle(isim)
                            yukleniyor = false
                            if (basarili) {
                                mekanAdi = ""
                                pencereAcik = false
                            }
                        }
                    }
                ) {
                    Text(if (yukleniyor) "Ekleniyor..." else "Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { pencereAcik = false }) { Text("Iptal") }
            }
        )
    }
}