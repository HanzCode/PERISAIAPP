package com.example.perisaiapps.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.perisaiapps.Component.GreetingSection
import com.example.perisaiapps.Component.HorizontalCardSection
import com.example.perisaiapps.Component.PeminatanSection

@Composable
fun HomeScreen(
    userName: String = "Farhan",
    navController: NavController
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B1533))
            .padding(16.dp)
            .padding(top = 18.dp) // Bisa digabung: .padding(start=16.dp, end=16.dp, bottom=16.dp, top=34.dp)
    ) {
        GreetingSection(userName)
        Spacer(modifier = Modifier.height(30.dp))
        HorizontalCardSection()
//        Spacer(modifier = Modifier.height(16.dp))
//        Text(
//            text = "Peminatan",
//            fontSize = 22.sp,
//            color = Color.White
//        )
        Spacer(modifier = Modifier.height(16.dp))
        PeminatanSection()

        // Anda bisa menggunakan navController di sini jika HomeScreen perlu melakukan navigasi
        // Contoh:
        // Button(onClick = { navController.navigate("rute_lain_di_bottom_nav") }) {
        //     Text("Navigasi dari Home")
        // }
    }
}

