package com.example.perisaiapps.Screen // Sesuaikan package Anda

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit // Import Icon Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview // Untuk preview jika dibutuhkan
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.perisaiapps.Model.Mentor // Pastikan path model benar
import com.example.perisaiapps.viewmodel.UserRole
import com.example.perisaiapps.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// --- Warna (ambil dari definisi sebelumnya atau sesuaikan) ---
private val darkPurpleBlue = Color(0xFF120E26)
private val cardBackgroundColor = Color(0xFF1F1A38)
private val lightGrayPlaceholder = Color(0xFF4A4A5A)
private val buttonBackgroundColor = Color(0xFFD0D0D0)
private val buttonTextColor = Color(0xFF120E26)
private val textColorPrimary = Color.White
private val textColorSecondary = Color.White.copy(alpha = 0.7f)
private val textColorAccent = Color(0xFFFDD835)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailMentorScreen(
    navController: NavController,
    mentorId: String,
    userViewModel: UserViewModel = viewModel()
) {
    var mentorDetail by remember { mutableStateOf<Mentor?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // State untuk edit langsung di halaman ini
    var currentAvailability by remember(mentorDetail?.isAvailable) { mutableStateOf(mentorDetail?.isAvailable ?: true) }
    var showEditDeskripsiDialog by remember { mutableStateOf(false) }
    var tempDeskripsi by remember(mentorDetail?.deskripsi) { mutableStateOf(mentorDetail?.deskripsi ?: "") }

    val currentUserRole = userViewModel.userRole.value
    val currentAuthUserId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current

    LaunchedEffect(mentorId) {
        if (mentorId.isNotEmpty()) {
            isLoading = true
            errorMessage = null
            FirebaseFirestore.getInstance().collection("Mentor").document(mentorId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val fetchedMentor = document.toObject(Mentor::class.java)
                        mentorDetail = fetchedMentor
                        fetchedMentor?.let {
                            currentAvailability = it.isAvailable
                            tempDeskripsi = it.deskripsi // Inisialisasi tempDeskripsi
                        }
                        Log.d("DetailMentorScreen", "Data mentor ditemukan: ${mentorDetail?.name}")
                    } else {
                        errorMessage = "Data mentor tidak ditemukan."
                        Log.w("DetailMentorScreen", "Dokumen tidak ditemukan untuk Mentor ID: $mentorId")
                    }
                    isLoading = false
                }
                .addOnFailureListener { exception ->
                    errorMessage = "Gagal mengambil detail: ${exception.message}"
                    Log.e("DetailMentorScreen", "Error mengambil detail mentor", exception)
                    isLoading = false
                }
        } else {
            errorMessage = "ID Mentor tidak valid."
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        if (userViewModel.userRole.value == UserRole.UNKNOWN && currentAuthUserId != null) {
            userViewModel.fetchUserRole()
        }
    }

    // Tentukan apakah pengguna saat ini bisa mengedit profil mentor ini
    val canEditThisProfile = currentUserRole == UserRole.ADMIN ||
            (currentUserRole == UserRole.MENTOR && mentorDetail?.userId == currentAuthUserId && currentAuthUserId != null)


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mentorDetail?.name ?: "Detail Mentor", color = textColorPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = textColorPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = darkPurpleBlue)
            )
        },
        containerColor = darkPurpleBlue
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = textColorPrimary)
                errorMessage != null -> Text(
                    text = errorMessage ?: "Terjadi kesalahan.", // Lebih aman
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    textAlign = TextAlign.Center
                )
                mentorDetail != null -> {
                    val mentor = mentorDetail!! // Aman karena sudah dicek not null
                    DetailMentorContentLayout(
                        mentor = mentor,
                        currentAvailabilityFromState = currentAvailability,
                        canEditProfile = canEditThisProfile, // Gunakan variabel yang sudah dikonsolidasi
                        onAvailabilityChange = { newStatus ->
                            updateMentorSingleField(
                                mentorId = mentor.id,
                                fieldName = "isAvailable", // Nama field di Firestore
                                newValue = newStatus,
                                context = context,
                                onSuccess = {
                                    currentAvailability = newStatus
                                    mentorDetail = mentorDetail?.copy(isAvailable = newStatus)
                                    Toast.makeText(context, "Status ketersediaan diperbarui", Toast.LENGTH_SHORT).show()
                                },
                                onFailure = { errorMsg ->
                                    Toast.makeText(context, "Gagal: $errorMsg", Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        onEditDeskripsiClick = {
                            tempDeskripsi = mentor.deskripsi // Update tempDeskripsi dengan nilai terbaru sebelum buka dialog
                            showEditDeskripsiDialog = true
                        }
                    )

                    if (showEditDeskripsiDialog && canEditThisProfile) { // Hanya tampilkan dialog jika bisa edit
                        EditDeskripsiDialog(
                            initialDeskripsi = tempDeskripsi,
                            onDismiss = { showEditDeskripsiDialog = false },
                            onSave = { newDeskripsi ->
                                updateMentorSingleField(
                                    mentorId = mentor.id,
                                    fieldName = "deskripsi", // Nama field di Firestore
                                    newValue = newDeskripsi,
                                    context = context,
                                    onSuccess = {
                                        mentorDetail = mentorDetail?.copy(deskripsi = newDeskripsi)
                                        tempDeskripsi = newDeskripsi
                                        showEditDeskripsiDialog = false
                                        Toast.makeText(context, "Deskripsi berhasil diperbarui", Toast.LENGTH_SHORT).show()
                                    },
                                    onFailure = { errorMsg ->
                                        Toast.makeText(context, "Gagal: $errorMsg", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        )
                    }
                }
                else -> Text("Tidak ada data untuk ditampilkan.", color = textColorSecondary, modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun DetailMentorContentLayout(
    mentor: Mentor,
    currentAvailabilityFromState: Boolean,
    canEditProfile: Boolean, // Diubah dari canEditStatus
    onAvailabilityChange: (Boolean) -> Unit,
    onEditDeskripsiClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 80.dp)
    ) {
        MentorProfileHeader(mentor = mentor)
        Spacer(modifier = Modifier.height(24.dp))
        Divider(color = textColorSecondary.copy(alpha = 0.2f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // Status Ketersediaan
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Status Ketersediaan:", style = MaterialTheme.typography.titleSmall.copy(color = textColorSecondary)) // Diubah ke secondary untuk label
                Text(
                    if (currentAvailabilityFromState) "Bersedia" else "Sibuk",
                    color = if (currentAvailabilityFromState) Color(0xFF00C853) else Color(0xFFD50000),
                    fontWeight = FontWeight.Bold, fontSize = 18.sp
                )
            }
            if (canEditProfile) {
                Switch(
                    checked = currentAvailabilityFromState,
                    onCheckedChange = onAvailabilityChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF00C853).copy(alpha = 0.7f),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFD50000).copy(alpha = 0.7f)
                    )
                )
            }
        }

        // Tombol Edit Deskripsi (jika bisa edit)
        if (canEditProfile) {
            OutlinedButton(
                onClick = onEditDeskripsiClick,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = textColorPrimary),
                border = BorderStroke(1.dp, textColorSecondary.copy(alpha = 0.5f)) // Border lebih soft
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Deskripsi", modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Edit Deskripsi & Info Lainnya") // Teks tombol disesuaikan
            }
        }

        Divider(color = textColorSecondary.copy(alpha = 0.2f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(24.dp))
        PrestasiSection(achievements = mentor.achievements ?: emptyList())
    }

    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp), contentAlignment = Alignment.BottomCenter) {
        HubungiMentorButton(onClick = { /* TODO */ })
    }
}

// --- Composable untuk Dialog Edit Deskripsi ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDeskripsiDialog(
    initialDeskripsi: String,
    onDismiss: () -> Unit,
    onSave: (newDeskripsi: String) -> Unit
) {
    var text by remember(initialDeskripsi) { mutableStateOf(initialDeskripsi) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Deskripsi & Info Universitas") }, // Judul dialog disesuaikan
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Deskripsi & Info Universitas") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 150.dp, max = 300.dp),
                maxLines = 10, // Izinkan beberapa baris
                colors = OutlinedTextFieldDefaults.colors( // Sesuaikan warna textfield
                    focusedBorderColor = textColorAccent,
                    unfocusedBorderColor = textColorSecondary,
                    cursorColor = textColorAccent,
                    focusedLabelColor = textColorAccent,
                    unfocusedLabelColor = textColorSecondary,
                    focusedTextColor = textColorPrimary,
                    unfocusedTextColor = textColorPrimary,
                    focusedPlaceholderColor = textColorSecondary,
                    unfocusedPlaceholderColor = textColorSecondary,
                    disabledTextColor = textColorSecondary,
                    disabledBorderColor = textColorSecondary,
                    disabledLabelColor = textColorSecondary,
                    disabledPlaceholderColor = textColorSecondary,
                    errorBorderColor = Color.Red,
                    errorLabelColor = Color.Red,
                    errorCursorColor = Color.Red
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(text.trim()) }, // Trim teks sebelum simpan
                colors = ButtonDefaults.buttonColors(containerColor = textColorAccent, contentColor = darkPurpleBlue)
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = textColorAccent)
            ) {
                Text("Batal")
            }
        },
        containerColor = cardBackgroundColor,
        titleContentColor = textColorPrimary,
        textContentColor = textColorPrimary // Agar label OutlinedTextField juga terlihat baik
    )
}

// Fungsi helper generik untuk update satu field di Firestore
private fun updateMentorSingleField(
    mentorId: String,
    fieldName: String,
    newValue: Any,
    context: Context,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    FirebaseFirestore.getInstance().collection("Mentor").document(mentorId)
        .update(fieldName, newValue)
        .addOnSuccessListener {
            Log.d("FirestoreUpdate", "Mentor field '$fieldName' updated successfully for $mentorId.")
            onSuccess()
        }
        .addOnFailureListener { e ->
            Log.e("FirestoreUpdate", "Error updating mentor field '$fieldName' for $mentorId.", e)
            onFailure(e.message ?: "Gagal memperbarui data.")
        }
}


// --- Composable lainnya (MentorProfileHeader, PrestasiSection, HubungiMentorButton) ---
// Pastikan implementasinya sudah ada dan sesuai dengan contoh sebelumnya.
// Saya akan sertakan lagi di bawah untuk kelengkapan, dengan sedikit penyesuaian warna.

@Composable
private fun MentorProfileHeader(mentor: Mentor) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        AsyncImage(
            model = mentor.photoUrl,
            contentDescription = "Foto ${mentor.name}",
            modifier = Modifier
                .width(100.dp)
                .aspectRatio(3f / 4f)
                .clip(RoundedCornerShape(12.dp))
                .background(lightGrayPlaceholder),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(16.dp))
        if (mentor.deskripsi.isNotBlank()) {
            Text(
                text = mentor.deskripsi,
                fontSize = 13.sp,
                color = textColorSecondary,
                lineHeight = 18.sp,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = mentor.name,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = textColorPrimary
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = mentor.peminatan,
        fontSize = 14.sp,
        color = textColorSecondary.copy(alpha = 0.8f)
    )
}

@Composable
private fun PrestasiSection(achievements: List<String>) {
    Column {
        Text(
            text = "Prestasi:",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColorPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        if (achievements.isEmpty()) {
            Text(
                text = "• Belum ada prestasi yang dicantumkan.",
                fontSize = 14.sp,
                color = textColorSecondary,
                modifier = Modifier.padding(start = 8.dp)
            )
        } else {
            achievements.forEach { achievement ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(bottom = 6.dp, start = 8.dp)
                ) {
                    Text(
                        text = "• ",
                        fontSize = 16.sp,
                        color = textColorPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.offset(y = (-1).dp)
                    )
                    Text(
                        text = achievement,
                        fontSize = 14.sp,
                        color = textColorPrimary,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun HubungiMentorButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonBackgroundColor,
            contentColor = buttonTextColor
        )
    ) {
        Text(
            text = "Hubungi Mentor",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun DetailMentorScreenContentPreviewFull() {
    MaterialTheme {
        Surface(color = darkPurpleBlue, modifier = Modifier.fillMaxSize()) {
            DetailMentorContentLayout(
                mentor = Mentor(
                    id = "previewId", userId = "previewUserId", name = "Dr. Preview Mentor, S.T., M.Eng.",
                    peminatan = "Machine Learning & AI",
                    deskripsi = "Seorang akademisi dan praktisi di bidang AI dengan pengalaman industri. Telah membimbing berbagai proyek inovatif. Asal dari Universitas Coding Nusantara.",
                    photoUrl = "",
                    isAvailable = true,
                    achievements = listOf("Best Paper Award ICCE 2024", "Top Innovator Grant 2023", "Speaker di AI Summit Global")
                ),
                currentAvailabilityFromState = true,
                canEditProfile = true,
                onAvailabilityChange = {},
                onEditDeskripsiClick = {}
            )
        }
    }
}