package com.example.practica.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.practica.ui.theme.PracticaTheme

@Composable
fun HomeScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("¡Hola, usuario!", fontSize = 22.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    PracticaTheme {
        HomeScreen()
    }
}