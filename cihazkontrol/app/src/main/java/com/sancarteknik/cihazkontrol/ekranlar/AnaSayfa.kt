@file:OptIn(ExperimentalMaterial3Api::class)

package com.sancarteknik.cihazkontrol.ekranlar

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sancarteknik.cihazkontrol.sync.SenkronizasyonServisi
import com.sancarteknik.cihazkontrol.utils.SharedPrefs
import com.sancarteknik.cihazkontrol.veritabani.Mekan
import com.sancarteknik.cihazkontrol.veritabani.UygulamaVeritabani
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.sancarteknik.cihazkontrol.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch

@Composable
fun AnaSayfa(
    mekanlar: () -> Unit,
    mekanKontrol: (Mekan) -> Unit,
    cihazlar: () -> Unit,
    veriTakibi: () -> Unit,
    ayarlar: () -> Unit
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val veritabani = remember { UygulamaVeritabani.getir(context) }

    var liste by remember { mutableStateOf<List<Mekan>>(emptyList()) }
    var senkronizeEdiliyor by remember { mutableStateOf(false) }
    var hataMesaji by remember { mutableStateOf<String?>(null) }

    suspend fun mekanlariYukle() {
        liste = veritabani.mekanDao().tumunuGetir()
    }

    suspend fun senkronizeEt() {
        val kullaniciId = SharedPrefs.getKullaniciId()
        if (kullaniciId == 0) return
        senkronizeEdiliyor = true
        hataMesaji = null
        try {
            val servis = SenkronizasyonServisi(context)
            val sonuc = servis.tamSenkronizasyon(kullaniciId)
            if (sonuc) {
                mekanlariYukle()
            } else {
                hataMesaji = "Senkronizasyon başarısız, çevrimdışı modda çalışılıyor."
                mekanlariYukle()
            }
        } catch (e: Exception) {
            hataMesaji = "Bağlantı hatası: ${e.message}"
            mekanlariYukle()
        }
        senkronizeEdiliyor = false
    }

    LaunchedEffect(Unit) {
        mekanlariYukle()
        senkronizeEt()
    }

    Scaffold(
        topBar = {
            Image(
                painter = painterResource(id = R.drawable.sancar_header),
                contentDescription = "Sancar Teknik",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 10.dp),
                contentScale = ContentScale.FillWidth
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KONTROL PANELİ",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                if (senkronizeEdiliyor) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "Kontrol etmek istediğiniz mekanı seçin.", style = MaterialTheme.typography.bodyMedium)

            if (hataMesaji != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = hataMesaji!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (liste.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🏢", fontSize = 30.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (senkronizeEdiliyor) "Mekanlar yükleniyor..." else "Henüz mekan tanımlanmamış.",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Mekan eklemek için Ayarlar bölümünü kullanın.")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(items = liste, key = { it.id }) { mekan ->
                        MekanPanelKarti(
                            mekan = mekan,
                            onClick = { mekanKontrol(mekan) }
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable { ayarlar() },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "⚙", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "Ayarlar", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(text = "Mekan, cihaz ve sistem ayarları", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MekanPanelKarti(mekan: Mekan, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🏢", fontSize = 30.sp)
            }
            Spacer(modifier = Modifier.width(18.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = mekan.isim, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Kontrol panelini aç", style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = "›", fontSize = 32.sp, fontWeight = FontWeight.Light)
        }
    }
}