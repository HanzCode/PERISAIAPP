package com.example.perisaiapps.Screen.AdminScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.perisaiapps.ViewModel.AdminDashboardViewModel
import com.google.firebase.auth.FirebaseAuth

// --- Palet Warna (konsisten dengan tema gelap) ---
private val darkBackground = Color(0xFF120E26)
private val cardBackground = Color(0xFF1F1A38)
private val textColorPrimary = Color.White
private val textColorSecondary = Color.White.copy(alpha = 0.7f)
private val accentColor = Color(0xFF8A2BE2) // Ungu sebagai aksen

// Data class untuk item menu agar lebih rapi
private data class AdminActionItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminDashboardViewModel = viewModel()) {

    val mentorCount by viewModel.mentorCount.collectAsState()
    val lombaCount by viewModel.lombaCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Daftar menu utama untuk admin
    val actionItems = listOf(
        AdminActionItem("Kelola Mentor", Icons.Default.AccountCircle, "admin_manage_mentors_route"),
        AdminActionItem("Kelola Lomba", Icons.Default.Create, "admin_manage_lomba_route"),
        AdminActionItem("Kelola Pengguna", Icons.Default.Person, "admin_manage_users_route"), // Contoh
    )
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // Jika lifecycle event adalah ON_RESUME (layar kembali aktif)
            if (event == Lifecycle.Event.ON_RESUME) {
                // Panggil fungsi untuk memuat ulang data statistik
                viewModel.fetchDashboardStats()
            }
        }
        // Tambahkan observer ke lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // Hapus observer saat Composable meninggalkan layar
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", color = textColorPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBackground)
            )
        },
        containerColor = darkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bagian Header
            Text(
                "Selamat Datang, Admin!",
                style = MaterialTheme.typography.headlineMedium,
                color = textColorPrimary
            )
            Text(
                "Anda dapat mengelola konten aplikasi dari sini.",
                style = MaterialTheme.typography.bodyMedium,
                color = textColorSecondary
            )
            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bagian Ringkasan Statistik
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardSummaryCard(
                    label = "Total Mentor",
                    count = if (isLoading && mentorCount == 0L) "..." else mentorCount.toString(), // Tampilkan "..." hanya saat loading awal
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )
                DashboardSummaryCard(
                    label = "Total Lomba",
                    count = if (isLoading && lombaCount == 0L) "..." else lombaCount.toString(), // Tampilkan "..." hanya saat loading awal
                    icon = Icons.Default.Info,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bagian Menu Aksi Utama dalam bentuk Grid
            Text(
                text = "Menu Utama",
                style = MaterialTheme.typography.titleLarge,
                color = textColorPrimary,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(actionItems) { item ->
                    DashboardActionCard(
                        label = item.label,
                        icon = item.icon,
                        onClick = { navController.navigate(item.route) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Mendorong tombol logout ke bawah

            // Tombol Logout
            OutlinedButton(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Logout", tint = textColorSecondary)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Logout", color = textColorSecondary)
            }
        }
    }
}

// Composable terpisah untuk kartu ringkasan
@Composable
private fun DashboardSummaryCard(label: String, count: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(36.dp)
            )
            Column {
                Text(
                    text = count,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColorPrimary
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColorSecondary
                )
            }
        }
    }
}

// Composable terpisah untuk kartu menu aksi
@Composable
private fun DashboardActionCard(label: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f) // Membuat kartu menjadi persegi
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = textColorPrimary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = label,
                color = textColorSecondary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}