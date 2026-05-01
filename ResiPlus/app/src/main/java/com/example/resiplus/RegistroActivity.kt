package com.example.resiplus

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.resiplus.database.DatabaseHelper

class RegistroActivity : AppCompatActivity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.activity_registro)

        val db = DatabaseHelper(this)
        val rol = intent.getStringExtra("ROL") ?: "FAMILIAR"
        val residenciaSpinner = findViewById<Spinner>(R.id.spinnerResidencia)
        val residenteSpinner = findViewById<Spinner>(R.id.spinnerResidente)
        val residenteGroup = findViewById<LinearLayout>(R.id.layoutResidente)
        val badge = findViewById<TextView>(R.id.tvRolRegistro)
        val btnRegistro = findViewById<Button>(R.id.btnCrearCuenta)
        val tvInfo = findViewById<TextView>(R.id.tvInfoRegistro)
        val residencias = db.getResidencias()

        badge.text = rol
        btnRegistro.text = if (rol == "FAMILIAR") "Solicitar alta familiar" else "Solicitar alta personal"
        tvInfo.text = if (rol == "FAMILIAR") {
            "Tu solicitud quedara pendiente hasta que el personal de la residencia apruebe el vinculo con el residente."
        } else {
            "Tu solicitud quedara pendiente hasta que la residencia valide que perteneces a ese centro."
        }

        residenciaSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, residencias)
        residenciaSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (rol == "FAMILIAR") {
                    cargarResidentes(db, residencias[position], residenteSpinner)
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        }

        residenteGroup.visibility = if (rol == "FAMILIAR") View.VISIBLE else View.GONE
        if (rol == "FAMILIAR" && residencias.isNotEmpty()) {
            cargarResidentes(db, residencias.first(), residenteSpinner)
        }

        findViewById<TextView>(R.id.btnVolverRegistro).setOnClickListener { finish() }
        btnRegistro.setOnClickListener {
            val nombre = findViewById<EditText>(R.id.etNombreRegistro).text.toString().trim()
            val email = findViewById<EditText>(R.id.etEmailRegistro).text.toString().trim()
            val pass = findViewById<EditText>(R.id.etPasswordRegistro).text.toString().trim()
            val residencia = residenciaSpinner.selectedItem?.toString()?.trim().orEmpty()
            val idResidente = if (rol == "FAMILIAR") {
                (residenteSpinner.selectedItem as? OpcionResidente)?.id
            } else {
                null
            }

            when {
                nombre.isEmpty() || email.isEmpty() || pass.isEmpty() || residencia.isEmpty() ->
                    Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                rol == "FAMILIAR" && idResidente == null ->
                    Toast.makeText(this, "Selecciona a tu residente", Toast.LENGTH_SHORT).show()
                else -> {
                    val ok = db.registrarUsuario(nombre, email, pass, rol, residencia, idResidente)
                    if (ok == -1L) {
                        Toast.makeText(this, "Ese correo ya existe o no se pudo registrar", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Registro enviado. Espera aprobacion del personal.", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun cargarResidentes(db: DatabaseHelper, residencia: String, spinner: Spinner) {
        val residentes = db.getResidentesPorResidencia(residencia).map { OpcionResidente(it.id, it.nombre) }
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, residentes)
    }

    data class OpcionResidente(val id: Int, val nombre: String) {
        override fun toString() = nombre
    }
}
