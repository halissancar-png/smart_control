@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.sancarteknik.cihazkontrol.ekranlar

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sancarteknik.cihazkontrol.R

@Composable
fun UstBaslik(
    baslik: String,
    geri: (() -> Unit)? = null
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // ✅ Yatay modda header çok küçültüldü
    val headerHeight = if (isLandscape) 40.dp else 70.dp
    val baslikYaziBoyutu = if (isLandscape) 16.sp else 20.sp
    val altYaziBoyutu = if (isLandscape) 10.sp else 12.sp
    val offsetY = if (isLandscape) 4.dp else 16.dp
    val baslikPaddingStart = if (isLandscape) 100.dp else 140.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(headerHeight)
    ) {
        Image(
            painter = painterResource(id = R.drawable.sancar_sayfa_header),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        if (geri != null) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .clickable { geri() }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = baslikPaddingStart, end = 16.dp)
                .offset(y = offsetY),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = baslik,
                fontSize = baslikYaziBoyutu,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Sancar Teknik",
                fontSize = altYaziBoyutu,
                fontWeight = FontWeight.Medium,
                color = androidx.compose.ui.graphics.Color(0xFFB9E8FF)
            )
        }
    }
}