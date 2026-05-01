package com.example.resiplus

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.resiplus.database.DatabaseHelper

class AltaResidenteActivity : AppCompatActivity() {
    private var residenteId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alta_residente)

        val db = DatabaseHelper(this)
        val residencia = intent.getStringExtra("RESIDENCIA")
            ?: getSharedPreferences("resiplus_prefs", MODE_PRIVATE)
                .getString("usuario_residencia", "Residencia San Francisco")
            ?: "Residencia San Francisco"

        findViewById<TextView>(R.id.btnVolverAltaResidente).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvResidenciaAltaResidente).text = residencia
        residenteId = intent.getIntExtra("RESIDENTE_ID", -1)

        if (residenteId != -1) {
            val residente = db.getResidente(residenteId)
            findViewById<TextView>(R.id.tvTituloAltaResidente).text = "Editar residente"
            if (residente != null) {
                findViewById<EditText>(R.id.etNombreResidenteAlta).setText(residente.nombre)
                findViewById<EditText>(R.id.etEdadResidenteAlta).setText(residente.edad.toString())
                findViewById<EditText>(R.id.etHabitacionResidenteAlta).setText(residente.habitacion)
                findViewById<EditText>(R.id.etPlantaResidenteAlta).setText(residente.planta)
                findViewById<EditText>(R.id.etNacimientoResidenteAlta).setText(residente.fechaNacimiento)
                findViewById<EditText>(R.id.etIngresoResidenteAlta).setText(residente.fechaIngreso)
                findViewById<EditText>(R.id.etObservacionesResidenteAlta).setText(residente.observaciones)
                findViewById<EditText>(R.id.etNecesidadesResidenteAlta).setText(residente.necesidades)
            }
        }

        findViewById<Button>(R.id.btnGuardarResidente).setOnClickListener {
            val nombre = findViewById<EditText>(R.id.etNombreResidenteAlta).text.toString().trim()
            val edad = findViewById<EditText>(R.id.etEdadResidenteAlta).text.toString().trim().toIntOrNull()
            val habitacion = findViewById<EditText>(R.id.etHabitacionResidenteAlta).text.toString().trim()
            val planta = findViewById<EditText>(R.id.etPlantaResidenteAlta).text.toString().trim()
            val nacimiento = findViewById<EditText>(R.id.etNacimientoResidenteAlta).text.toString().trim()
            val ingreso = findViewById<EditText>(R.id.etIngresoResidenteAlta).text.toString().trim()
            val observaciones = findViewById<EditText>(R.id.etObservacionesResidenteAlta).text.toString().trim()
            val necesidades = findViewById<EditText>(R.id.etNecesidadesResidenteAlta).text.toString().trim()

            when {
                nombre.isEmpty() || edad == null || habitacion.isEmpty() || planta.isEmpty() ||
                    nacimiento.isEmpty() || ingreso.isEmpty() || observaciones.isEmpty() || necesidades.isEmpty() -> {
                    Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                }

                else -> {
                    if (residenteId != -1) {
                        db.actualizarResidente(
                            residenteId,
                            nombre,
                            edad,
                            habitacion,
                            planta,
                            nacimiento,
                            ingreso,
                            observaciones,
                            necesidades
                        )
                        Toast.makeText(this, "Residente actualizado", Toast.LENGTH_LONG).show()
                    } else {
                        val ok = db.insertarResidente(
                            nombre,
                            edad,
                            habitacion,
                            planta,
                            residencia,
                            nacimiento,
                            ingreso,
                            observaciones,
                            necesidades
                        )
                        if (ok == -1L) {
                            Toast.makeText(this, "No se pudo guardar el residente", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        Toast.makeText(this, "Residente añadido", Toast.LENGTH_LONG).show()
                    }
                    finish()
                }
            }
        }
    }
}
