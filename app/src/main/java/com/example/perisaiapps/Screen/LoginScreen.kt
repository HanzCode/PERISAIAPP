package com.example.perisaiapps.Screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.perisaiapps.viewmodel.UserRole
import com.example.perisaiapps.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(
    navController: NavController,
    userViewModel: UserViewModel = viewModel() // Dapatkan instance ViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) } // Loading untuk proses login
    var loginErrorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    // Amati peran pengguna dari ViewModel
    val userRole by userViewModel.userRole
    val isLoadingRole by userViewModel.isLoadingRole
    val roleFetchError by userViewModel.errorMessage

    // Handle navigasi saat state Success
    LaunchedEffect(userRole) {
        if (!isLoadingRole && roleFetchError == null) { // Hanya navigasi jika tidak loading dan tidak ada error fetch peran
            when (userRole) {
                UserRole.ADMIN -> {
                    navController.navigate("admin_dashboard_route") { // Rute baru untuk admin
                        popUpTo("login") { inclusive = true } // Hapus login dari backstack
                    }
                }

                UserRole.MENTOR -> {
                    navController.navigate("mentor_dashboard_route") { // Rute baru untuk mentor
                        popUpTo("login") { inclusive = true }
                    }
                }

                UserRole.USER -> {
                    navController.navigate("home") { // Rute "home" untuk user biasa (ke MainScreen)
                        popUpTo("login") { inclusive = true }
                    }
                }

                UserRole.UNKNOWN -> {
                    // Jika peran UNKNOWN setelah fetch selesai (bukan saat inisialisasi),
                    // mungkin ada masalah. Anda bisa tetap di halaman login atau tampilkan pesan.
                    // Untuk saat ini, kita biarkan (tidak navigasi otomatis jika UNKNOWN pasca fetch)
                    if (auth.currentUser != null && !isLoadingRole) { // Hanya tampilkan error jika user sudah login tapi peran unknown
                        loginErrorMessage = roleFetchError ?: "Peran pengguna tidak dikenali."
                    }
                }
            }
        }
    }

    LaunchedEffect(roleFetchError) {
        roleFetchError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            // visualTransformation = PasswordVisualTransformation(), // Untuk menyembunyikan password
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))


        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    loginErrorMessage = "Email dan Password tidak boleh kosong."
                    return@Button
                }
                isLoading = true
                loginErrorMessage = null
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("LoginScreen", "Login berhasil. Fetching user role...")
                            // JANGAN LANGSUNG NAVIGASI DI SINI
                            // Panggil fetchUserRole, LaunchedEffect di atas akan menangani navigasi
                            userViewModel.fetchUserRole()
                            // isLoading akan di-set false oleh LaunchedEffect setelah navigasi atau jika peran unknown
                        } else {
                            Log.w("LoginScreen", "Login gagal: ", task.exception)
                            loginErrorMessage = task.exception?.message ?: "Login gagal."
                            isLoading = false
                        }
                        // isLoading untuk tombol login di set false setelah fetchUserRole mulai atau jika login gagal
                        // isLoadingRole dari ViewModel akan menangani tampilan loading saat fetch peran
                    }
            },
            enabled = !isLoading && !isLoadingRole // Tombol disable saat login atau fetch role
        ) {
            if (isLoading || isLoadingRole) { // Tampilkan loading jika salah satu proses berjalan
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Text("Login")
            }
        }
        loginErrorMessage?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}



