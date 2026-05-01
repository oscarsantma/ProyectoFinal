package com.example.resiplus

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.resiplus.database.DatabaseHelper

class PerfilResidenteActivity : AppCompatActivity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.activity_perfil_residente)

        findViewById<TextView>(R.id.btnVolver).setOnClickListener { finish() }
        val idUsuario = getSharedPreferences("resiplus_prefs", MODE_PRIVATE).getInt("usuario_id", -1)
        val residente = DatabaseHelper(this).getResidenteVinculado(idUsuario) ?: return

        findViewById<TextView>(R.id.tvNombrePerfilResidente).text = residente.nombre
        findViewById<TextView>(R.id.tvResumenPerfilResidente).text =
            "${residente.edad} anos · Hab. ${residente.habitacion} · ${residente.planta}"
        findViewById<TextView>(R.id.tvFechaNacimientoPerfil).text = residente.fechaNacimiento
        findViewById<TextView>(R.id.tvFechaIngresoPerfil).text = residente.fechaIngreso
        findViewById<TextView>(R.id.tvHabitacionPerfil).text = "${residente.habitacion} · ${residente.planta}"
        findViewById<TextView>(R.id.tvObservacionesPerfil).text = residente.observaciones
    }
}
