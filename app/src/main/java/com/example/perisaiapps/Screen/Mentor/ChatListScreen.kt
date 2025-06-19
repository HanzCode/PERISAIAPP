package com.example.perisaiapps.ui.screen.mentor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.perisaiapps.Model.ChatConversation
import com.example.perisaiapps.ui.theme.PerisaiAppsDarkTheme // Import tema gelap baru kita

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(onChatClicked: (String) -> Unit) {
    val conversations = listOf(
        ChatConversation("1", "Budi Santoso", "", "Baik, Pak. Akan saya kerjakan revisinya.", "10:43", 2),
        ChatConversation("2", "Citra Lestari", "https://example.com/citra.jpg", "Terima kasih banyak atas masukannya!", "Kemarin"),
        ChatConversation("3", "David Maulana", "", "Apakah Bapak ada waktu luang besok?", "2 hari lalu", 1),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pesan", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background, // darkPurpleBlue
                    titleContentColor = MaterialTheme.colorScheme.onSurface // White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background // darkPurpleBlue
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            items(conversations, key = { it.id }) { conversation ->
                ChatListItem(
                    conversation = conversation,
                    modifier = Modifier.clickable { onChatClicked(conversation.id) }
                )
                Divider(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.padding(start = 88.dp)) // cardBackgroundColor
            }
        }
    }
}

@Composable
fun ChatListItem(
    conversation: ChatConversation,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = conversation.menteePhotoUrl,
            contentDescription = "Foto profil ${conversation.menteeName}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary) // lightGrayPlaceholder
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = conversation.menteeName,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface, // White
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = conversation.lastMessage,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant, // White 70%
                fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = conversation.timestamp,
                color = MaterialTheme.colorScheme.onSurfaceVariant, // White 70%
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (conversation.unreadCount > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary, // textColorAccent (kuning)
                    contentColor = MaterialTheme.colorScheme.onPrimary // darkPurpleBlue
                ) {
                    Text(text = conversation.unreadCount.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatListScreenPreview() {
    PerisaiAppsDarkTheme { // Gunakan wrapper tema gelap untuk preview
        ChatListScreen(onChatClicked = {})
    }
}