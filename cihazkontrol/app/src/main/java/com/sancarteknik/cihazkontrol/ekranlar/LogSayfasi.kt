@file:OptIn(ExperimentalMaterial3Api::class)

package com.sancarteknik.cihazkontrol.ekranlar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment  // ✅ EKLENDİ
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sancarteknik.cihazkontrol.utils.SharedPrefs
import kotlinx.coroutines.launch

@Composable
fun LogSayfasi(
    geri: () -> Unit
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var loglar by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var yukleniyor by remember { mutableStateOf(false) }
    var hata by remember { mutableStateOf<String?>(null) }

    // ============================================
    // LOGLARI YÜKLE
    // ============================================

    suspend fun loglariYukle() {
        val kullaniciId = SharedPrefs.getKullaniciId()
        if (kullaniciId == 0) {
            hata = "Kullanıcı oturumu açık değil"
            return
        }

        yukleniyor = true
        hata = null

        try {
            // TODO: Web API'den logları çek
            // Şimdilik örnek veri göster
            val ornekLoglar = listOf(
                mapOf("zaman" to "2024-01-15 14:30:25", "islem" to "Cihaz Açıldı - Salon"),
                mapOf("zaman" to "2024-01-15 14:25:10", "islem" to "Cihaz Kapatıldı - Mutfak"),
                mapOf("zaman" to "2024-01-15 14:20:05", "islem" to "Mekan Eklendi - Ofis")
            )
            loglar = ornekLoglar

        } catch (e: Exception) {
            hata = "Loglar yüklenirken hata: ${e.message}"
        }

        yukleniyor = false
    }

    LaunchedEffect(Unit) {
        loglariYukle()
    }

    // ============================================
    // EKRAN
    // ============================================

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("İşlem Geçmişi") },
                navigationIcon = {
                    IconButton(onClick = geri) {
                        Text("<")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                loglariYukle()
                            }
                        }
                    ) {
                        Text("🔄")
                    }
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
            Text(
                text = "Son İşlemler",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (yukleniyor) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center  // ✅ ARTIK ÇALIŞIYOR
                ) {
                    CircularProgressIndicator()
                }
            } else if (hata != null) {
                Text(
                    text = hata!!,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (loglar.isEmpty()) {
                Text("Henüz işlem kaydı yok.")
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(loglar) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = log["islem"] ?: "",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = log["zaman"] ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}