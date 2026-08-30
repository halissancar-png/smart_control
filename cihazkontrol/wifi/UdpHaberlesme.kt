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

// ==================================================
// UDP MESAJ
// ==================================================

data class UdpMesaj(

    val mesaj: String,

    val modul: String = "",

    val ip: String = "",

    val giden: Boolean,

    val zaman: Long =
        System.currentTimeMillis()
)


// ==================================================
// UDP HABERLESME MERKEZI
// ==================================================

object UdpHaberlesme {

    // ==================================================
    // AYARLAR
    // ==================================================

    private const val UDP_PORT = 4210

    private const val BUFFER_SIZE = 2048

    private const val MAX_MESAJ = 200


    // ==================================================
    // UDP SCOPE
    // ==================================================

    private val udpScope =
        CoroutineScope(
            Dispatchers.IO
        )


    // ==================================================
    // SOCKET
    // ==================================================

    @Volatile
    private var socket: DatagramSocket? = null


    // ==================================================
    // DINLEME GOREVI
    // ==================================================

    @Volatile
    private var dinlemeJob: Job? = null


    // ==================================================
    // BASLAT / DURDUR KILIDI
    // ==================================================
    //
    // Ayni anda iki baslatma islemi yapilmasini
    // engeller.
    // ==================================================

    private val socketMutex =
        Mutex()


    // ==================================================
    // MESAJ LISTESI
    // ==================================================

    private val _mesajlar =
        MutableStateFlow<List<UdpMesaj>>(
            emptyList()
        )

    val mesajlar:
            StateFlow<List<UdpMesaj>> =
        _mesajlar.asStateFlow()


    // ==================================================
    // UDP MERKEZINI BASLAT
    // ==================================================

    fun baslat() {

        udpScope.launch {

            socketMutex.withLock {

                // --------------------------------------
                // ZATEN CALISIYORSA HICBIR SEY YAPMA
                // --------------------------------------

                if (
                    socket != null &&
                    socket!!.isBound &&
                    !socket!!.isClosed &&
                    dinlemeJob?.isActive == true
                ) {

                    Log.d(
                        "UdpHaberlesme",
                        "UDP zaten aktif"
                    )

                    return@withLock
                }


                // --------------------------------------
                // ESKI GOREVI TEMIZLE
                // --------------------------------------

                dinlemeJob?.cancel()

                dinlemeJob = null


                // --------------------------------------
                // ESKI SOCKETI KAPAT
                // --------------------------------------

                socket?.close()

                socket = null


                // --------------------------------------
                // YENI SOCKET OLUSTUR
                // --------------------------------------

                try {

                    val yeniSocket =
                        DatagramSocket(null).apply {

                            reuseAddress = true

                            broadcast = true

                            bind(
                                InetSocketAddress(
                                    UDP_PORT
                                )
                            )
                        }


                    socket =
                        yeniSocket


                    Log.d(
                        "UdpHaberlesme",
                        "================================"
                    )

                    Log.d(
                        "UdpHaberlesme",
                        "UDP MERKEZI BASLATILDI"
                    )

                    Log.d(
                        "UdpHaberlesme",
                        "PORT : $UDP_PORT"
                    )

                    Log.d(
                        "UdpHaberlesme",
                        "================================"
                    )


                    // ----------------------------------
                    // TEK DINLEME GOREVI
                    // ----------------------------------

                    dinlemeJob =
                        udpScope.launch {

                            dinle(
                                yeniSocket
                            )
                        }

                } catch (
                    e: Exception
                ) {

                    socket = null

                    dinlemeJob = null

                    Log.e(
                        "UdpHaberlesme",
                        "UDP SOCKET OLUSTURULAMADI",
                        e
                    )
                }
            }
        }
    }


    // ==================================================
    // UDP DINLEME
    // ==================================================

    private suspend fun dinle(
        aktifSocket: DatagramSocket
    ) {

        try {

            while (
                !aktifSocket.isClosed
            ) {

                val buffer =
                    ByteArray(
                        BUFFER_SIZE
                    )


                val paket =
                    DatagramPacket(
                        buffer,
                        buffer.size
                    )


                // ----------------------------------
                // PAKET BEKLE
                // ----------------------------------

                aktifSocket.receive(
                    paket
                )


                // ----------------------------------
                // MESAJ
                // ----------------------------------

                val mesaj =
                    String(
                        paket.data,
                        paket.offset,
                        paket.length,
                        Charsets.UTF_8
                    ).trim()


                if (
                    mesaj.isEmpty()
                ) {

                    continue
                }


                // ----------------------------------
                // IP
                // ----------------------------------

                val ip =
                    paket.address
                        ?.hostAddress
                        ?: ""


                // ----------------------------------
                // PORT
                // ----------------------------------

                val port =
                    paket.port


                // ----------------------------------
                // MODUL
                // ----------------------------------

                val modul =
                    modulBul(
                        mesaj
                    )


                // ----------------------------------
                // LOG
                // ----------------------------------

                Log.d(
                    "UdpHaberlesme",
                    "================================"
                )

                Log.d(
                    "UdpHaberlesme",
                    "UDP GELDI"
                )

                Log.d(
                    "UdpHaberlesme",
                    "IP    : $ip"
                )

                Log.d(
                    "UdpHaberlesme",
                    "PORT  : $port"
                )

                Log.d(
                    "UdpHaberlesme",
                    "MESAJ : $mesaj"
                )

                Log.d(
                    "UdpHaberlesme",
                    "================================"
                )


                // ----------------------------------
                // MERKEZE EKLE
                // ----------------------------------

                mesajEkle(

                    UdpMesaj(

                        mesaj = mesaj,

                        modul = modul,

                        ip = ip,

                        giden = false
                    )
                )
            }

        } catch (
            e: java.net.SocketException
        ) {

            // --------------------------------------
            // Socket kapatildiysa bu normaldir.
            // HATA OLARAK GOSTERME.
            // --------------------------------------

            if (
                !aktifSocket.isClosed
            ) {

                Log.e(
                    "UdpHaberlesme",
                    "UDP SOCKET HATASI",
                    e
                )
            }

        } catch (
            e: Exception
        ) {

            Log.e(
                "UdpHaberlesme",
                "UDP DINLEME HATASI",
                e
            )

        } finally {

            // --------------------------------------
            // SADECE KENDI SOCKETIMIZI TEMIZLE
            // --------------------------------------

            socketMutex.withLock {

                if (
                    socket === aktifSocket
                ) {

                    socket = null

                    dinlemeJob = null
                }
            }


            aktifSocket.close()


            Log.d(
                "UdpHaberlesme",
                "UDP DINLEME GOREVI SONA ERDI"
            )
        }
    }


    // ==================================================
    // UDP MESAJ GONDER
    // ==================================================

    suspend fun gonder(
        hedefIp: String,
        mesaj: String
    ): Boolean {

        return withContext(
            Dispatchers.IO
        ) {

            try {

                // ----------------------------------
                // IP KONTROL
                // ----------------------------------

                if (
                    hedefIp.isBlank()
                ) {

                    Log.e(
                        "UdpHaberlesme",
                        "HEDEF IP BOS"
                    )

                    return@withContext false
                }


                // ----------------------------------
                // UDP MERKEZI YOKSA BASLAT
                // ----------------------------------

                var aktifSocket =
                    socket


                if (
                    aktifSocket == null ||
                    aktifSocket.isClosed
                ) {

                    Log.d(
                        "UdpHaberlesme",
                        "UDP SOCKET YOK, BASLATILIYOR"
                    )


                    baslat()


                    // ----------------------------------
                    // SOCKET OLUSMASINI BEKLE
                    //
                    // DIKKAT:
                    //
                    // repeat(40) { ... return@repeat }
                    // KULLANILMAZ. Kotlin'de return@repeat
                    // sadece o tek lambda cagrisindan cikar,
                    // dongunun tamamini durdurmaz - bu yuzden
                    // socket ilk denemede hazir olsa bile
                    // gereksiz yere 40 denemenin tamami
                    // (2 saniye) beklenirdi. Bunun yerine
                    // klasik bir while dongusu ile gercekten
                    // erken cikiyoruz.
                    // ----------------------------------

                    var deneme = 0

                    while (deneme < 40) {

                        delay(50)

                        aktifSocket =
                            socket

                        if (
                            aktifSocket != null &&
                            !aktifSocket!!.isClosed &&
                            aktifSocket!!.isBound
                        ) {

                            break
                        }

                        deneme++
                    }
                }


                // ----------------------------------
                // HALA SOCKET YOKSA
                // ----------------------------------

                if (
                    aktifSocket == null ||
                    aktifSocket!!.isClosed
                ) {

                    Log.e(
                        "UdpHaberlesme",
                        "UDP SOCKET HAZIR DEGIL"
                    )

                    return@withContext false
                }


                // ----------------------------------
                // MESAJI BYTE'A CEVIR
                // ----------------------------------

                val veri =
                    mesaj.toByteArray(
                        Charsets.UTF_8
                    )


                // ----------------------------------
                // HEDEF ADRES
                // ----------------------------------

                val adres =
                    InetAddress.getByName(
                        hedefIp
                    )


                // ----------------------------------
                // UDP PAKETI
                // ----------------------------------

                val paket =
                    DatagramPacket(

                        veri,

                        veri.size,

                        adres,

                        UDP_PORT
                    )


                // ----------------------------------
                // GONDER
                // ----------------------------------

                aktifSocket!!.send(
                    paket
                )


                // ----------------------------------
                // LOG
                // ----------------------------------

                Log.d(
                    "UdpHaberlesme",
                    "================================"
                )

                Log.d(
                    "UdpHaberlesme",
                    "UDP GONDERILDI"
                )

                Log.d(
                    "UdpHaberlesme",
                    "IP    : $hedefIp"
                )

                Log.d(
                    "UdpHaberlesme",
                    "PORT  : $UDP_PORT"
                )

                Log.d(
                    "UdpHaberlesme",
                    "MESAJ : $mesaj"
                )

                Log.d(
                    "UdpHaberlesme",
                    "================================"
                )


                // ----------------------------------
                // GIDEN MESAJI KAYDET
                // ----------------------------------

                mesajEkle(

                    UdpMesaj(

                        mesaj = mesaj,

                        modul =
                            modulBul(
                                mesaj
                            ),

                        ip = hedefIp,

                        giden = true
                    )
                )


                true

            } catch (
                e: Exception
            ) {

                Log.e(
                    "UdpHaberlesme",
                    "UDP GONDERME HATASI",
                    e
                )

                false
            }
        }
    }


    // ==================================================
    // MESAJ LISTESINE EKLE
    // ==================================================

    private fun mesajEkle(
        mesaj: UdpMesaj
    ) {

        val yeniListe =
            (
                    _mesajlar.value + mesaj
                    ).takeLast(
                    MAX_MESAJ
                )


        _mesajlar.value =
            yeniListe
    }


    // ==================================================
    // MESAJDAN MODUL ISMI BUL
    // ==================================================

    private fun modulBul(
        mesaj: String
    ): String {

        val parcalar =
            mesaj
                .split("|")


        if (
            parcalar.isNotEmpty() &&
            parcalar[0].startsWith(
                "SNCR_",
                ignoreCase = true
            )
        ) {

            return parcalar[0]
        }


        return ""
    }


    // ==================================================
// UDP SOCKET YENIDEN BASLAT
// ==================================================

    fun yenidenBaslat() {

        udpScope.launch {

            socketMutex.withLock {

                Log.d(
                    "UdpHaberlesme",
                    "UDP SOCKET YENIDEN BASLATILIYOR"
                )

                dinlemeJob?.cancel()

                dinlemeJob = null

                socket?.close()

                socket = null
            }

            delay(100)

            baslat()
        }
    }







    // ==================================================
    // UDP MERKEZINI DURDUR
    // ==================================================

    fun durdur() {

        Log.d(
            "UdpHaberlesme",
            "UDP MERKEZI DURDURULUYOR"
        )


        udpScope.launch {

            socketMutex.withLock {

                dinlemeJob?.cancel()

                dinlemeJob = null


                socket?.close()

                socket = null
            }
        }
    }


    // ==================================================
    // MESAJLARI TEMIZLE
    // ==================================================
    //
    // DIKKAT:
    //
    // Burada socket KAPATILMAZ.
    //
    // Sadece mesaj gecmisi temizlenir.
    // ==================================================

    fun temizle() {

        _mesajlar.value =
            emptyList()
    }
}