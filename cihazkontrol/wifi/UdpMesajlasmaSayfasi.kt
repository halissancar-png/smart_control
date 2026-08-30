package com.sancarteknik.cihazkontrol.ekranlar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sancarteknik.cihazkontrol.wifi.UdpHaberlesme
import com.sancarteknik.cihazkontrol.wifi.UdpMesaj
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UdpMesajlasmaSayfasi(
    geri: () -> Unit
) {

    // ==================================================
    // MERKEZDEKI UDP MESAJLARINI AL
    // ==================================================
    //
    // Burada UDP dinleme BASLATILMAZ.
    //
    // UDP merkezini MainActivity baslatir.
    //
    // Bu ekran sadece merkezde bulunan mesajlari
    // goruntuler.
    // ==================================================

    val mesajlar by
    UdpHaberlesme.mesajlar.collectAsState()


    // ==================================================
    // LISTE DURUMU
    // ==================================================

    val listeDurumu =
        rememberLazyListState()


    // ==================================================
    // YENI MESAJ GELINCE EN ALTA GIT
    // ==================================================

    LaunchedEffect(
        mesajlar.size
    ) {

        if (
            mesajlar.isNotEmpty()
        ) {

            listeDurumu.animateScrollToItem(
                mesajlar.lastIndex
            )
        }
    }


    // ==================================================
    // EKRAN
    // ==================================================

    Column(
        modifier =
            Modifier.fillMaxSize()
    ) {

        // ==================================================
        // UST BAR
        // ==================================================

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            TextButton(
                onClick = geri
            ) {

                Text("<")
            }


            Spacer(
                modifier =
                    Modifier.width(8.dp)
            )


            Column {

                Text(
                    text = "Veri Takibi",

                    style =
                        MaterialTheme
                            .typography
                            .titleLarge
                )


                Text(
                    text =
                        "UDP merkezi aktif • Port 4210",

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall
                )
            }
        }


        HorizontalDivider()


        // ==================================================
        // MESAJLAR
        // ==================================================

        if (
            mesajlar.isEmpty()
        ) {

            Box(
                modifier =
                    Modifier.fillMaxSize(),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text =
                        "Henuz UDP mesaji yok."
                )
            }

        } else {

            LazyColumn(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = 8.dp,
                            vertical = 8.dp
                        ),

                state =
                    listeDurumu,

                verticalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {

                items(

                    items =
                        mesajlar,

                    key = {
                        it.zaman
                    }

                ) { mesaj ->

                    UdpMesajBalonu(
                        mesaj = mesaj
                    )
                }
            }
        }
    }
}


// ==================================================
// UDP MESAJ BALONU
// ==================================================

@Composable
private fun UdpMesajBalonu(
    mesaj: UdpMesaj
) {

    // ==================================================
    // SAAT
    // ==================================================

    val saat =
        SimpleDateFormat(
            "HH:mm:ss",
            Locale.getDefault()
        ).format(
            Date(
                mesaj.zaman
            )
        )


    // ==================================================
    // BASLIK
    // ==================================================

    val baslik =
        if (
            mesaj.giden
        ) {

            if (
                mesaj.modul.isNotEmpty()
            ) {

                "${mesaj.modul} -> gonderildi"

            } else {

                "Gonderildi"
            }

        } else {

            if (
                mesaj.modul.isNotEmpty()
            ) {

                "${mesaj.modul} -> alindi"

            } else {

                "Cihazdan alindi"
            }
        }


    // ==================================================
    // BALON KONUMU
    // ==================================================
    //
    // Giden  : SOL
    // Gelen  : SAG
    // ==================================================

    Row(

        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            if (
                mesaj.giden
            ) {

                Arrangement.Start

            } else {

                Arrangement.End
            }
    ) {

        Box(

            modifier =
                Modifier
                    .fillMaxWidth(0.82f)

                    .background(

                        color =
                            if (
                                mesaj.giden
                            ) {

                                MaterialTheme
                                    .colorScheme
                                    .primaryContainer

                            } else {

                                MaterialTheme
                                    .colorScheme
                                    .secondaryContainer
                            },

                        shape =
                            MaterialTheme
                                .shapes
                                .medium
                    )

                    .padding(
                        10.dp
                    )
        ) {

            Column {

                // ======================================
                // MODUL
                // ======================================

                Text(

                    text =
                        baslik,

                    style =
                        MaterialTheme
                            .typography
                            .labelMedium
                )


                // ======================================
                // MESAJ
                // ======================================

                Text(

                    text =
                        mesaj.mesaj,

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    modifier =
                        Modifier.padding(
                            top = 3.dp
                        )
                )


                // ======================================
                // SAAT + IP
                // ======================================

                Row(

                    modifier =
                        Modifier.padding(
                            top = 5.dp
                        )
                ) {

                    Text(

                        text =
                            saat,

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall
                    )


                    if (
                        mesaj.ip.isNotEmpty()
                    ) {

                        Spacer(
                            modifier =
                                Modifier.width(
                                    8.dp
                                )
                        )


                        Text(

                            text =
                                mesaj.ip,

                            style =
                                MaterialTheme
                                    .typography
                                    .labelSmall
                        )
                    }
                }
            }
        }
    }
}