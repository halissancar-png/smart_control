

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.sancarteknik.cihazkontrol.ekranlar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UstBaslik(
    baslik: String,
    geri: (() -> Unit)? = null
) {

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp
            ),

        shape = RoundedCornerShape(22.dp),

        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.primary
        ),

        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .height(82.dp)
                .padding(
                    horizontal = 16.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {

            // ==========================================
            // GERI BUTONU
            // ==========================================

            if (geri != null) {

                Card(

                    modifier = Modifier
                        .size(50.dp),

                    shape =
                        RoundedCornerShape(15.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                Color.White.copy(
                                    alpha = 0.16f
                                )
                        )
                ) {

                    Box(

                        modifier =
                            Modifier.fillMaxSize(),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Text(

                            text = "‹",

                            fontSize = 38.sp,

                            fontWeight =
                                FontWeight.Light,

                            color = Color.White
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.width(14.dp)
                )
            }


            // ==========================================
            // LOGO
            // ==========================================

            Box(

                modifier = Modifier
                    .size(50.dp)
                    .clip(
                        RoundedCornerShape(15.dp)
                    )
                    .background(Color.White),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(

                    text = "ST",

                    fontSize = 20.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        MaterialTheme
                            .colorScheme
                            .primary
                )
            }


            Spacer(
                modifier =
                    Modifier.width(14.dp)
            )


            // ==========================================
            // BASLIK
            // ==========================================

            Column {

                Text(

                    text = baslik,

                    fontSize = 21.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color = Color.White
                )

                Spacer(
                    modifier =
                        Modifier.height(3.dp)
                )

                Text(

                    text = "Sancar Teknik",

                    fontSize = 12.sp,

                    color =
                        Color.White.copy(
                            alpha = 0.80f
                        )
                )
            }
        }
    }
}

