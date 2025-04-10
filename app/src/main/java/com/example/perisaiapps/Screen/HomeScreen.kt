package com.example.perisaiapps.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.perisaiapps.Component.GreetingSection
import com.example.perisaiapps.Component.HorizontalCardSection

@Composable
fun HomeScreen(userName: String = "Farhan"){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B1533))
            .padding(16.dp)
    ) {
        GreetingSection(userName)
        Spacer(modifier = Modifier.height(30.dp))
        HorizontalCardSection()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Peminatan",
            fontSize = 22.sp,
            color = Color.White
        )
//        PeminatanSection()
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}

