package com.example.perisaiapps.Component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PeminatanSection() {
   Column () {
       Text(
           text = "Peminatan",
           fontWeight = FontWeight.Bold,
           color = Color.White,
           fontSize = 18.sp,
           modifier = Modifier.padding(bottom = 8.dp)
       )
//    Spacer(modifier = Modifier.height(18.dp))

       LazyVerticalGrid(
           columns = GridCells.Fixed(2),
           modifier = Modifier.fillMaxHeight(),
           contentPadding = PaddingValues(8.dp),
           verticalArrangement = Arrangement.spacedBy(12.dp),
           horizontalArrangement = Arrangement.spacedBy(12.dp)
       ) {
           items(6) {
               Box(
                   modifier = Modifier
                       .aspectRatio(1f)
                       .background(Color.LightGray, shape = RoundedCornerShape(12.dp))
               )
           }
       }
   }
}

@Preview
@Composable
fun PeminatanSectionPreview() {
    PeminatanSection()
}