package com.example.perisaiapps.ui.screen.mentor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.perisaiapps.Model.ChatMessage
import com.example.perisaiapps.ui.theme.PerisaiAppsTheme
import com.example.perisaiapps.viewmodel.ChatViewModel
import com.google.common.io.Files.append
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.regex.Pattern


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailChatScreen(
    chatId: String,
    onNavigateBack: () -> Unit,
    onNavigateToNotes: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    // Ambil pesan secara real-time
    val messages by viewModel.getMessages(chatId).collectAsState(initial = emptyList())
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Otomatis scroll ke pesan terbaru
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }
    // Akan berjalan sekali saat layar dibuka
    LaunchedEffect(key1 = chatId) {
        viewModel.markMessagesAsRead(chatId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ruang Diskusi") }, // Judul bisa diganti dengan nama lawan bicara
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToNotes) {
                        Icon(Icons.Default.Note, contentDescription = "Lihat Catatan")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            MessageInput(
                value = viewModel.messageText.value,
                onValueChange = { viewModel.messageText.value = it },
                onSendClick = { viewModel.sendMessage(chatId) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = listState,
            reverseLayout = true // Pesan baru muncul dari bawah
        ) {
            items(messages, key = { message ->

                if (message.id.isNotBlank()) message.id else "${message.timestamp}-${message.text}"
            }) { message ->
                // ===============================================================
                MessageBubble(
                    message = message,
                    isFromCurrentUser = message.senderId == currentUserId
                )
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage, isFromCurrentUser: Boolean) {
    val horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
    val bubbleColor = if (isFromCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isFromCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val uriHandler = LocalUriHandler.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(bubbleColor)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Panggil fungsi helper yang baru
            val annotatedText = buildAnnotatedStringWithLinks(message.text)

            ClickableText(
                text = annotatedText,
                // Berikan style dasar (tanpa warna) ke ClickableText
                style = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                onClick = { offset ->
                    annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            uriHandler.openUri(annotation.item)
                        }
                }
            )
        }
    }
}

@Composable
fun MessageInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ketik pesan...") },
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onSendClick,
            enabled = value.isNotBlank(),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Kirim Pesan")
        }
    }
}
@Composable
private fun buildAnnotatedStringWithLinks(
    fullText: String,
    linkStyle: SpanStyle = SpanStyle(
        color = Color(0xFF64B5F6), // Warna biru untuk link
        textDecoration = TextDecoration.Underline
    )
): AnnotatedString {
    val urlPattern = Pattern.compile(
        "(?:^|[\\W])((ht|f)tp(s?)://|www\\.)"
                + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+/?)*"
                + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]*$~@!:/{};']*)",
        Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL
    )

    val annotatedString = buildAnnotatedString {
        val matcher = urlPattern.matcher(fullText)
        var lastEnd = 0

        while (matcher.find()) {
            val startIndex = matcher.start(1)
            val endIndex = matcher.end()
            val url = fullText.substring(startIndex, endIndex)

            // Tambahkan teks biasa sebelum link ditemukan
            if (startIndex > lastEnd) {
                append(fullText.substring(lastEnd, startIndex))
            }

            // Tambahkan teks link dengan gaya dan anotasi khusus
            pushStringAnnotation(tag = "URL", annotation = url)
            withStyle(style = linkStyle) {
                append(url)
            }
            pop()

            lastEnd = endIndex
        }

        // Tambahkan sisa teks biasa setelah link terakhir
        if (lastEnd < fullText.length) {
            append(fullText.substring(lastEnd))
        }
    }
    return annotatedString
}

