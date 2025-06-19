package com.example.perisaiapps.Model

import com.google.firebase.firestore.DocumentId

data class Lomba(
    @DocumentId
    val id: String = "", // Untuk menyimpan ID dokumen dari Firestore
    val linkInfo: String = "", // Link ke halaman detail lomba
    val deskripsi: String = "",
    val imageUrl : String = "", // URL gambar poster/banner lomba
    val namaLomba: String = "",
    val pelaksanaan: String = "", // Contoh: "Pelaksanaan: 20-22 Mei 2025" atau bisa juga Timestamp
    val pendaftaran: String = "", // Contoh: "Deadline: 15 Mei 2025" atau bisa juga Timestamp
    val penyelenggara: String = ""
)