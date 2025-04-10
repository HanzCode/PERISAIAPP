package com.example.perisaiapps.Component

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun HorizontalCardSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(Color(0xFF3E3264), shape = RoundedCornerShape(16.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)

    ) {
        repeat(5) {
            Box(
                modifier = Modifier
                    .size(115.dp, 140.dp)
                    .background(Color.Gray, shape = RoundedCornerShape(16.dp))
                    .padding(8.dp)

            )
        }
    }
}

@Preview
@Composable
fun HorizontalCardSectionPreview() {
    HorizontalCardSection()
}