package com.sancarteknik.cihazkontrol.ekranlar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BasitSayfa(
    baslik: String,
    geri: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Button(onClick = geri) {
            Text("Geri")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = baslik,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}