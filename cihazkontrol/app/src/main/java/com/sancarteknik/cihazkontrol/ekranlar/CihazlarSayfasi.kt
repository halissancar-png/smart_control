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
import com.sancarteknik.cihazkontrol.veritabani.Cihaz
import com.sancarteknik.cihazkontrol.veritabani.Mekan
import com.sancarteknik.cihazkontrol.veritabani.UygulamaVeritabani
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CihazlarSayfasi(
    geri: () -> Unit,
    cihazEkle: () -> Unit
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val veritabani = remember { UygulamaVeritabani.getir(context) }

    var cihazlar by remember { mutableStateOf<List<Cihaz>>(emptyList()) }
    var mekanlar by remember { mutableStateOf<List<Mekan>>(emptyList()) }
    var yukleniyor by remember { mutableStateOf(false) }

    suspend fun verileriYukle() {
        cihazlar = veritabani.cihazDao().tumunuGetir()
        mekanlar = veritabani.mekanDao().tumunuGetir()
    }

    suspend fun cihazSil(cihaz: Cihaz): Boolean {
        val kullaniciId = SharedPrefs.getKullaniciId()
        if (kullaniciId == 0) return false
        try {
            val webCihazId = if (cihaz.webId > 0) cihaz.webId else cihaz.id
            val response = ApiClient.service.cihazSil(kullaniciId, webCihazId)
            if (response.isSuccessful && response.body() == "OK") {
                veritabani.cihazDao().sil(cihaz)
                verileriYukle()
                return true
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }

    LaunchedEffect(Unit) {
        verileriYukle()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cihazlar") },
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
                onClick = cihazEkle,
                modifier = Modifier.fillMaxWidth(),
                enabled = !yukleniyor
            ) {
                Text("+ Cihaz Ekle")
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Kayitli Cihazlar", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(10.dp))

            if (cihazlar.isEmpty()) {
                Text("Henüz kayitli cihaz yok.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items = cihazlar, key = { it.id }) { cihaz ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(15.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = cihaz.cihazAdi, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "${cihaz.cihazTipi} | ${cihaz.kanalSayisi} kanal",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Teknik ID: ${cihaz.teknikId}")
                                val mekanAdi = mekanlar.firstOrNull { it.id == cihaz.mekanId }?.isim ?: "Mekan bulunamadı"
                                Text("Mekan: $mekanAdi")
                                if (cihaz.durum.isNotEmpty()) {
                                    Text(text = "Durum: ${cihaz.durum}", style = MaterialTheme.typography.bodySmall)
                                }
                                if (cihaz.sonGorulme > 0) {
                                    val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                                    Text(
                                        text = "Son görülme: ${format.format(Date(cihaz.sonGorulme))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row {
                                    Button(onClick = { }) { Text("Kontrol") }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                yukleniyor = true
                                                cihazSil(cihaz)
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
}