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
import com.sancarteknik.cihazkontrol.wifi.UdpHaberlesme
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun MekanKontrolSayfasi(
    mekan: Mekan,
    geri: () -> Unit
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val veritabani = remember { UygulamaVeritabani.getir(context) }

    var cihazlar by remember { mutableStateOf<List<Cihaz>>(emptyList()) }

    suspend fun cihazlariYukle() {
        cihazlar = veritabani
            .cihazDao()
            .tumunuGetir()
            .filter { it.mekanId == mekan.id && it.aktif }
    }

    fun komutGonder(cihaz: Cihaz, kanal: Int, deger: Int) {
        coroutineScope.launch {
            val udpMesaj = if (deger == 1) "ON" else "OFF"
            UdpHaberlesme.gonder(
                "255.255.255.255",
                "${cihaz.teknikId}|$kanal|$udpMesaj"
            )

            val kullaniciId = SharedPrefs.getKullaniciId()
            try {
                ApiClient.service.komutGonder(
                    cihazId = cihaz.id,
                    kanal = kanal,
                    deger = deger,
                    kullaniciId = kullaniciId
                )
            } catch (e: Exception) {
                // Web API hatasını görmezden gel
            }
        }
    }

    LaunchedEffect(mekan.id) {
        cihazlariYukle()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mekan Yönetimi") },
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
            Text(text = "Kontrol Paneli", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.padding(6.dp))
            Text(text = "Mekan: ${mekan.isim}")
            Spacer(modifier = Modifier.padding(8.dp))

            if (cihazlar.isEmpty()) {
                Text(text = "Bu mekana kayitli cihaz yok.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items = cihazlar, key = { it.id }) { cihaz ->
                        CihazKontrolKarti(
                            cihaz = cihaz,
                            onKomut = { kanal, deger ->
                                komutGonder(cihaz, kanal, deger)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CihazKontrolKarti(
    cihaz: Cihaz,
    onKomut: (Int, Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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

            Spacer(modifier = Modifier.padding(2.dp))

            for (i in 1..cihaz.kanalSayisi) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "$i", modifier = Modifier.width(30.dp), style = MaterialTheme.typography.bodyMedium)

                    // ✅ DÜZELTİLDİ: colors → buttonColors
                    Button(
                        onClick = { onKomut(i, 1) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("AÇ")
                    }

                    // ✅ DÜZELTİLDİ: colors → buttonColors
                    Button(
                        onClick = { onKomut(i, 0) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("KAPAT")
                    }

                    Text(
                        text = if (cihaz.durum.contains("$i=ON")) "🟢" else "🔴",
                        modifier = Modifier.width(30.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        for (i in 1..cihaz.kanalSayisi) {
                            onKomut(i, 1)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Tümünü AÇ")
                }

                // ✅ DÜZELTİLDİ: colors → buttonColors
                Button(
                    onClick = {
                        for (i in 1..cihaz.kanalSayisi) {
                            onKomut(i, 0)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Tümünü KAPAT")
                }
            }
        }
    }
}