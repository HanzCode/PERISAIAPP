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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.perisaiapps.Model.ChatMessage
import com.example.perisaiapps.Model.User
import com.example.perisaiapps.viewmodel.ChatViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.regex.Pattern

private fun formatTimestamp(timestamp: Timestamp): String {
    // Cukup gunakan formatnya saja, ini sudah cukup
    val sdf = SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return sdf.format(timestamp.toDate())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailChatScreen(
    chatId: String,
    onNavigateBack: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToAddParticipants: (String) -> Unit,
    navController: NavController,
    viewModel: ChatViewModel = viewModel()
) {
    // --- Ambil semua state dari ViewModel di sini ---
    val messages by viewModel.messages.collectAsState()
    val chatDetails by viewModel.chatRoomDetails.collectAsState()
    val participants by viewModel.participantProfiles.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // --- State untuk mengontrol UI (Dialog, Menu) ---
    var showMenu by remember { mutableStateOf(false) }
    var showParticipantsDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }

    // --- Launcher untuk memilih file/gambar ---
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.sendImageMessage(chatId, it) } }

    val groupImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.updateGroupPhoto(chatId, it) } }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.sendFileMessage(chatId, it) } }

    // --- Effects untuk mengambil data dan auto-scroll ---
    LaunchedEffect(key1 = chatId) {
        viewModel.listenForMessages(chatId)
        viewModel.loadChatRoomAndParticipants(chatId)
        viewModel.markMessagesAsRead(chatId)
    }

    LaunchedEffect(key1 = messages) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch { listState.animateScrollToItem(0) }
        }
    }

    // --- Dialog untuk menampilkan daftar anggota ---
    if (showParticipantsDialog) {
        ParticipantListDialog(
            participants = participants,
            onDismiss = { showParticipantsDialog = false }
        )
    }

    // --- Dialog untuk mengubah nama grup ---
    if (showEditNameDialog) {
        EditGroupNameDialog(
            initialName = chatDetails?.groupName ?: "",
            onDismiss = { showEditNameDialog = false },
            onSave = { newName ->
                viewModel.updateGroupName(chatId, newName)
                showEditNameDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            val otherUser = participants.firstOrNull { it.userId != currentUserId }
            val title = if (chatDetails?.type == "GROUP") chatDetails?.groupName ?: "Grup" else otherUser?.displayName ?: "Diskusi"
            val photoUrl = if (chatDetails?.type == "GROUP") chatDetails?.groupPhotoUrl else otherUser?.photoUrl

            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary),
                            contentAlignment = Alignment.Center
                        ) {
                            if (photoUrl.isNullOrBlank()) {
                                Icon(
                                    Icons.Default.Group,
                                    contentDescription = "Foto Profil Default",
                                    tint = MaterialTheme.colorScheme.onTertiary
                                )
                            } else {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Foto Profil Chat",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = title, style = MaterialTheme.typography.titleLarge)
                    }
                },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali") } },
                actions = {
                    if (chatDetails?.type == "GROUP") {
                        Box {
                            IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, "Menu") }
                            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                DropdownMenuItem(text = { Text("Lihat Anggota") }, onClick = { showParticipantsDialog = true; showMenu = false })
                                DropdownMenuItem(text = { Text("Tambah Anggota") }, onClick = { onNavigateToAddParticipants(chatId); showMenu = false })
                                DropdownMenuItem(text = { Text("Ubah Nama Grup") }, onClick = { showEditNameDialog = true; showMenu = false })
                                DropdownMenuItem(text = { Text("Ubah Foto Grup") }, onClick = { groupImagePickerLauncher.launch("image/*"); showMenu = false })
                            }
                        }
                    }
                    IconButton(onClick = onNavigateToNotes) { Icon(Icons.Default.Note, "Lihat Catatan") }
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
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            state = listState,
            reverseLayout = true
        ) {
            items(messages, key = { it.id }) { message ->
                val senderProfile = participants.find { it.userId == message.senderId }
                MessageBubble(
                    message = message,
                    isFromCurrentUser = message.senderId == currentUserId,
                    displaySenderInfo = chatDetails?.type == "GROUP" && message.senderId != currentUserId,
                    senderProfile = senderProfile,
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
    displaySenderInfo: Boolean,
    senderProfile: User?,
    navController: NavController,
    viewModel: ChatViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start
    ) {
        if (displaySenderInfo) {
            Text(
                text = senderProfile?.displayName ?: "User",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary, // Gunakan warna aksen
                modifier = Modifier.padding(start = 48.dp, bottom = 4.dp) // Beri indentasi agar sejajar dengan gelembung
            )
        }

        Row(
            horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Tampilkan Avatar HANYA untuk pesan masuk
            if (!isFromCurrentUser) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary), contentAlignment = Alignment.Center) {
                    if(senderProfile?.photoUrl.isNullOrBlank()) {
                        Icon(Icons.Default.Person, contentDescription = "Avatar", tint = MaterialTheme.colorScheme.onTertiary)
                    } else {
                        AsyncImage(
                            model = senderProfile?.photoUrl, contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Tampilkan status UPLOADING/FAILED HANYA untuk pesan keluar
            if (isFromCurrentUser) {
                MessageStatus(message = message)
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Konten Gelembung Pesan
            MessageContent(message, isFromCurrentUser, navController, viewModel)
        }
    }
}

@Composable
private fun MessageContent(message: ChatMessage, isFromCurrentUser: Boolean, navController: NavController, viewModel: ChatViewModel) {
    val bubbleColor = if (isFromCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isFromCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .clip(RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isFromCurrentUser) 16.dp else 0.dp,
                bottomEnd = if (isFromCurrentUser) 0.dp else 16.dp
            ))
            .background(bubbleColor)
            .clickable(enabled = message.status == "SENT" && (message.type == "IMAGE" || message.type == "FILE")) {
                when (message.type) {
                    "IMAGE" -> message.imageUrl?.let { url ->
                        val encodedUrl = URLEncoder.encode(url, StandardCharsets.UTF_8.name())
                        navController.navigate("full_screen_image/$encodedUrl")
                    }
                    "FILE" -> message.fileUrl?.let { url ->
                        viewModel.downloadAndOpenFile(context, url, message.fileName ?: "file")
                    }
                    else -> {}
                }
            }
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            when (message.type) {
                "IMAGE" -> {
                    AsyncImage(
                        model = message.localUri ?: message.imageUrl,
                        contentDescription = "Gambar terkirim",
                        modifier = Modifier.heightIn(max = 250.dp).fillMaxWidth().clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
                "FILE" -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Description, "File", tint = textColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message.fileName ?: "File",
                            style = TextStyle(color = textColor),
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
                                .firstOrNull()?.let { annotation -> uriHandler.openUri(annotation.item) }
                        }
                    )
                }
            }
            Text(
                text = formatTimestamp(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = (if (isFromCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun MessageStatus(message: ChatMessage) {
    when(message.status) {
        "UPLOADING" -> CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 1.5.dp)
        "FAILED" -> Icon(Icons.Default.Warning, "Gagal terkirim", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
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
@Composable
private fun ParticipantListDialog(participants: List<User>, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Anggota Grup") },
        text = {
            LazyColumn {
                items(participants, key = { it.userId }) { user ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // --- PERBAIKAN DI SINI JUGA ---
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary),
                            contentAlignment = Alignment.Center
                        ) {
                            if (user.photoUrl.isBlank()) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Foto Default",
                                    tint = MaterialTheme.colorScheme.onTertiary
                                )
                            } else {
                                AsyncImage(
                                    model = user.photoUrl,
                                    contentDescription = "Foto ${user.displayName}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        // -------------------------

                        Spacer(modifier = Modifier.width(16.dp))
                        Text(user.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditGroupNameDialog(initialName: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var newName by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ubah Nama Grup") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("Nama grup baru") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onSave(newName) }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}