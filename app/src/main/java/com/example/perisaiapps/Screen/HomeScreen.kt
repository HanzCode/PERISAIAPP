package com.example.perisaiapps.Screen

import android.util.Log // Import Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState // Import untuk scrolling
import androidx.compose.foundation.verticalScroll // Import untuk scrolling
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // Import untuk side-effect
import androidx.compose.runtime.getValue // Import untuk state delegation
import androidx.compose.runtime.mutableStateOf // Import untuk state
import androidx.compose.runtime.remember // Import untuk state
import androidx.compose.runtime.setValue // Import untuk state delegation
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.perisaiapps.Component.GreetingSection
import com.example.perisaiapps.Component.HorizontalCardSection // Pastikan path ini benar
import com.example.perisaiapps.Component.PeminatanSection
import com.example.perisaiapps.Model.Lomba // Import model Lomba
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query // Import untuk ordering dan limiting

@Composable
fun HomeScreen(
    userName: String = "Farhan",
    navController: NavController
){
    // State untuk menyimpan daftar lomba
    var lombaListForHome by remember { mutableStateOf<List<Lomba>>(emptyList()) }
    var isLombaLoading by remember { mutableStateOf(true) }
    var lombaErrorMessage by remember { mutableStateOf<String?>(null) }

    // LaunchedEffect untuk mengambil data lomba saat HomeScreen pertama kali dibuat
    LaunchedEffect(Unit) {
        isLombaLoading = true
        lombaErrorMessage = null
        FirebaseFirestore.getInstance().collection("Lomba")
            // Anda bisa menambahkan orderBy atau filter lain jika perlu
            // Contoh: .orderBy("tanggalPosting", Query.Direction.DESCENDING)
            .limit(5) // Ambil 5 lomba teratas (atau sesuaikan)
            .get()
            .addOnSuccessListener { result ->
                try {
                    val data = result.documents.mapNotNull { document ->
                        document.toObject(Lomba::class.java)?.copy(id = document.id)
                    }
                    lombaListForHome = data
                    Log.d("HomeScreenLomba", "Data Lomba untuk Home berhasil diambil: ${data.size} items")
                } catch (e: Exception) {
                    Log.e("HomeScreenLomba", "Error mapping Lomba documents", e)
                    lombaErrorMessage = "Gagal memproses data lomba."
                } finally {
                    isLombaLoading = false
                }
            }
            .addOnFailureListener { exception ->
                Log.e("HomeScreenLomba", "Error getting Lomba documents: ", exception)
                lombaErrorMessage = "Gagal mengambil data lomba."
                isLombaLoading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B1533))
            // Tambahkan padding horizontal di Column utama, bukan di setiap section
            .padding(horizontal = 16.dp)
            // Buat seluruh halaman bisa di-scroll jika kontennya panjang
            .verticalScroll(rememberScrollState())
    ) {
        // Padding atas bisa pakai Spacer atau padding di Column
        Spacer(modifier = Modifier.height(18.dp))
        GreetingSection(userName)
        Spacer(modifier = Modifier.height(30.dp))

        // Panggil HorizontalCardSection dengan data lomba dan navController
        HorizontalCardSection(
            title = "Informasi Lomba", // Tambahkan judul untuk section ini
            lombaList = lombaListForHome,
            navController = navController,
            isLoading = isLombaLoading,
            errorMessage = lombaErrorMessage,
            modifier = Modifier.fillMaxWidth() // Pastikan section ini mengambil lebar penuh
        )

        Spacer(modifier = Modifier.height(16.dp))
        PeminatanSection()
        Spacer(modifier = Modifier.height(16.dp)) // Padding bawah
    }
}