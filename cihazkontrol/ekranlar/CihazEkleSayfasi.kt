@file:OptIn(ExperimentalMaterial3Api::class)

package com.sancarteknik.cihazkontrol.ekranlar

// ============================================================
// ANDROID
// ============================================================

import android.Manifest
import android.content.pm.PackageManager
import android.net.wifi.WifiManager

// ============================================================
// COMPOSE
// ============================================================

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp

// ============================================================
// LIFECYCLE (SSID'yi ON_RESUME'da yenilemek icin)
// ============================================================

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

// ============================================================
// ANDROID YARDIMCILARI
// ============================================================

import androidx.core.content.ContextCompat

// ============================================================
// VERITABANI
// ============================================================

import com.sancarteknik.cihazkontrol.veritabani.Cihaz
import com.sancarteknik.cihazkontrol.veritabani.Mekan
import com.sancarteknik.cihazkontrol.veritabani.UygulamaVeritabani

// ============================================================
// WIFI / UDP
// ============================================================

import com.sancarteknik.cihazkontrol.wifi.UdpHaberlesme
import com.sancarteknik.cihazkontrol.wifi.WifiTarayici

// ============================================================
// COROUTINE
// ============================================================

import kotlinx.coroutines.launch

import kotlinx.coroutines.delay

import android.util.Log






// ============================================================
// CIHAZ EKLEME SAYFASI
// ============================================================

@Composable
fun CihazEkleSayfasi(
    geri: () -> Unit
) {

    // ========================================================
    // TEMEL NESNELER
    // ========================================================

    val context =
        LocalContext.current

    val coroutineScope =
        rememberCoroutineScope()

    val veritabani =
        remember {
            UygulamaVeritabani.getir(context)
        }


    // ========================================================
    // MEKANLAR
    // ========================================================

    var mekanlar by remember {
        mutableStateOf<List<Mekan>>(emptyList())
    }

    var secilenMekan by remember {
        mutableStateOf<Mekan?>(null)
    }


    // ========================================================
    // CIHAZ ADI
    // ========================================================

    var cihazAdi by remember {
        mutableStateOf("")
    }


    // ========================================================
    // WIFI AG LISTESI
    // ========================================================

    var aglar by remember {
        mutableStateOf<List<String>>(emptyList())
    }

    var taraniyor by remember {
        mutableStateOf(false)
    }

    var taramaMesaji by remember {
        mutableStateOf(
            "Henuz ag taramasi yapilmadi"
        )
    }


    // ========================================================
    // SECILEN MODUL
    // Ornek:
    //
    // SNCR_O1_6E79BA
    // ========================================================

    var secilenModul by remember {
        mutableStateOf<String?>(null)
    }


    // ========================================================
    // SECILEN WIFI
    // ========================================================

    var secilenAg by remember {
        mutableStateOf<String?>(null)
    }

    var wifiSifresi by remember {
        mutableStateOf("")
    }


    // ========================================================
    // ISLEM DURUMLARI
    // ========================================================

    var gonderiliyor by remember {
        mutableStateOf(false)
    }

    var kaydediliyor by remember {
        mutableStateOf(false)
    }

    var durum by remember {
        mutableStateOf("Hazir")
    }


    // ========================================================
    // AKTIF CONFIG ISLEMI
    // ========================================================

    var configBaslangicZamani by remember {
        mutableStateOf(0L)
    }

    var configIsleniyor by remember {
        mutableStateOf(false)
    }





    // ========================================================
    // MENU DURUMLARI
    // ========================================================

    var modulListesiAcik by remember {
        mutableStateOf(false)
    }

    var agListesiAcik by remember {
        mutableStateOf(false)
    }

    var mekanListesiAcik by remember {
        mutableStateOf(false)
    }


    // ========================================================
    // KAYITLI CIHAZLAR
    //
    // Daha once kaydedilmis cihazlar burada tutulur.
    // ========================================================

    var kayitliModuller by remember {
        mutableStateOf<List<Cihaz>>(emptyList())
    }


    // ========================================================
    // VERITABANI BILGILERINI YUKLE
    // ========================================================

    LaunchedEffect(Unit) {

        mekanlar =
            veritabani
                .mekanDao()
                .tumunuGetir()

        kayitliModuller =
            veritabani
                .cihazDao()
                .tumunuGetir()
    }


    // ========================================================
    // MERKEZI UDP MESAJLARINI IZLE
    //
    // UdpHaberlesme uygulamanin ortak UDP merkezidir.
    //
    // Buradan:
    //
    // CONFIG_OK
    // CONFIG_FAIL
    //
    // gibi cevaplari aliyoruz.
    // ========================================================

    val udpMesajlar by
    UdpHaberlesme.mesajlar
        .collectAsState()


    // ========================================================
    // SON CONFIG_OK MESAJI
    //
    // ESP:
    //
    // SNCR_O1_6E79BA|CONFIG_OK|192.168.1.12
    // ========================================================

    val sonConfigOk =
        if (configIsleniyor) {

            udpMesajlar
                .asReversed()
                .firstOrNull { udpMesaj ->

                    if (udpMesaj.giden) {
                        false
                    } else {

                        val parcalar =
                            udpMesaj.mesaj
                                .trim()
                                .split("|")

                        udpMesaj.zaman > configBaslangicZamani &&
                                parcalar.size >= 3 &&
                                parcalar[0].startsWith(
                                    "SNCR_",
                                    ignoreCase = true
                                ) &&
                                parcalar[1].equals(
                                    "CONFIG_OK",
                                    ignoreCase = true
                                )
                    }
                }

        } else {

            null
        }


    // ========================================================
    // CONFIG_OK BILGILERI
    // ========================================================

    val configParcalar =
        sonConfigOk
            ?.mesaj
            ?.trim()
            ?.split("|")


    val configModul =
        configParcalar
            ?.getOrNull(0)


    val configIp =
        configParcalar
            ?.getOrNull(2)

    Log.d("CihazEkle", "sonConfigOk = ${sonConfigOk?.mesaj}")
    Log.d("CihazEkle", "configModul = $configModul")
    Log.d("CihazEkle", "configIp = $configIp")



    // ========================================================
    // SON CONFIG_FAIL MESAJINI BUL
    //
    // ESP'nin gonderdigi cevap:
    //
    // SNCR_O1_6E79BA|CONFIG_FAIL
    //
    // ========================================================

    val sonConfigFail =
        if (configIsleniyor) {

            udpMesajlar
                .asReversed()
                .firstOrNull { udpMesaj ->

                    if (udpMesaj.giden) {
                        false
                    } else {

                        val parcalar =
                            udpMesaj.mesaj
                                .trim()
                                .split("|")

                        udpMesaj.zaman > configBaslangicZamani &&
                                parcalar.size >= 2 &&
                                parcalar[0].startsWith(
                                    "SNCR_",
                                    ignoreCase = true
                                ) &&
                                parcalar[1].equals(
                                    "CONFIG_FAIL",
                                    ignoreCase = true
                                )
                    }
                }

        } else {

            null
        }





    // ========================================================
    // CONFIG_FAIL GELDIGINDE
    //
    // ESP cevap verdiyse artik "cevap bekleniyor"
    // durumunda kalmayacagiz.
    // ========================================================

    LaunchedEffect(sonConfigFail) {

        if (sonConfigFail != null) {

            gonderiliyor = false

            configIsleniyor = false

            durum =
                "Wi-Fi baglantisi basarisiz. SSID veya sifreyi kontrol edin."
        }
    }


    // ========================================================
    // CONFIG_OK GELDIGINDE
    // ========================================================

    LaunchedEffect(sonConfigOk) {

        if (sonConfigOk != null) {


            Log.d(
                "CihazEkle",
                "sonConfigOk = ${sonConfigOk.mesaj}"
            )

            Log.d(
                "CihazEkle",
                "configModul = $configModul"
            )

            Log.d(
                "CihazEkle",
                "configIp = $configIp"
            )


            gonderiliyor = false

            //configIsleniyor = false

            durum =
                "Modul yapilandirildi."
        }
    }


    // ========================================================
    // TELEFONUN BAGLI OLDUGU WIFI
    // ========================================================

    val wifiManager =
        context
            .applicationContext
            .getSystemService(
                WifiManager::class.java
            )


    // --------------------------------------------------
    // DIKKAT:
    //
    // Onceden bu deger sadece "remember { }" ile BIR KEZ
    // hesaplaniyordu. Kullanici modul secip sistem Wi-Fi
    // ayarlarina gidip modulun AP'sine baglandiktan sonra
    // uygulamaya geri dondugunde, composable yeniden
    // olusturulmedigi icin bu deger hic guncellenmiyordu.
    // Sonuc: "modulBagli" hep eski/yanlis SSID'ye gore
    // hesaplaniyor, kullanici bir turlu ilerleyemiyordu.
    //
    // Cozum: ekran her ON_RESUME oldugunda (yani kullanici
    // Wi-Fi ayarlarindan geri dondugunde) SSID'yi yeniden
    // okuyoruz.
    // --------------------------------------------------

    fun guncelSsidOku(): String {

        val bilgi =
            wifiManager.connectionInfo

        return if (
            bilgi.ssid != "<unknown ssid>"
        ) {

            bilgi.ssid
                .removePrefix("\"")
                .removeSuffix("\"")

        } else {

            ""
        }
    }

    var mevcutSSID by remember {
        mutableStateOf(guncelSsidOku())
    }

    val lifecycleOwner =
        LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {

        val gozlemci =
            LifecycleEventObserver { _, olay ->

                if (olay == Lifecycle.Event.ON_RESUME) {

                    mevcutSSID =
                        guncelSsidOku()
                }
            }

        lifecycleOwner.lifecycle
            .addObserver(gozlemci)

        onDispose {

            lifecycleOwner.lifecycle
                .removeObserver(gozlemci)
        }
    }


    // ========================================================
    // TELEFON MODULUN AP'SINE BAGLI MI?
    // ========================================================

    val modulBagli =
        secilenModul != null &&
                mevcutSSID.equals(
                    secilenModul,
                    ignoreCase = true
                )


    // ========================================================
    // MODUL VE NORMAL WIFI LISTELERI
    // ========================================================

    val moduller =
        aglar.filter {

            it.startsWith(
                "SNCR_",
                ignoreCase = true
            )
        }


    val mevcutAglar =
        aglar.filter {

            !it.startsWith(
                "SNCR_",
                ignoreCase = true
            )
        }


    // ========================================================
    // WIFI TARAMA
    // ========================================================

    fun aglariTara() {

        taraniyor = true

        taramaMesaji =
            "Aglar taraniyor..."

        // ----------------------------------------------
        // DIKKAT:
        //
        // WifiTarayici.tara() artik ASENKRON - tarama
        // sonuclari gercekten hazir oldugunda callback
        // cagriliyor (onceden startScan() sonrasi hemen
        // eski/onbellek sonuclar okunuyordu).
        // ----------------------------------------------

        WifiTarayici(context).tara { bulunanAglar ->

            aglar =
                bulunanAglar

            taraniyor = false

            val modulSayisi =
                bulunanAglar.count {

                    it.startsWith(
                        "SNCR_",
                        ignoreCase = true
                    )
                }

            val wifiSayisi =
                bulunanAglar.count {

                    !it.startsWith(
                        "SNCR_",
                        ignoreCase = true
                    )
                }

            taramaMesaji =
                "Tarama tamamlandi: " +
                        "$modulSayisi modul, " +
                        "$wifiSayisi Wi-Fi agi bulundu."
        }
    }


    // ========================================================
    // KONUM IZNI
    // ========================================================

    val izinIste =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { izinler ->

            val izinVerildi =
                izinler[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true ||
                        izinler[
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ] == true

            if (izinVerildi) {

                aglariTara()

            } else {

                taramaMesaji =
                    "Ag taramak icin konum izni gerekli."
            }
        }


    // ========================================================
    // FORMU TEMIZLE
    // ========================================================

    fun formuTemizle() {

        cihazAdi = ""

        secilenMekan = null

        secilenModul = null

        secilenAg = null

        wifiSifresi = ""

        modulListesiAcik = false

        agListesiAcik = false

        mekanListesiAcik = false

        gonderiliyor = false

        configIsleniyor = false

        kaydediliyor = false

        durum = "Hazir"
    }


    // ========================================================
    // EKRAN
    // ========================================================

    Scaffold(

        topBar = {

            TopAppBar(

                
                title = {
                    Text("Cihaz Ekle")
                },

                navigationIcon = {

                    IconButton(

                        onClick = {

                            //UdpHaberlesme.temizle()
                            //UdpHaberlesme.yenidenBaslat()

                            formuTemizle()

                            geri()
                        }

                    ) {

                        Text("<")
                    }
                }
            )
        }

    ) { padding ->

        Column(

            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(
                        rememberScrollState()
                    ),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {


            // ==================================================
            // 1. WIFI TARAMA
            // ==================================================

            Button(

                onClick = {

                    val izinGerekli =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED

                    if (izinGerekli) {

                        izinIste.launch(

                            arrayOf(

                                Manifest.permission.ACCESS_FINE_LOCATION,

                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )

                    } else {

                        aglariTara()
                    }
                },

                enabled = !taraniyor,

                modifier =
                    Modifier.fillMaxWidth()

            ) {

                Text(

                    if (taraniyor)
                        "Aglar taraniyor..."
                    else
                        "Agları Tara"
                )
            }


            Text(
                text = taramaMesaji
            )


            // ==================================================
            // 2. MODUL SEC
            // ==================================================

            Text(
                text = "Modul",
                style =
                    MaterialTheme
                        .typography
                        .titleLarge
            )


            Box(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                OutlinedButton(

                    onClick = {

                        modulListesiAcik =
                            !modulListesiAcik
                    },

                    modifier =
                        Modifier.fillMaxWidth()

                ) {

                    Text(
                        secilenModul
                            ?: "Modul seciniz"
                    )
                }


                DropdownMenu(

                    expanded =
                        modulListesiAcik,

                    onDismissRequest = {

                        modulListesiAcik = false
                    }

                ) {

                    if (moduller.isEmpty()) {

                        DropdownMenuItem(

                            text = {
                                Text(
                                    "SNCR modulu bulunamadi"
                                )
                            },

                            onClick = {
                                modulListesiAcik = false
                            }
                        )

                    } else {

                        moduller.forEach { modul ->

                            val kayitli =
                                kayitliModuller.any {

                                    it.teknikId.equals(
                                        modul,
                                        ignoreCase = true
                                    )
                                }


                            DropdownMenuItem(

                                text = {

                                    Text(

                                        if (kayitli)
                                            "$modul (Kayitli)"
                                        else
                                            modul
                                    )
                                },

                                enabled =
                                    !kayitli,

                                onClick = {

                                    secilenModul =
                                        modul

                                    secilenAg = null

                                    wifiSifresi = ""

                                    durum =
                                        "Modul secildi: $modul"

                                    modulListesiAcik =
                                        false
                                }
                            )
                        }
                    }
                }
            }


            // ==================================================
            // 3. MODUL BAGLANTI DURUMU
            // ==================================================

            if (secilenModul != null) {

                Text(

                    text =
                        if (modulBagli)
                            "Modul AP'sine bagli"
                        else
                            "Once $secilenModul agina baglanin"
                )
            }


            // ==================================================
            // 4. ANA WIFI AGINI SEC
            // ==================================================

            Text(
                text = "Wi-Fi Agi",
                style =
                    MaterialTheme
                        .typography
                        .titleLarge
            )


            Box(
                modifier =
                    Modifier.fillMaxWidth()
            ) {

                OutlinedButton(

                    onClick = {

                        if (modulBagli) {

                            agListesiAcik =
                                !agListesiAcik

                        } else {

                            durum =
                                "Once modul Wi-Fi agina baglanin"
                        }
                    },

                    modifier =
                        Modifier.fillMaxWidth()

                ) {

                    Text(
                        secilenAg
                            ?: "Wi-Fi agi seciniz"
                    )
                }


                DropdownMenu(

                    expanded =
                        agListesiAcik,

                    onDismissRequest = {

                        agListesiAcik = false
                    }

                ) {

                    if (mevcutAglar.isEmpty()) {

                        DropdownMenuItem(

                            text = {
                                Text(
                                    "Wi-Fi agi bulunamadi"
                                )
                            },

                            onClick = {
                                agListesiAcik = false
                            }
                        )

                    } else {

                        mevcutAglar.forEach { ag ->

                            DropdownMenuItem(

                                text = {
                                    Text(ag)
                                },

                                onClick = {

                                    secilenAg =
                                        ag

                                    wifiSifresi =
                                        ""

                                    durum =
                                        "Wi-Fi agi secildi: $ag"

                                    agListesiAcik =
                                        false
                                }
                            )
                        }
                    }
                }
            }


            // ==================================================
            // 5. WIFI SIFRESI
            // ==================================================

            if (secilenAg != null) {

                OutlinedTextField(

                    value =
                        wifiSifresi,

                    onValueChange = {
                        wifiSifresi = it
                    },

                    label = {
                        Text("Wi-Fi Sifresi")
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    singleLine = true
                )
            }


            // ==================================================
            // 6. CONFIG GONDER
            // ==================================================

            Button(

                onClick = {

                    val modul =
                        secilenModul

                    val ag =
                        secilenAg


                    if (!modulBagli) {

                        durum =
                            "Modul AP'sine bagli degil"

                        return@Button
                    }


                    if (
                        modul == null ||
                        ag == null ||
                        wifiSifresi.isEmpty()
                    ) {

                        durum =
                            "Modul, Wi-Fi agi ve sifre gerekli"

                        return@Button
                    }


                    coroutineScope.launch {

                        // --------------------------------------
                        // ONCE ESKI CEVAPLARI TEMIZLE
                        // --------------------------------------

                        //UdpHaberlesme.temizle()
                        //UdpHaberlesme.yenidenBaslat()

                        configBaslangicZamani =
                            System.currentTimeMillis()

                        configIsleniyor = true

                        gonderiliyor = true

                        durum =
                            "Wi-Fi bilgileri module gonderiliyor..."


                        // --------------------------------------
                        // ESP'NIN BEKLEDIGI FORMAT
                        //
                        // SNCR_O1_XXXXXX|CONFIG|SSID|SIFRE
                        // --------------------------------------

                        val mesaj =
                            "$modul|CONFIG|$ag|$wifiSifresi"


                        // --------------------------------------
                        // ESP AP ADRESI
                        // --------------------------------------

                        val basarili =
                            UdpHaberlesme.gonder(

                                "192.168.4.1",

                                mesaj
                            )


                        // --------------------------------------
                        // UDP PAKETI TELEFONDAN CIKTI
                        // --------------------------------------

                        if (basarili) {

                            durum =
                                "Bilgiler gonderildi. Cevap bekleniyor..."


                            // --------------------------------------
                            // 20 SANIYELIK ZAMAN ASIMI
                            //
                            // Bu sure icinde CONFIG_OK veya
                            // CONFIG_FAIL gelmezse bekleme
                            // durumunu sonlandiriyoruz.
                            // --------------------------------------

                            delay(20_000)


                            // --------------------------------------
                            // HALA CEVAP BEKLENIYOR MU?
                            //
                            // CONFIG_OK veya CONFIG_FAIL geldiyse
                            // yukaridaki dinleme mekanizmasi zaten
                            // gonderiliyor degerini false yapmis
                            // olacak.
                            // --------------------------------------

                            if (gonderiliyor) {

                                gonderiliyor = false

                                configIsleniyor = false

                                durum =
                                    "20 saniye icinde cevap alinamadi. Tekrar deneyin."
                            }


                        } else {

                            gonderiliyor = false

                            durum =
                                "UDP mesaji gonderilemedi"
                        }
                    }
                },

                enabled =
                    modulBagli &&
                            secilenAg != null &&
                            wifiSifresi.isNotEmpty() &&
                            !gonderiliyor,

                modifier =
                    Modifier.fillMaxWidth()

            ) {

                Text(

                    if (gonderiliyor)
                        "Cevap Bekleniyor..."
                    else
                        "Wi-Fi Bilgilerini Module Gonder"
                )
            }


            // ==================================================
            // 7. CONFIG_FAIL
            //
            // ESP:
            //
            // SNCR_O1_6E79BA|CONFIG_FAIL
            //
            // geldiginde burasi gorunur.
            // ==================================================

            if (sonConfigFail != null) {

                HorizontalDivider()


                Text(
                    text =
                        "Modul yapilandirilamadi",

                    style =
                        MaterialTheme
                            .typography
                            .titleLarge
                )


                Text(
                    text =
                        "Wi-Fi baglantisi basarisiz."
                )


                Text(
                    text =
                        "SSID veya Wi-Fi sifresini kontrol edin."
                )


                Button(

                    onClick = {

                        //UdpHaberlesme.temizle()
                        //UdpHaberlesme.yenidenBaslat()

                        gonderiliyor = false

                        durum =
                            "Yeni Wi-Fi bilgileri girebilirsiniz."
                    },

                    modifier =
                        Modifier.fillMaxWidth()

                ) {

                    Text("Tekrar Dene")
                }
            }


            // ==================================================
            // 8. CONFIG_OK
            //
            // ESP:
            //
            // SNCR_O1_6E79BA|CONFIG_OK|192.168.1.12
            //
            // geldiginde cihaz kayit bolumu gorunur.
            // ==================================================

            if (
                sonConfigOk != null &&
                configModul != null &&
                configIp != null
            ) {

                HorizontalDivider()


                Text(
                    text =
                        "Modul yapilandirildi",

                    style =
                        MaterialTheme
                            .typography
                            .titleLarge
                )


                Text(
                    text =
                        "Teknik ID: $configModul"
                )


                Text(
                    text =
                        "IP: $configIp"
                )


                // ------------------------------------------
                // CIHAZ ADI
                // ------------------------------------------

                OutlinedTextField(

                    value =
                        cihazAdi,

                    onValueChange = {
                        cihazAdi = it
                    },

                    label = {
                        Text("Cihaz Adi")
                    },

                    modifier =
                        Modifier.fillMaxWidth(),

                    singleLine = true
                )


                // ------------------------------------------
                // MEKAN SEC
                // ------------------------------------------

                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    OutlinedButton(

                        onClick = {

                            mekanListesiAcik =
                                !mekanListesiAcik
                        },

                        modifier =
                            Modifier.fillMaxWidth()

                    ) {

                        Text(
                            secilenMekan?.isim
                                ?: "Mekan seciniz"
                        )
                    }


                    DropdownMenu(

                        expanded =
                            mekanListesiAcik,

                        onDismissRequest = {

                            mekanListesiAcik = false
                        }

                    ) {

                        mekanlar.forEach { mekan ->

                            DropdownMenuItem(

                                text = {
                                    Text(mekan.isim)
                                },

                                onClick = {

                                    secilenMekan =
                                        mekan

                                    mekanListesiAcik =
                                        false
                                }
                            )
                        }
                    }
                }


                // ------------------------------------------
                // CIHAZI KAYDET
                // ------------------------------------------

                Button(

                    onClick = {

                        if (
                            cihazAdi.trim().length < 3
                        ) {

                            durum =
                                "Cihaz adi en az 3 karakter olmali"

                            return@Button
                        }


                        val mekan =
                            secilenMekan


                        if (mekan == null) {

                            durum =
                                "Mekan seciniz"

                            return@Button
                        }


                        coroutineScope.launch {

                            kaydediliyor = true


                            // ----------------------------------
                            // AYNI TEKNIK ID VAR MI?
                            // ----------------------------------

                            val mevcutCihaz =
                                veritabani
                                    .cihazDao()
                                    .teknikIdIleGetir(
                                        configModul
                                    )


                            if (mevcutCihaz != null) {

                                durum =
                                    "Bu cihaz zaten kayitli"

                                kaydediliyor = false

                                return@launch
                            }


                            // ----------------------------------
                            // ROOM'A KAYDET
                            // ----------------------------------

                            veritabani
                                .cihazDao()
                                .ekle(

                                    Cihaz(

                                        teknikId =
                                            configModul,

                                        cihazAdi =
                                            cihazAdi.trim(),

                                        wifiSSID =
                                            secilenAg ?: "",

                                        ip =
                                            configIp,

                                        mekanId =
                                            mekan.id,

                                        aktif =
                                            true
                                    )
                                )


                            // ----------------------------------
                            // ISLEM TAMAMLANDI
                            // ----------------------------------

                            kaydediliyor = false

                            //UdpHaberlesme.temizle()
                            //UdpHaberlesme.yenidenBaslat()

                            formuTemizle()

                            geri()
                        }
                    },

                    enabled =
                        !kaydediliyor,

                    modifier =
                        Modifier.fillMaxWidth()

                ) {

                    Text(

                        if (kaydediliyor)
                            "Kaydediliyor..."
                        else
                            "Cihazi Kaydet"
                    )
                }
            }


            // ==================================================
            // 9. DURUM
            // ==================================================

            HorizontalDivider()


            Text(
                text =
                    "Durum: $durum",

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium
            )
        }
    }
}