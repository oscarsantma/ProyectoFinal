package com.example.resiplus.model

data class Residente(
    val id: Int,
    val nombre: String,
    val edad: Int,
    val habitacion: String,
    val planta: String,
    val residencia: String,
    val fechaNacimiento: String,
    val fechaIngreso: String,
    val observaciones: String,
    val necesidades: String,
    val activo: Boolean = true
)
