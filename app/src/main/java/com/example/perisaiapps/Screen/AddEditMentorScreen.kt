package com.example.perisaiapps.Screen.admin

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.perisaiapps.Model.Mentor
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// --- Konstanta untuk Cloudinary (GANTI DENGAN NILAI ANDA!) ---
private const val YOUR_CLOUD_NAME = "duaqqcjmr"
private const val YOUR_UNSIGNED_UPLOAD_PRESET = "perisai_mentor"
// --- ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMentorScreen(
    navController: NavController,
    mentorId: String? = null // Null jika mode 'Tambah Baru', ada isinya jika mode 'Edit'
) {
    val context = LocalContext.current

    // State untuk input fields
    var name by remember { mutableStateOf("") }
    var peminatan by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var availableUntilString by remember { mutableStateOf("") }

    // State untuk gambar
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingPhotoUrl by remember { mutableStateOf<String?>(null) } // Untuk URL foto saat ini (mode edit)

    var isLoading by remember { mutableStateOf(false) }
    val isEditMode = mentorId != null
    val screenTitle = if (isEditMode) "Edit Mentor" else "Tambah Mentor Baru"

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            selectedImageUri = uri
        }
    )

    // Load data mentor jika dalam mode edit
    LaunchedEffect(mentorId) {
        if (isEditMode && mentorId != null) {
            isLoading = true
            FirebaseFirestore.getInstance().collection("Mentor").document(mentorId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val mentor = document.toObject(Mentor::class.java)
                        mentor?.let {
                            name = it.name
                            peminatan = it.peminatan
                            deskripsi = it.Deskripsi // Perhatikan D kapital jika model Anda begitu
                            existingPhotoUrl = it.photoUrl.ifBlank { null } // Simpan URL foto yang sudah ada
                            it.availableUntil?.toDate()?.let { date ->
                                availableUntilString = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
                            }
                            Log.d("AddEditMentorScreen", "Data mentor dimuat untuk diedit: ${it.name}")
                        }
                    } else {
                        Log.w("AddEditMentorScreen", "Dokumen mentor tidak ditemukan untuk ID: $mentorId")
                        Toast.makeText(context, "Data mentor tidak ditemukan.", Toast.LENGTH_LONG).show()
                        navController.navigateUp() // Kembali jika data tidak ditemukan
                    }
                    isLoading = false
                }
                .addOnFailureListener { exception ->
                    Log.e("AddEditMentorScreen", "Gagal mengambil data mentor untuk ID: $mentorId", exception)
                    Toast.makeText(context, "Gagal mengambil data mentor: ${exception.message}", Toast.LENGTH_LONG).show()
                    isLoading = false
                    navController.navigateUp()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Mentor") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                singleLine = true
            )
            OutlinedTextField(
                value = peminatan,
                onValueChange = { peminatan = it },
                label = { Text("Bidang Peminatan") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                singleLine = true
            )
            OutlinedTextField(
                value = deskripsi,
                onValueChange = { deskripsi = it },
                label = { Text("Deskripsi Mentor") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = KeyboardType.Text)
            )

            // --- Bagian Pilih dan Preview Gambar ---
            Spacer(modifier = Modifier.height(8.dp))
            Text("Foto Mentor", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.3f))
                    .border(1.dp, Color.Gray, CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val imageToShow = selectedImageUri ?: existingPhotoUrl?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }

                if (imageToShow != null) {
                    AsyncImage(
                        model = imageToShow,
                        contentDescription = "Preview Foto Mentor",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Face,
                        contentDescription = "Pilih Foto",
                        modifier = Modifier.size(48.dp),
                        tint = Color.Gray
                    )
                }
            }

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Pilih Foto")
            }
            // --- Akhir Bagian Pilih dan Preview Gambar ---

            OutlinedTextField(
                value = availableUntilString,
                onValueChange = { availableUntilString = it },
                label = { Text("Tersedia Hingga (DD/MM/YYYY HH:MM)") },
                placeholder = { Text("Contoh: 25/12/2025 17:00") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isBlank() || peminatan.isBlank() || deskripsi.isBlank()) {
                        Toast.makeText(context, "Nama, Peminatan, dan Deskripsi wajib diisi!", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    val availableUntilTimestamp: Timestamp? = try {
                        if (availableUntilString.isNotBlank()) {
                            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            sdf.parse(availableUntilString)?.let { Timestamp(it) } // Konversi ke Timestamp jika parse berhasil
                        } else { null }
                    } catch (e: Exception) {
                        Log.e("AddEditMentorScreen", "Error parsing date: ${e.message}")
                        Toast.makeText(context, "Format tanggal/waktu salah.", Toast.LENGTH_LONG).show()
                        null // Kembalikan null jika error
                    }

                    if (availableUntilString.isNotBlank() && availableUntilTimestamp == null) {
                        // Jika string tanggal diisi tapi parsing gagal, jangan lanjutkan
                        isLoading = false // Pastikan isLoading direset
                        return@Button
                    }

                    // Panggil fungsi untuk menyimpan data (dengan upload Cloudinary)
                    saveMentorDataWithCloudinary(
                        context = context,
                        cloudName = "duaqqcjmr",
                        uploadPreset = "perisai_mentor",
                        mentorDocumentId = mentorId, // ID dokumen mentor jika mode edit
                        name = name.trim(),
                        peminatan = peminatan.trim(),
                        deskripsi = deskripsi.trim(),
                        selectedImageUri = selectedImageUri,
                        existingPhotoUrl = existingPhotoUrl,
                        availableUntil = availableUntilTimestamp,
                        onSuccess = { returnedPhotoUrl -> // Callback saat sukses semua
                            isLoading = false
                            Log.d("AddEditMentorScreen", "Operasi mentor sukses. URL Foto: $returnedPhotoUrl")
                            Toast.makeText(context, if (isEditMode) "Mentor berhasil diperbarui!" else "Mentor berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                            navController.navigateUp()
                        },
                        onFailure = { exception -> // Callback saat ada kegagalan
                            isLoading = false
                            Log.e("AddEditMentorScreen", "Operasi mentor gagal", exception)
                            Toast.makeText(context, "Gagal: ${exception.message}", Toast.LENGTH_LONG).show()
                        },
                        setLoading = { loadingState -> isLoading = loadingState } // Untuk kontrol UI loading
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (isEditMode) "Simpan Perubahan" else "Tambah Mentor")
                }
            }
        }
    }
}

// Fungsi helper untuk menyimpan data mentor dan menghandle upload gambar Cloudinary
private fun saveMentorDataWithCloudinary(
    context: Context,
    cloudName: String,
    uploadPreset: String,
    mentorDocumentId: String?, // Ini adalah ID dokumen dari collection "Mentor" (untuk edit)
    name: String,
    peminatan: String,
    deskripsi: String,
    selectedImageUri: Uri?,
    existingPhotoUrl: String?, // URL foto lama jika ada (untuk edit)
    availableUntil: Timestamp?,
    onSuccess: (newOrExistingPhotoUrl: String) -> Unit,
    onFailure: (Exception) -> Unit,
    setLoading: (Boolean) -> Unit
) {
    setLoading(true) // Mulai loading

    if (cloudName.equals("NAMA_CLOUD_ANDA", ignoreCase = true) ||
        uploadPreset.equals("NAMA_UPLOAD_PRESET_ANDA", ignoreCase = true) ||
        cloudName.isBlank() || // Tambahan: cek juga jika kosong
        uploadPreset.isBlank()  // Tambahan: cek juga jika kosong
    ) {
        Log.e("CloudinaryConfig", "Cloudinary config error: cloudName='$cloudName', uploadPreset='$uploadPreset'")
        onFailure(Exception("Konfigurasi Cloudinary (Cloud Name / Upload Preset) belum diatur dengan benar! Harap periksa konstanta di kode."))
        setLoading(false)
        return
    }
    if (selectedImageUri != null) {
        Log.d("CloudinaryUpload", "Uploading new image to Cloudinary...")
        val requestId = MediaManager.get().upload(selectedImageUri)
            .unsigned(uploadPreset)
            .option("cloud_name", cloudName)
            // .option("folder", "perisai_app/mentor_photos") // Opsional: tentukan folder di Cloudinary
            // .option("public_id", mentorDocumentId ?: UUID.randomUUID().toString()) // Opsional: public_id unik
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) { Log.d("CloudinaryUpload", "Upload started: $requestId") }
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) { /* Handle progress jika perlu */ }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val imageUrl = resultData["secure_url"] as? String
                    if (imageUrl != null) {
                        Log.d("CloudinaryUpload", "Upload success, URL: $imageUrl")
                        proceedWithSaveToFirestore(
                            mentorDocumentId, name, peminatan, deskripsi,
                            imageUrl, availableUntil, { onSuccess(imageUrl) }, onFailure, setLoading
                        )
                    } else {
                        Log.e("CloudinaryUpload", "Upload success but URL is null from Cloudinary.")
                        onFailure(Exception("URL gambar dari Cloudinary tidak ditemukan."))
                        setLoading(false)
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.e("CloudinaryUpload", "Upload error: ${error.description}")
                    onFailure(Exception("Cloudinary Upload Error: ${error.description}"))
                    setLoading(false)
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    Log.w("CloudinaryUpload", "Upload rescheduled: ${error.description}")
                    onFailure(Exception("Upload dijadwalkan ulang: ${error.description}"))
                    setLoading(false)
                }
            }).dispatch(context) // Gunakan context aplikasi

        if (requestId.isBlank()) {
            Log.e("CloudinaryUpload", "Failed to dispatch upload request. Request ID is blank.")
            onFailure(Exception("Gagal memulai proses upload gambar."))
            setLoading(false)
        }

    } else {
        Log.d("CloudinaryUpload", "No new image selected. Proceeding with existing URL: $existingPhotoUrl")
        proceedWithSaveToFirestore(
            mentorDocumentId, name, peminatan, deskripsi,
            existingPhotoUrl, // Gunakan URL yang sudah ada (bisa null jika tambah baru tanpa foto)
            availableUntil, { onSuccess(existingPhotoUrl ?: "") }, onFailure, setLoading
        )
    }
}

private fun proceedWithSaveToFirestore(
    mentorDocumentId: String?, // ID dokumen mentor (jika edit)
    name: String,
    peminatan: String,
    deskripsi: String,
    finalPhotoUrl: String?, // URL foto final dari Cloudinary atau yang sudah ada
    availableUntil: Timestamp?,
    onSuccessFirestore: () -> Unit, // Callback setelah sukses Firestore
    onFailureFirestore: (Exception) -> Unit,
    setLoading: (Boolean) -> Unit // Tetap kontrol loading dari sini
) {
    val db = FirebaseFirestore.getInstance()
    // Jika model Mentor Anda punya field 'userId' (untuk UID Auth), Anda perlu mengisinya
    // terutama saat tambah baru. Contoh:
    // val currentAuthUserId = FirebaseAuth.getInstance().currentUser?.uid

    val mentorDataMap = mutableMapOf<String, Any?>(
        "name" to name,
        "peminatan" to peminatan,
        "Deskripsi" to deskripsi, // Perhatikan D kapital
        "photoUrl" to (finalPhotoUrl ?: ""), // Simpan string kosong jika null
        "availableUntil" to availableUntil
        // Jika Anda menyimpan userId dari Auth di model Mentor:
        // if (mentorDocumentId == null && currentAuthUserId != null) { // Hanya untuk tambah baru
        //     mentorDataMap["userId"] = currentAuthUserId
        // }
    )

    val task = if (mentorDocumentId != null) { // Mode Edit
        Log.d("Firestore", "Updating mentor: $mentorDocumentId")
        db.collection("Mentor").document(mentorDocumentId)
            .set(mentorDataMap, SetOptions.merge()) // Gunakan merge untuk update, agar tidak menghapus field lain seperti userId
    } else { // Mode Tambah Baru
        Log.d("Firestore", "Adding new mentor")
        db.collection("Mentor").add(mentorDataMap) // .add() akan auto-generate ID dokumen
    }

    task.addOnSuccessListener {
        Log.d("Firestore", if (mentorDocumentId != null) "Mentor berhasil diperbarui" else "Mentor berhasil ditambahkan")
        onSuccessFirestore() // Panggil callback sukses
        // setLoading(false) // setLoading sudah dihandle di callback atasnya setelah onSuccessFirestore
    }
        .addOnFailureListener { e ->
            Log.w("Firestore", if (mentorDocumentId != null) "Error memperbarui mentor" else "Error menambahkan mentor", e)
            onFailureFirestore(e) // Panggil callback failure
            // setLoading(false) // setLoading sudah dihandle di callback atasnya setelah onFailureFirestore
        }
    // setLoading(false) dipanggil setelah onSuccess atau onFailure di saveMentorDataWithCloudinary
}