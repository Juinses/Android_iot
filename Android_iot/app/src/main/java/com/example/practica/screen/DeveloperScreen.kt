package com.example.practica.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.practica.R

@Composable
fun DeveloperScreen(nav: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Datos del Desarrollador", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(20.dp))
        
        LazyColumn {
            item {
                DeveloperCard(
                    name = "Juan David Blanco Carmona",
                    role = "Estudiante de Ingeniería en Informática",
                    email = "juan.blanco11@inacapmail.cl",
                    institution = "INACAP",
                    section = "V-B50-N4-P14-C1"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = { nav.popBackStack() }) {
            Text("Volver al Menú")
        }
    }
}

@Composable
fun DeveloperCard(name: String, role: String, email: String, institution: String, section: String) {
    Card(
        modifier = Modifier.fillMaxSize().padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar Placeholder
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground), // Usando recurso por defecto
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(role, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(email, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(institution, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Text(section, style = MaterialTheme.typography.bodySmall)
        }
    }
}