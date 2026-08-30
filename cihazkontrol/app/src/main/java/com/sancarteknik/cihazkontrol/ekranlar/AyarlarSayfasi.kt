@file:OptIn(ExperimentalMaterial3Api::class)

package com.sancarteknik.cihazkontrol.ekranlar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sancarteknik.cihazkontrol.utils.SharedPrefs

@Composable
fun AyarlarSayfasi(
    geri: () -> Unit,
    mekanlar: () -> Unit,
    cihazlar: () -> Unit,
    veriTakibi: () -> Unit,
    log: () -> Unit,              // ✅ EKLENDİ
    senkronizasyon: () -> Unit    // ✅ EKLENDİ
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "Yonetim", style = MaterialTheme.typography.titleLarge)

            Button(onClick = mekanlar, modifier = Modifier.fillMaxWidth()) {
                Text("Mekan Yonetimi")
            }
            Button(onClick = cihazlar, modifier = Modifier.fillMaxWidth()) {
                Text("Cihaz Yonetimi")
            }
            Button(onClick = veriTakibi, modifier = Modifier.fillMaxWidth()) {
                Text("Veri Takibi")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Veri", style = MaterialTheme.typography.titleLarge)

            // ✅ SENKRONİZASYON BUTONU
            Button(
                onClick = senkronizasyon,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔄 Veri Senkronizasyonu")
            }

            // ✅ LOG BUTONU
            Button(
                onClick = log,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📋 İşlem Geçmişi")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Sistem", style = MaterialTheme.typography.titleLarge)

            Button(onClick = { }, modifier = Modifier.fillMaxWidth()) {
                Text("Baglanti Ayarlari")
            }
            Button(onClick = { }, modifier = Modifier.fillMaxWidth()) {
                Text("Veri Ayarlari")
            }
            Button(onClick = { }, modifier = Modifier.fillMaxWidth()) {
                Text("Hakkinda")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    SharedPrefs.cikisYap()
                    geri()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Çıkış Yap")
            }
        }
    }
}