package com.example.resiplus.model
data class Visita(
    val id: Int,
    val idFamiliar: Int,
    val nombreFamiliar: String,
    val fecha: String,
    val hora: String,
    val estado: String,
    val nota: String,
    val nombreResidente: String = ""
)
