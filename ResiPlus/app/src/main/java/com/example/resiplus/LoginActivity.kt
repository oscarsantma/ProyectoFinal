package com.example.resiplus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.resiplus.database.DatabaseHelper

class LoginActivity : AppCompatActivity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.activity_login)

        val db = DatabaseHelper(this)
        val rol = intent.getStringExtra("ROL") ?: "FAMILIAR"
        val header = findViewById<LinearLayout>(R.id.layoutHeader)
        val badge = findViewById<TextView>(R.id.tvRolBadge)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegistro = findViewById<TextView>(R.id.tvCrearCuenta)
        val btnVolver = findViewById<TextView>(R.id.btnVolverLogin)
        badge.text = rol
        header.setBackgroundColor(getColor(R.color.verde_andalucia_primary))
        btnLogin.setBackgroundResource(R.drawable.bg_boton_verde)
        tvRegistro.visibility = if (rol == "ADMIN") View.GONE else View.VISIBLE
        btnVolver.setOnClickListener {
            startActivity(Intent(this, SeleccionRolActivity::class.java))
            finish()
        }

        btnLogin.setOnClickListener {
            val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
            val pass = findViewById<EditText>(R.id.etPassword).text.toString().trim()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val u = db.login(email, pass)
            if (u == null) {
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (u.rol != rol) {
                Toast.makeText(this, "Este usuario no tiene rol de $rol", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (u.estado != "APROBADO") {
                val msg = if (u.estado == "RECHAZADO") {
                    "Tu registro fue rechazado. Habla con la residencia."
                } else {
                    "Tu cuenta aun no ha sido aprobada por la residencia."
                }
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            getSharedPreferences("resiplus_prefs", MODE_PRIVATE).edit().apply {
                putInt("usuario_id", u.id)
                putString("usuario_nombre", u.nombre)
                putString("usuario_rol", u.rol)
                putString("usuario_residencia", u.residencia)
                putInt("usuario_residente_id", u.idResidente ?: -1)
                apply()
            }
            val destino = when (rol) {
                "FAMILIAR" -> DashboardFamiliarActivity::class.java
                "ADMIN" -> DashboardAdminActivity::class.java
                else -> DashboardPersonalActivity::class.java
            }
            startActivity(Intent(this, destino))
            finish()
        }

        tvRegistro.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java).putExtra("ROL", rol))
        }

        findViewById<TextView>(R.id.tvOlvidePassword).setOnClickListener {
            Toast.makeText(this, "Proximamente", Toast.LENGTH_SHORT).show()
        }
    }
}
