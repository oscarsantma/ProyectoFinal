package com.example.resiplus.model
data class Usuario(
    val id: Int = 0,
    val nombre: String,
    val email: String,
    val password: String,
    val rol: String,
    val residencia: String,
    val estado: String,
    val idResidente: Int? = null
)
