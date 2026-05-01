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

class AltaUsuarioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alta_usuario)

        val db = DatabaseHelper(this)
        val residenciaSpinner = findViewById<Spinner>(R.id.spinnerResidenciaAdmin)
        val rolSpinner = findViewById<Spinner>(R.id.spinnerRolAdmin)
        val residenteSpinner = findViewById<Spinner>(R.id.spinnerResidenteAdmin)
        val layoutResidente = findViewById<LinearLayout>(R.id.layoutResidenteAdmin)
        val residencias = db.getResidencias()
        val roles = listOf("PERSONAL", "FAMILIAR", "ADMIN")

        residenciaSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, residencias)
        rolSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        fun recargarResidentes() {
            val residencia = residenciaSpinner.selectedItem?.toString().orEmpty()
            val residentes = db.getResidentesPorResidencia(residencia)
                .map { RegistroActivity.OpcionResidente(it.id, it.nombre) }
            residenteSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, residentes)
        }

        rolSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val rol = roles[position]
                layoutResidente.visibility = if (rol == "FAMILIAR") View.VISIBLE else View.GONE
                if (rol == "FAMILIAR") recargarResidentes()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        }

        residenciaSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (rolSpinner.selectedItem?.toString() == "FAMILIAR") recargarResidentes()
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        }

        findViewById<TextView>(R.id.btnVolverAltaUsuario).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnCrearUsuarioAdmin).setOnClickListener {
            val nombre = findViewById<EditText>(R.id.etNombreUsuarioAdmin).text.toString().trim()
            val email = findViewById<EditText>(R.id.etEmailUsuarioAdmin).text.toString().trim()
            val pass = findViewById<EditText>(R.id.etPasswordUsuarioAdmin).text.toString().trim()
            val residencia = residenciaSpinner.selectedItem?.toString().orEmpty()
            val rol = rolSpinner.selectedItem?.toString().orEmpty()
            val idResidente = if (rol == "FAMILIAR") {
                (residenteSpinner.selectedItem as? RegistroActivity.OpcionResidente)?.id
            } else {
                null
            }

            when {
                nombre.isEmpty() || email.isEmpty() || pass.isEmpty() || residencia.isEmpty() || rol.isEmpty() ->
                    Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()

                rol == "FAMILIAR" && idResidente == null ->
                    Toast.makeText(this, "Selecciona el residente asociado", Toast.LENGTH_SHORT).show()

                else -> {
                    val ok = db.crearUsuarioDesdeAdmin(nombre, email, pass, rol, residencia, idResidente)
                    if (ok == -1L) {
                        Toast.makeText(this, "No se pudo crear el usuario", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Usuario creado y aprobado", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            }
        }
    }
}
