@file:OptIn(ExperimentalMaterial3Api::class)

package com.sancarteknik.cihazkontrol.ekranlar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sancarteknik.cihazkontrol.sync.SenkronizasyonServisi
import com.sancarteknik.cihazkontrol.utils.SharedPrefs
import kotlinx.coroutines.launch

@Composable
fun SenkronizasyonSayfasi(
    geri: () -> Unit
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var senkronizeEdiliyor by remember { mutableStateOf(false) }
    var sonucMesaji by remember { mutableStateOf<String?>(null) }
    var durum by remember { mutableStateOf("Hazır") }

    // ============================================
    // SENKRONİZASYON BAŞLAT
    // ============================================

    fun senkronizasyonBaslat() {
        coroutineScope.launch {
            senkronizeEdiliyor = true
            durum = "Senkronizasyon başlatılıyor..."
            sonucMesaji = null

            try {
                val kullaniciId = SharedPrefs.getKullaniciId()
                if (kullaniciId == 0) {
                    sonucMesaji = "❌ Kullanıcı oturumu açık değil"
                    durum = "Hata"
                    senkronizeEdiliyor = false
                    return@launch
                }

                durum = "Mekanlar senkronize ediliyor..."
                val servis = SenkronizasyonServisi(context)
                val sonuc = servis.tamSenkronizasyon(kullaniciId)

                if (sonuc) {
                    sonucMesaji = "✅ Senkronizasyon başarılı!"
                    durum = "Tamamlandı"
                } else {
                    sonucMesaji = "⚠️ Senkronizasyon kısmen başarısız. Çevrimdışı modda çalışılıyor."
                    durum = "Kısmi başarı"
                }
            } catch (e: Exception) {
                sonucMesaji = "❌ Hata: ${e.message}"
                durum = "Hata"
            }

            senkronizeEdiliyor = false
        }
    }

    // ============================================
    // EKRAN
    // ============================================

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Veri Senkronizasyonu") },
                navigationIcon = {
                    IconButton(onClick = geri) {
                        Text("<")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Başlık
            Text(
                text = "🔄 Veri Senkronizasyonu",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Web sunucusu ile yerel veritabanını senkronize eder.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Durum
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Durum: $durum",
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (sonucMesaji != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = sonucMesaji!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (sonucMesaji!!.startsWith("✅")) {
                                MaterialTheme.colorScheme.primary
                            } else if (sonucMesaji!!.startsWith("❌")) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Senkronizasyon Butonu
            Button(
                onClick = { senkronizasyonBaslat() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !senkronizeEdiliyor
            ) {
                if (senkronizeEdiliyor) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text("Senkronize ediliyor...")
                    }
                } else {
                    Text("🔄 Senkronizasyonu Başlat")
                }
            }

            // Bilgi
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "📋 Senkronize Edilen Veriler",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Mekanlar\n• Cihazlar\n• Cihaz durumları\n• Kanal açıklamaları",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}