package com.example.note77

data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val username: String = ""  // ✅ Added to associate note with user
)

