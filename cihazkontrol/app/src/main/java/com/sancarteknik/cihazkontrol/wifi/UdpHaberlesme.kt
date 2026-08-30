package com.sancarteknik.cihazkontrol.wifi

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress

data class UdpMesaj(
    val mesaj: String,
    val modul: String = "",
    val ip: String = "",
    val giden: Boolean,
    val zaman: Long = System.currentTimeMillis()
)

object UdpHaberlesme {

    private const val UDP_PORT = 4210
    private const val BUFFER_SIZE = 2048
    private const val MAX_MESAJ = 200

    private val udpScope = CoroutineScope(Dispatchers.IO)

    @Volatile
    private var socket: DatagramSocket? = null

    @Volatile
    private var dinlemeJob: Job? = null

    private val socketMutex = Mutex()

    private val _mesajlar = MutableStateFlow<List<UdpMesaj>>(emptyList())
    val mesajlar: StateFlow<List<UdpMesaj>> = _mesajlar.asStateFlow()

    // ==================================================
    // IP CACHE
    // ==================================================

    // private val ipCache = mutableMapOf<String, String>()   // ❌ ESKİ
    val ipCache = mutableMapOf<String, String>()             // ✅ YENİ (public)

    // ✅ Son gönderilen mesajı hatırla (broadcast kontrolü için)
    private var sonGonderilenMesaj: String = ""

    fun cihazIpAl(teknikId: String): String {
        return ipCache[teknikId] ?: "255.255.255.255"
    }

    fun cihazIpGuncelle(teknikId: String, ip: String) {
        if (ip.isNotBlank() && ip != "255.255.255.255") {
            ipCache[teknikId] = ip
            Log.d("UdpHaberlesme", "💾 IP GÜNCELLENDİ: $teknikId -> $ip")
        }
    }

    fun ipDurumu(teknikId: String): String {
        return ipCache[teknikId] ?: "255.255.255.255"
    }

    // ==================================================
    // BASLAT / DURDUR
    // ==================================================

    fun baslat() {
        udpScope.launch {
            socketMutex.withLock {
                if (socket != null && socket!!.isBound && !socket!!.isClosed && dinlemeJob?.isActive == true) {
                    Log.d("UdpHaberlesme", "UDP zaten aktif")
                    return@withLock
                }

                dinlemeJob?.cancel()
                dinlemeJob = null
                socket?.close()
                socket = null

                try {
                    val yeniSocket = DatagramSocket(null).apply {
                        reuseAddress = true
                        broadcast = true
                        bind(InetSocketAddress(UDP_PORT))
                    }
                    socket = yeniSocket
                    Log.d("UdpHaberlesme", "UDP MERKEZI BASLATILDI Port: $UDP_PORT")
                    dinlemeJob = udpScope.launch { dinle(yeniSocket) }
                } catch (e: Exception) {
                    socket = null
                    dinlemeJob = null
                    Log.e("UdpHaberlesme", "UDP SOCKET OLUSTURULAMADI", e)
                }
            }
        }
    }

    fun durdur() {
        Log.d("UdpHaberlesme", "UDP MERKEZI DURDURULUYOR")
        udpScope.launch {
            socketMutex.withLock {
                dinlemeJob?.cancel()
                dinlemeJob = null
                socket?.close()
                socket = null
            }
        }
    }

    fun yenidenBaslat() {
        udpScope.launch {
            socketMutex.withLock {
                Log.d("UdpHaberlesme", "UDP SOCKET YENIDEN BASLATILIYOR")
                dinlemeJob?.cancel()
                dinlemeJob = null
                socket?.close()
                socket = null
            }
            delay(100)
            baslat()
        }
    }

    fun temizle() {
        _mesajlar.value = emptyList()
    }

    // ==================================================
    // DINLEME
    // ==================================================

    private suspend fun dinle(aktifSocket: DatagramSocket) {
        try {
            while (!aktifSocket.isClosed) {
                val buffer = ByteArray(BUFFER_SIZE)
                val paket = DatagramPacket(buffer, buffer.size)
                aktifSocket.receive(paket)

                val mesaj = String(paket.data, paket.offset, paket.length, Charsets.UTF_8).trim()
                if (mesaj.isEmpty()) continue

                val ip = paket.address?.hostAddress ?: ""
                val port = paket.port
                val modul = modulBul(mesaj)

                Log.d("UdpHaberlesme", "📥 PAKET ALINDI IP:$ip PORT:$port MESAJ:$mesaj")

                // ✅ KENDI BROADCAST'İ IGNORE ET (SADECE STATUS VE 255.255.255.255 DEĞİLSE)
                if (mesaj == sonGonderilenMesaj && ip != "255.255.255.255") {
                    // Sadece STATUS ise ignore et
                    if (mesaj.contains("STATUS")) {
                        Log.d("UdpHaberlesme", "⛔ KENDI STATUS IGNORE: $mesaj")
                        continue
                    }
                }

                // IP GÜNCELLEME SADECE STATUS CEVABINDA YAPILIR
                if (modul.isNotEmpty() && ip.isNotBlank() && ip != "255.255.255.255") {
                    val parcalar = mesaj.split("|")
                    if (parcalar.size >= 2 && parcalar[1].equals("STATUS", true)) {
                        cihazIpGuncelle(modul, ip)
                    }
                }

                mesajEkle(UdpMesaj(mesaj = mesaj, modul = modul, ip = ip, giden = false))
            }
        } catch (e: java.net.SocketException) {
            if (!aktifSocket.isClosed) {
                Log.e("UdpHaberlesme", "UDP SOCKET HATASI", e)
            }
        } catch (e: Exception) {
            Log.e("UdpHaberlesme", "UDP DINLEME HATASI", e)
        } finally {
            socketMutex.withLock {
                if (socket === aktifSocket) {
                    socket = null
                    dinlemeJob = null
                }
            }
            aktifSocket.close()
            Log.d("UdpHaberlesme", "UDP DINLEME GOREVI SONA ERDI")
        }
    }

    // ==================================================
    // GONDER
    // ==================================================

    suspend fun gonder(hedefIp: String, mesaj: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (hedefIp.isBlank()) {
                    Log.e("UdpHaberlesme", "HEDEF IP BOS")
                    return@withContext false
                }

                var aktifSocket = socket
                if (aktifSocket == null || aktifSocket.isClosed) {
                    Log.d("UdpHaberlesme", "UDP SOCKET YOK, BASLATILIYOR")
                    baslat()
                    repeat(40) {
                        delay(50)
                        aktifSocket = socket
                        if (aktifSocket != null && !aktifSocket!!.isClosed && aktifSocket!!.isBound) {
                            return@repeat
                        }
                    }
                }

                if (aktifSocket == null || aktifSocket!!.isClosed) {
                    Log.e("UdpHaberlesme", "UDP SOCKET HAZIR DEGIL")
                    return@withContext false
                }

                val veri = mesaj.toByteArray(Charsets.UTF_8)
                val adres = InetAddress.getByName(hedefIp)
                val paket = DatagramPacket(veri, veri.size, adres, UDP_PORT)
                aktifSocket!!.send(paket)

                // ✅ Gönderilen mesajı kaydet (broadcast kontrolü için)
                sonGonderilenMesaj = mesaj

                Log.d("UdpHaberlesme", "📤 UDP GONDERILDI IP:$hedefIp PORT:$UDP_PORT MESAJ:$mesaj")

                mesajEkle(UdpMesaj(mesaj = mesaj, modul = modulBul(mesaj), ip = hedefIp, giden = true))
                true
            } catch (e: Exception) {
                Log.e("UdpHaberlesme", "UDP GONDERME HATASI", e)
                false
            }
        }
    }

    // ==================================================
    // YARDIMCILAR
    // ==================================================

    private fun mesajEkle(mesaj: UdpMesaj) {
        val yeniListe = (_mesajlar.value + mesaj).takeLast(MAX_MESAJ)
        _mesajlar.value = yeniListe
    }

    private fun modulBul(mesaj: String): String {
        val parcalar = mesaj.split("|")
        if (parcalar.isNotEmpty() && parcalar[0].startsWith("SNCR_", ignoreCase = true)) {
            return parcalar[0]
        }
        return ""
    }
}