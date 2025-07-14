package com.example.perisaiapps.ui.screen.mentor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.perisaiapps.Model.ChatMessage
import com.example.perisaiapps.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern
import kotlin.coroutines.resume


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailChatScreen(
    chatId: String,
    onNavigateBack: () -> Unit,
    onNavigateToNotes: () -> Unit,
    navController: NavController,
    viewModel: ChatViewModel = viewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.sendImageMessage(chatId, it) }
    }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.sendFileMessage(chatId, it) }
    }

    LaunchedEffect(key1 = chatId) {
        viewModel.markMessagesAsRead(chatId)
        viewModel.listenForMessages(chatId)
    }
    LaunchedEffect(key1 = messages) {
        // Scroll hanya jika ada pesan baru DAN pesan pertama bukan dari user saat ini
        // atau jika pesan pertama adalah milik user saat ini dengan status SENT
        val firstMessage = messages.firstOrNull()
        if (firstMessage != null && (firstMessage.senderId != currentUserId || firstMessage.status == "SENT")) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ruang Diskusi") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToNotes) {
                        Icon(Icons.Default.Note, contentDescription = "Lihat Catatan")
                    }
                }
            )
        },
        bottomBar = {
            MessageInput(
                value = viewModel.messageText.value,
                onValueChange = { viewModel.messageText.value = it },
                onSendClick = { viewModel.sendMessage(chatId) },
                onImageClick = { imagePickerLauncher.launch("image/*") },
                onFileClick = { filePickerLauncher.launch("*/*") }
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
            reverseLayout = true
        ) {
            items(messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    isFromCurrentUser = message.senderId == currentUserId,
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isFromCurrentUser: Boolean,
    navController: NavController,
    viewModel: ChatViewModel
) {
    val horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
    val bubbleColor = if (isFromCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isFromCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (message.status == "UPLOADING" && isFromCurrentUser) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(bubbleColor)
                .clickable (enabled = message.status == "SENT") {
                    when (message.type) {
                        "IMAGE" -> {
                            message.imageUrl?.let { url ->
                                val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.name())
                                navController.navigate("full_screen_image/$encodedUrl")
                            }
                        }
                        "FILE" -> {
                            message.fileUrl?.let { url ->
                                viewModel.downloadAndOpenFile(context, url, message.fileName ?: "downloaded_file")
                            }
                        }
                    }
                }
                .padding(
                    horizontal = if (message.type == "IMAGE") 4.dp else 16.dp,
                    vertical = if (message.type == "IMAGE") 4.dp else 10.dp
                )
        ) {
            when (message.type) {
                "IMAGE" -> {
                    AsyncImage(
                        model = message.localUri ?: message.imageUrl,
                        contentDescription = "Gambar terkirim",
                        modifier = Modifier
                            .sizeIn(maxWidth = 200.dp, maxHeight = 250.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                "FILE" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = "File", tint = textColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message.fileName ?: "File",
                            color = textColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                else -> { // Teks
                    val annotatedText = buildAnnotatedStringWithLinks(message.text)
                    val uriHandler = LocalUriHandler.current
                    ClickableText(
                        text = annotatedText,
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
    }
    if (message.status == "FAILED" && isFromCurrentUser) {
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Default.Warning, contentDescription = "Gagal terkirim", tint = MaterialTheme.colorScheme.error)
    }
}


@Composable
fun MessageInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onImageClick: () -> Unit,
    onFileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onImageClick) {
            Icon(Icons.Default.Image, contentDescription = "Kirim Gambar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onFileClick) {
            Icon(Icons.Default.AttachFile, contentDescription = "Lampirkan File", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ketik pesan...") },
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
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
private fun buildAnnotatedStringWithLinks(fullText: String): AnnotatedString {
    val urlPattern = Pattern.compile(
        "(?:^|[\\W])((ht|f)tp(s?)://|www\\.)"
                + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+/?)*"
                + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]*$~@!:/{};']*)",
        Pattern.CASE_INSENSITIVE or Pattern.MULTILINE or Pattern.DOTALL
    )

    val annotatedString = buildAnnotatedString {
        append(fullText)
        val matcher = urlPattern.matcher(fullText)
        while (matcher.find()) {
            val startIndex = matcher.start(1)
            val endIndex = matcher.end()
            val url = fullText.substring(startIndex, endIndex)
            addStyle(
                style = SpanStyle(color = Color(0xFF64B5F6), textDecoration = TextDecoration.Underline),
                start = startIndex, end = endIndex
            )
            addStringAnnotation(tag = "URL", annotation = url, start = startIndex, end = endIndex)
        }
    }
    return annotatedString
}