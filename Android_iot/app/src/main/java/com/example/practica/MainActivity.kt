package com.example.practica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.practica.nav.AppNavGraph
import com.example.practica.ui.theme.PracticaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PracticaTheme { AppNavGraph() }
        }
    }
}


