package com.example.perisaiapps.Screen.AdminScreen

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.perisaiapps.Model.Lomba
import com.google.firebase.firestore.FirebaseFirestore

//Palet Warna
private val darkBackground = Color(0xFF120E26)
private val cardBackground = Color(0xFF1F1A38)
private val textColorPrimary = Color.White
private val textColorSecondary = Color.White.copy(alpha = 0.7f)
private val accentColor = Color(0xFF8A2BE2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun AddEditLombaScreen(navController: NavController, lombaId: String? = null) {
    val context = LocalContext.current
    val isEditMode = lombaId != null
    val screenTitle = if (isEditMode) "Edit Informasi Lomba" else "Tambah Lomba Baru"

    var namaLomba by remember { mutableStateOf("") }
    var penyelenggara by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var pendaftaran by remember { mutableStateOf("") }
    var pelaksanaan by remember { mutableStateOf("") }
    var linkInfo by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> selectedImageUri = uri }
    )

    LaunchedEffect(lombaId) {
        if (isEditMode && lombaId != null) {
            isLoading = true
            FirebaseFirestore.getInstance().collection("Lomba").document(lombaId).get()
                .addOnSuccessListener { doc ->
                    doc?.toObject(Lomba::class.java)?.let { lomba ->
                        namaLomba = lomba.namaLomba
                        penyelenggara = lomba.penyelenggara
                        deskripsi = lomba.deskripsi
                        pendaftaran = lomba.pendaftaran
                        pelaksanaan = lomba.pelaksanaan
                        linkInfo = lomba.linkInfo
                        existingImageUrl = lomba.imageUrl.ifBlank { null }
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(context, "Gagal memuat data.", Toast.LENGTH_SHORT).show()
                    navController.navigateUp()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle, color = textColorPrimary) },
                navigationIcon = { IconButton(onClick = { navController.navigateUp() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = textColorPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBackground)
            )
        },
        containerColor = darkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CustomLombaTextField(value = namaLomba, onValueChange = { namaLomba = it }, label = "Nama Lomba")
            CustomLombaTextField(value = penyelenggara, onValueChange = { penyelenggara = it }, label = "Penyelenggara")
            CustomLombaTextField(
                value = deskripsi,
                onValueChange = { deskripsi = it },
                label = "Deskripsi Singkat",
                singleLine = false,
                modifier = Modifier.defaultMinSize(minHeight = 120.dp)
            )
            CustomLombaTextField(value = pendaftaran, onValueChange = { pendaftaran = it }, label = "Info Pendaftaran (misal: Deadline)")
            CustomLombaTextField(value = pelaksanaan, onValueChange = { pelaksanaan = it }, label = "Info Pelaksanaan (misal: Tanggal)")
            CustomLombaTextField(value = linkInfo, onValueChange = { linkInfo = it }, label = "Link Info / Pendaftaran", imeAction = ImeAction.Done)

            Spacer(Modifier.height(8.dp))
            Text("Poster Lomba", style = MaterialTheme.typography.titleMedium, color = textColorPrimary)
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)).background(cardBackground).clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val imageToShow = selectedImageUri ?: existingImageUrl?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }
                if (imageToShow != null) {
                    AsyncImage(model = imageToShow, "Poster Lomba", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.ImageSearch, "Pilih Poster", modifier = Modifier.size(48.dp), tint = textColorSecondary)
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (namaLomba.isBlank() || penyelenggara.isBlank()) {
                        Toast.makeText(context, "Nama Lomba dan Penyelenggara wajib diisi!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    isLoading = true
                    saveLombaData(
                        context, lombaId, namaLomba, penyelenggara, deskripsi, pendaftaran, pelaksanaan, linkInfo,
                        selectedImageUri, existingImageUrl,
                        onSuccess = {
                            isLoading = false
                            Toast.makeText(context, "Data lomba berhasil disimpan", Toast.LENGTH_SHORT).show()
                            navController.navigate("admin_manage_lomba_route") { popUpTo("admin_manage_lomba_route") { inclusive = true } }
                        },
                        onFailure = { error ->
                            isLoading = false
                            Toast.makeText(context, "Gagal menyimpan: ${error.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White) else Text(if (isEditMode) "Simpan Perubahan" else "Tambah Lomba")
            }
        }
    }
}
@Composable
private fun CustomLombaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    imeAction: ImeAction = ImeAction.Next
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = imeAction),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColorPrimary,
            unfocusedTextColor = textColorPrimary,
            cursorColor = accentColor,
            focusedBorderColor = accentColor,
            unfocusedBorderColor = textColorSecondary,
            focusedLabelColor = accentColor,
            unfocusedLabelColor = textColorSecondary,
            focusedContainerColor = cardBackground,
            unfocusedContainerColor = cardBackground
        )
    )
}

private fun saveLombaData(
    context: Context, lombaId: String?, namaLomba: String, penyelenggara: String, deskripsi: String, pendaftaran: String,
    pelaksanaan: String, linkInfo: String, selectedImageUri: Uri?, existingImageUrl: String?,
    onSuccess: () -> Unit, onFailure: (Exception) -> Unit
) {
    if (selectedImageUri != null) {
        MediaManager.get().upload(selectedImageUri).unsigned("perisai_mentor").option("cloud_name", "duaqqcjmr")
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val imageUrl = resultData["secure_url"] as? String
                    proceedToSaveLomba(lombaId, namaLomba, penyelenggara, deskripsi, pendaftaran, pelaksanaan, linkInfo, imageUrl, onSuccess, onFailure)
                }
                override fun onError(requestId: String, error: ErrorInfo) { onFailure(Exception(error.description)) }
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch(context)
    } else {
        proceedToSaveLomba(lombaId, namaLomba, penyelenggara, deskripsi, pendaftaran, pelaksanaan, linkInfo, existingImageUrl, onSuccess, onFailure)
    }
}

private fun proceedToSaveLomba(
    lombaId: String?, namaLomba: String, penyelenggara: String, deskripsi: String, pendaftaran: String,
    pelaksanaan: String, linkInfo: String, finalImageUrl: String?,
    onSuccess: () -> Unit, onFailure: (Exception) -> Unit
) {
    val lombaData = mapOf(
        "namaLomba" to namaLomba,
        "penyelenggara" to penyelenggara,
        "deskripsi" to deskripsi,
        "pendaftaran" to pendaftaran,
        "pelaksanaan" to pelaksanaan,
        "linkInfo" to linkInfo,
        "imageUrl" to (finalImageUrl ?: "")
    )

    val db = FirebaseFirestore.getInstance().collection("Lomba")
    val task = if (lombaId != null) {
        db.document(lombaId).update(lombaData)
    } else {
        db.add(lombaData)
    }

    task.addOnSuccessListener { onSuccess() }.addOnFailureListener { onFailure(it) }
}