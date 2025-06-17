package com.example.perisaiapps.Screen.admin

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Switch
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.perisaiapps.Model.Mentor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// --- Konstanta & Warna ---
private const val CLOUDINARY_CLOUD_NAME = "duaqqcjmr"
private const val CLOUDINARY_UPLOAD_PRESET = "perisai_mentor"
private val darkBackground = Color(0xFF120E26)
private val cardBackground = Color(0xFF1F1A38)
private val accentColor = Color(0xFF8A2BE2)
private val textColorPrimary = Color.White
private val textColorSecondary = Color.White.copy(alpha = 0.7f)
private val successColor = Color(0xFF00C853)
private val errorColor = Color(0xFFD50000)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditMentorScreen(
    navController: NavController,
    mentorId: String? = null
) {
    val context = LocalContext.current
    val isEditMode = mentorId != null
    val screenTitle = if (isEditMode) "Edit Detail Mentor" else "Tambah Mentor Baru"

    // --- State untuk Form ---
    var name by remember { mutableStateOf("") }
    var peminatan by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var isAvailable by remember { mutableStateOf(true) }
    var mentorEmail by remember { mutableStateOf("") }
    var mentorPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingPhotoUrl by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // --- State untuk Prestasi ---
    var achievementsList by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentAchievementText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> selectedImageUri = uri }
    )

    // Load data mentor hanya jika dalam mode edit
    LaunchedEffect(key1 = mentorId) {
        if (isEditMode && mentorId != null) {
            isLoading = true
            FirebaseFirestore.getInstance().collection("Mentor").document(mentorId).get()
                .addOnSuccessListener { doc ->
                    doc?.toObject(Mentor::class.java)?.let { mentor ->
                        name = mentor.name
                        peminatan = mentor.peminatan
                        deskripsi = mentor.deskripsi
                        existingPhotoUrl = mentor.photoUrl.ifBlank { null }
                        isAvailable = mentor.isAvailable
                        achievementsList = mentor.achievements ?: emptyList()
                    }
                    isLoading = false
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Gagal memuat data: ${exception.message}", Toast.LENGTH_SHORT).show()
                    isLoading = false
                    navController.navigateUp()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle, color = textColorPrimary) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // -- Field untuk Akun Mentor Baru (Hanya Muncul saat Tambah Baru) --
            AnimatedVisibility(visible = !isEditMode) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SectionTitle("Buat Akun Login untuk Mentor")
                    CustomOutlinedTextField(
                        value = mentorEmail,
                        onValueChange = { mentorEmail = it },
                        label = "Email Login Mentor",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                    )
                    CustomOutlinedTextField(
                        value = mentorPassword,
                        onValueChange = { mentorPassword = it },
                        label = "Password Login Mentor",
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
                    )
                    Divider(color = accentColor.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            // -- Field untuk Profil Mentor --
            SectionTitle("Detail Profil Mentor")
            CustomOutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nama Mentor",
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next)
            )
            CustomOutlinedTextField(
                value = peminatan,
                onValueChange = { peminatan = it },
                label = "Bidang Peminatan",
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next)
            )
            CustomOutlinedTextField(
                value = deskripsi,
                onValueChange = { deskripsi = it },
                label = "Deskripsi Mentor",
                singleLine = false,
                modifier = Modifier.defaultMinSize(minHeight = 150.dp)
            )

            // --- Bagian Prestasi (Achievements) ---
            Divider(color = accentColor.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
            SectionTitle("Prestasi Mentor")
            OutlinedTextField(
                value = currentAchievementText,
                onValueChange = { currentAchievementText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Ketik prestasi lalu klik tambah") },
                singleLine = true,
//                colors = OutlinedTextFieldDefaults.colors(containerColor = cardBackground),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (currentAchievementText.isNotBlank()) {
                                achievementsList = achievementsList + currentAchievementText.trim()
                                currentAchievementText = ""
                                keyboardController?.hide()
                            }
                        }
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Tambah Prestasi", tint = accentColor)
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (currentAchievementText.isNotBlank()) {
                            achievementsList = achievementsList + currentAchievementText.trim()
                            currentAchievementText = ""
                            keyboardController?.hide()
                        }
                    }
                )
            )
            if (achievementsList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    achievementsList.forEach { achievement ->
                        InputChip(
                            selected = false,
                            onClick = { /* Bisa untuk edit chip nanti */ },
                            label = { Text(achievement) },
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Hapus Prestasi",
                                    modifier = Modifier.size(InputChipDefaults.IconSize).clickable {
                                        achievementsList = achievementsList - achievement
                                    }
                                )
                            }
                        )
                    }
                }
            }

            // --- Bagian Pilih Gambar & Status ---
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ProfileImagePicker(
                    selectedImageUri = selectedImageUri,
                    existingPhotoUrl = existingPhotoUrl,
                    onClick = { imagePickerLauncher.launch("image/*") }
                )
                AvailabilitySwitch(
                    isAvailable = isAvailable,

                    onCheckedChange = { isAvailable = it },

                )
            }
            Log.d("isAvailable","${isAvailable}")

            // Tombol Simpan/Update
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    if (name.isBlank() || peminatan.isBlank() || (!isEditMode && (mentorEmail.isBlank() || mentorPassword.length < 6))) {
                        val message = if (!isEditMode && (mentorEmail.isBlank() || mentorPassword.length < 6)) {
                            "Email dan Password (min. 6 karakter) wajib diisi untuk mentor baru!"
                        } else {
                            "Nama dan Peminatan wajib diisi!"
                        }
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    isLoading = true
                    handleSaveMentor(
                        isEditMode = isEditMode,
                        mentorId = mentorId,
                        email = mentorEmail,
                        password = mentorPassword,
                        name = name.trim(),
                        peminatan = peminatan.trim(),
                        deskripsi = deskripsi.trim(),
                        isAvailable = isAvailable,
                        achievements = achievementsList,
                        selectedImageUri = selectedImageUri,
                        existingPhotoUrl = existingPhotoUrl,
                        context = context,
                        onSuccess = {
                            isLoading = false
                            val successMessage = if (isEditMode) "Mentor berhasil diperbarui" else "Mentor baru berhasil ditambahkan"
                            Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                            navController.navigate("admin_manage_mentors_route") {
                                popUpTo("admin_manage_mentors_route") {
                                    inclusive = true
                                }
                            }
//                            navController.popBackStack()
                        },
                        onFailure = { error ->
                            isLoading = false
                            Toast.makeText(context, "Gagal: ${error.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(if (isEditMode) "Simpan Perubahan" else "Tambah Mentor")
                }
            }
        }
    }
}


// ===================================================================================
// Composable Helper untuk kerapian UI
// ===================================================================================

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = textColorPrimary,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: () -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        trailingIcon = {
            if (isPassword) {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = onPasswordVisibilityChange) {
                    Icon(imageVector = image, contentDescription = "Toggle Password Visibility")
                }
            }
        },
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

@Composable
private fun ProfileImagePicker(
    selectedImageUri: Uri?,
    existingPhotoUrl: String?,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Foto Profil", style = MaterialTheme.typography.bodyMedium, color = textColorSecondary)
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(cardBackground)
                .border(1.dp, textColorSecondary, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            val imageToShow = selectedImageUri ?: existingPhotoUrl?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }
            if (imageToShow != null) {
                AsyncImage(
                    model = imageToShow,
                    contentDescription = "Preview Foto Mentor",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Face,
                    contentDescription = "Pilih Foto",
                    modifier = Modifier.size(48.dp),
                    tint = textColorSecondary
                )
            }
        }
    }
}

@Composable
private fun AvailabilitySwitch(
    isAvailable: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Status Ketersediaan", style = MaterialTheme.typography.bodyMedium, color = textColorSecondary)
        Text(
            text = if (isAvailable) "Bersedia" else "Sibuk",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isAvailable) successColor else errorColor
        )
        Switch(
            checked = isAvailable,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = successColor.copy(alpha = 0.7f),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = errorColor.copy(alpha = 0.7f)
            )
        )
    }
}


// ===================================================================================
// Logika Backend (Fungsi Helper untuk Firebase & Cloudinary)
// ===================================================================================

private fun handleSaveMentor(
    isEditMode: Boolean, mentorId: String?, email: String, password: String, name: String, peminatan: String,
    deskripsi: String, isAvailable: Boolean, achievements: List<String>, selectedImageUri: Uri?,
    existingPhotoUrl: String?, context: Context, onSuccess: () -> Unit, onFailure: (Exception) -> Unit
) {
    if (isEditMode) {
        if (mentorId == null) {
            onFailure(Exception("ID Mentor tidak valid untuk mode edit."))
            return
        }
        updateExistingMentor(mentorId, name, peminatan, deskripsi, isAvailable, achievements, selectedImageUri, existingPhotoUrl, context, onSuccess, onFailure)
        Log.d("apalah", "DAta :,${name},${peminatan},${deskripsi},${isAvailable},${achievements}")
    } else {
        addNewMentor(email, password, name, peminatan, deskripsi, isAvailable, achievements, selectedImageUri, context, onSuccess, onFailure)
        Log.d("apalah", "DAta :,${name},${peminatan},${deskripsi},${isAvailable},${achievements}")
    }
}

private fun addNewMentor(
    email: String, password: String, name: String, peminatan: String, deskripsi: String, isAvailable: Boolean,
    achievements: List<String>, selectedImageUri: Uri?, context: Context, onSuccess: () -> Unit, onFailure: (Exception) -> Unit
) {
    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
        .addOnSuccessListener { authResult ->
            val newUserId = authResult.user?.uid
            if (newUserId == null) {
                onFailure(Exception("Gagal mendapatkan UID setelah membuat akun."))
                return@addOnSuccessListener
            }
            Log.d("AddMentor", "1. Akun Auth berhasil dibuat. UID: $newUserId")
            createUserProfile(newUserId, email, name) { userProfileError ->
                if (userProfileError != null) {
                    onFailure(userProfileError)
                    return@createUserProfile
                }
                Log.d("AddMentor", "2. Dokumen di 'users' berhasil dibuat.")
                Log.d("apalah", "DAta :,${name},${peminatan},${deskripsi},${isAvailable},${achievements}")
                uploadImageAndCreateMentorProfile(newUserId, name, peminatan, deskripsi, isAvailable, achievements, selectedImageUri, context, onSuccess, onFailure)
            }
        }
        .addOnFailureListener { authError ->
            Log.w("AddMentor", "Gagal membuat akun Auth.", authError)
            onFailure(authError)
        }
}

private fun updateExistingMentor(
    mentorId: String, name: String, peminatan: String, deskripsi: String, isAvailable: Boolean,
    achievements: List<String>, selectedImageUri: Uri?, existingPhotoUrl: String?, context: Context,
    onSuccess: () -> Unit, onFailure: (Exception) -> Unit
) {
    getFinalPhotoUrl(selectedImageUri, existingPhotoUrl, context) { finalPhotoUrl, error ->
        if (error != null) {
            onFailure(error)
            return@getFinalPhotoUrl
        }
        val updatedMentorData =
            mapOf(
            "name" to name,
            "peminatan" to peminatan,
            "deskripsi" to deskripsi,
            "isAvailable" to isAvailable,
            "photoUrl" to (finalPhotoUrl ?: ""),
            "achievements" to achievements
        )
//        FirebaseFirestore.getInstance().collection("Mentor").document(mentorId)
//            .update(mentorDataMap)
//            .addOnSuccessListener {
//                Log.d("UpdateMentor", "Profil Mentor berhasil diupdate.")
//                onSuccess()
//            }
//            .addOnFailureListener {
//                Log.w("UpdateMentor", "Gagal update profil Mentor.", it)
//                onFailure(it)
//            }
        FirebaseFirestore.getInstance().collection("Mentor").document(mentorId)
            // Ganti .update(map) dengan .set(map, merge)
            // Meskipun terlihat sama, ini menunjukkan pola yang bisa dipakai untuk objek
            .set(updatedMentorData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d("UpdateMentor", "Profil Mentor berhasil diupdate.")
                onSuccess()
            }
            .addOnFailureListener {
                Log.w("UpdateMentor", "Gagal update profil Mentor.", it)
                onFailure(it)
            }
    }
}

private fun createUserProfile(uid: String, email: String, name: String, onComplete: (Exception?) -> Unit) {
    val userDoc = mapOf("uid" to uid, "email" to email, "displayName" to name, "role" to "mentor")
    FirebaseFirestore.getInstance().collection("users").document(uid)
        .set(userDoc)
        .addOnCompleteListener { task -> onComplete(task.exception) }
}

private fun uploadImageAndCreateMentorProfile(
    userId: String, name: String, peminatan: String, deskripsi: String, isAvailable: Boolean,
    achievements: List<String>, selectedImageUri: Uri?, context: Context, onSuccess: () -> Unit, onFailure: (Exception) -> Unit
) {
    getFinalPhotoUrl(selectedImageUri, null, context) { finalPhotoUrl, error ->
        if (error != null) {
            onFailure(error)
            return@getFinalPhotoUrl
        }
        val mentorProfile = Mentor(
            userId = userId,
            name = name,
            peminatan = peminatan,
            deskripsi = deskripsi,
            photoUrl = finalPhotoUrl ?: "",
            isAvailable = isAvailable,
            achievements = achievements
        )
        FirebaseFirestore.getInstance().collection("Mentor").add(mentorProfile)
            .addOnSuccessListener {
                Log.d("AddMentor", "3. Profil Mentor berhasil dibuat di koleksi 'Mentor'.")
                Log.d("AddMentor", "Mentor ID: ${userId},${name},${peminatan},${deskripsi},${finalPhotoUrl},${isAvailable},${achievements}")

                onSuccess()
            }
            .addOnFailureListener {
                Log.w("AddMentor", "Gagal membuat profil di koleksi 'Mentor'.", it)
                Log.d("AddMentor", "Mentor ID: ${userId},${name},${peminatan},${deskripsi},${finalPhotoUrl},${isAvailable},${achievements}")

                onFailure(it)
            }
    }
}

private fun getFinalPhotoUrl(
    newImageUri: Uri?, existingUrl: String?, context: Context, onResult: (String?, Exception?) -> Unit
) {
    if (newImageUri != null) {
        MediaManager.get().upload(newImageUri)
            .unsigned(CLOUDINARY_UPLOAD_PRESET)
            .option("cloud_name", CLOUDINARY_CLOUD_NAME)
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String, resultData: Map<*, *>) { onResult(resultData["secure_url"] as? String, null) }
                override fun onError(requestId: String, error: ErrorInfo) { onResult(null, Exception("Cloudinary Error: ${error.description}")) }
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch(context)
    } else {
        onResult(existingUrl, null)
    }
}