package com.example.perisaiapps.Screen

import android.R.attr.onClick
import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.perisaiapps.Model.Lomba
import com.example.perisaiapps.ViewModel.DetailLombaViewModel
import java.util.regex.Pattern

// --- Warna ---
private val darkBackground = Color(0xFF120E26)
private val cardBackground = Color(0xFF1F1A38)
private val textColorPrimary = Color.White
private val textColorSecondary = Color.White.copy(alpha = 0.7f)
private val accentColor = Color(0xFF8A2BE2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailLombaScreen(
    navController: NavController,
    lombaId: String,
    viewModel: DetailLombaViewModel = viewModel()
) {
    val lombaDetail by viewModel.lombaDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showImageDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(lombaId) {
        viewModel.fetchLombaDetail(lombaId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lombaDetail?.namaLomba ?: "Detail Lomba", color = textColorPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = textColorPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBackground)
            )
        },
        containerColor = darkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (!errorMessage.isNullOrEmpty()) {
                Text(errorMessage!!, color = Color.Red, modifier = Modifier.align(Alignment.Center).padding(16.dp))
            } else {
                // Cek jika lombaDetail tidak null sebelum menampilkan konten
                lombaDetail?.let {
                    DetailLombaContent(lomba = it, onImageClick = { showImageDialog = true })
                }
            }

            // Tombol Pendaftaran
            lombaDetail?.linkInfo?.takeIf { it.isNotBlank() }?.let {
                PendaftaranButton(
                    linkInfo = it,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(32.dp)  // Meningkatkan padding untuk memastikan tombol terlihat
                )
            }
        }

        if (showImageDialog && lombaDetail != null) {
            FullScreenImageDialog(
                imageUrl = lombaDetail!!.imageUrl,
                onDismiss = { showImageDialog = false }
            )
        }
    }
}


@Composable
private fun PendaftaranButton(linkInfo: String, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    if (linkInfo.isNotBlank()) {
        Button(
            onClick = {
                var url = linkInfo.trim()
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://$url"
                }
                try {
                    uriHandler.openUri(url)
                } catch (e: Exception) {
                    Log.e("PendaftaranButton", "Tidak dapat membuka Uri: $url", e)
                }
            },
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text("Daftar Sekarang", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DetailLombaContent(lomba: Lomba, onImageClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp) // Beri ruang untuk tombol di bawah
        ) {
            // 1. Gambar Poster Utama
            AsyncImage(
                model = lomba.imageUrl,
                contentDescription = "Poster Lomba",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(cardBackground)
                    .clickable(onClick = onImageClick),
                contentScale = ContentScale.Crop
            )

            // 2. Konten Teks di bawah gambar
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Judul dan Penyelenggara
                Text(lomba.namaLomba, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = textColorPrimary)
                InfoRow(icon = Icons.Default.CorporateFare, text = lomba.penyelenggara)
                Divider(color = textColorSecondary.copy(alpha = 0.2f))

                // Jadwal
                Text("Jadwal Penting", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColorPrimary)
                InfoRow(icon = Icons.Default.CalendarToday, text = "Pendaftaran: ${lomba.pendaftaran}")
                InfoRow(icon = Icons.Default.Event, text = "Pelaksanaan: ${lomba.pelaksanaan}")
                Divider(color = textColorSecondary.copy(alpha = 0.2f))

                // Deskripsi dengan link yang bisa diklik
                Text("Deskripsi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColorPrimary)
                LinkifiedText(text = lomba.deskripsi, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

// Composable Helper untuk Info dengan Ikon
@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = textColorSecondary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, color = textColorSecondary, style = MaterialTheme.typography.bodyMedium)
    }
}

// Composable Helper untuk mendeteksi link di dalam teks
@Composable
private fun LinkifiedText(text: String, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val annotatedString = buildAnnotatedString {
        append(text)
        // Regex untuk mendeteksi URL
        val urlMatcher = Patterns.WEB_URL.matcher(text)
        while (urlMatcher.find()) {
            val startIndex = urlMatcher.start()
            val endIndex = urlMatcher.end()
            val url = text.substring(startIndex, endIndex)

            addStyle(
                style = SpanStyle(
                    color = Color(0xFF64B5F6), // Warna biru untuk link
                    textDecoration = TextDecoration.Underline
                ),
                start = startIndex,
                end = endIndex
            )
            addStringAnnotation(
                tag = "URL",
                annotation = url,
                start = startIndex,
                end = endIndex
            )
        }
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge.copy(color = textColorPrimary),
        modifier = modifier,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    var url = annotation.item
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "https://$url"
                    }
                    try {
                        uriHandler.openUri(url)
                    } catch (e: Exception) {
                        Log.e("LinkifiedText", "Tidak dapat membuka Uri: $url", e)
                    }
                }
        }
    )
}

@Composable
private fun FullScreenImageDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Gambar Lomba Full Screen",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Tutup",
                    tint = Color.White,
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                )
            }
        }
    }
}
