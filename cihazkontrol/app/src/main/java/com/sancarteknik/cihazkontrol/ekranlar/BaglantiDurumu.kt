package com.sancarteknik.cihazkontrol.ekranlar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.net.wifi.WifiManager
import com.sancarteknik.cihazkontrol.wifi.UdpHaberlesme
import java.net.InetAddress







@Composable
fun BaglantiDurumu(
    wifiBagli: Boolean,        // ✅ Tip belirtildi
    internetBagli: Boolean,    // ✅ Tip belirtildi
    udpBagli: Boolean          // ✅ Tip belirtildi
) {
    Row {
        if (wifiBagli) {
            Text(text = "📶", fontSize = 22.sp)
            Spacer(modifier = Modifier.width(4.dp))
        }
        if (internetBagli) {
            Text(text = "🌐", fontSize = 22.sp)
            Spacer(modifier = Modifier.width(4.dp))
        }
        if (udpBagli) {
            Text(text = "📡", fontSize = 22.sp)
        }
    }
}