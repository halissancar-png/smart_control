package com.sancarteknik.cihazkontrol.ekranlar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sancarteknik.cihazkontrol.api.ApiClient
import com.sancarteknik.cihazkontrol.sync.SenkronizasyonServisi
import com.sancarteknik.cihazkontrol.utils.SharedPrefs
import kotlinx.coroutines.launch

@Composable
fun GirisSayfasi(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isim by remember { mutableStateOf("") }
    var sifre by remember { mutableStateOf("") }
    var yukleniyor by remember { mutableStateOf(false) }
    var hata by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "SmartControl", fontSize = 28.sp, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Giriş Yap", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(24.dp))

                if (hata != null) {
                    Text(text = hata!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                OutlinedTextField(
                    value = isim,
                    onValueChange = { isim = it },
                    label = { Text("Kullanıcı Adı") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !yukleniyor,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = sifre,
                    onValueChange = { sifre = it },
                    label = { Text("Şifre") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !yukleniyor,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (isim.isBlank() || sifre.isBlank()) {
                            hata = "Kullanıcı adı ve şifre gerekli"
                            return@Button
                        }
                        coroutineScope.launch {
                            yukleniyor = true
                            hata = null
                            try {
                                val response = ApiClient.service.login(isim, sifre)
                                if (response.isSuccessful) {
                                    val cevap = response.body() ?: ""
                                    if (cevap.startsWith("OK|")) {
                                        val parcalar = cevap.split("|")
                                        if (parcalar.size >= 3) {
                                            val id = parcalar[1].toIntOrNull() ?: 0
                                            val ad = parcalar[2]
                                            if (id > 0) {
                                                SharedPrefs.setKullanici(id, ad)
                                                val syncServisi = SenkronizasyonServisi(context)
                                                syncServisi.tamSenkronizasyon(id)
                                                onLoginSuccess()
                                            } else {
                                                hata = "Geçersiz kullanıcı bilgisi"
                                            }
                                        } else {
                                            hata = "Geçersiz sunucu yanıtı"
                                        }
                                    } else {
                                        hata = "Kullanıcı adı veya şifre hatalı"
                                    }
                                } else {
                                    hata = "Sunucu bağlantı hatası: ${response.code()}"
                                }
                            } catch (e: Exception) {
                                hata = "Bağlantı hatası: ${e.message}"
                            }
                            yukleniyor = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !yukleniyor
                ) {
                    Text(if (yukleniyor) "Giriş yapılıyor..." else "Giriş Yap")
                }
            }
        }
    }
}