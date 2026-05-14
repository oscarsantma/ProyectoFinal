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

class LoginActivity : BaseActivity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.activity_login)

        val db = DatabaseHelper(this)
        val rol = intent.getStringExtra("ROL") ?: "FAMILIAR"

        findViewById<TextView>(R.id.tvRolBadge).text = rol
        // el admin no puede registrarse, solo iniciar sesion
        findViewById<TextView>(R.id.tvCrearCuenta).visibility = if (rol == "ADMIN") View.GONE else View.VISIBLE
        findViewById<TextView>(R.id.btnVolverLogin).setOnClickListener {
            startActivity(Intent(this, SeleccionRolActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
            val pass = findViewById<EditText>(R.id.etPassword).text.toString().trim()
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Thread {
                val u = db.login(email, pass)
                runOnUiThread {
                    if (u == null) {
                        Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }
                    if (u.rol != rol) {
                        Toast.makeText(this, "Este usuario no tiene rol de $rol", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }
                    if (u.estado != "APROBADO") {
                        val msg = if (u.estado == "RECHAZADO") "Tu registro fue rechazado. Habla con la residencia."
                                  else "Tu cuenta aun no ha sido aprobada por la residencia."
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                        return@runOnUiThread
                    }
                    val prefs = getSharedPreferences("resiplus_prefs", MODE_PRIVATE).edit()
                    prefs.putInt("usuario_id", u.id)
                    prefs.putString("usuario_nombre", u.nombre)
                    prefs.putString("usuario_rol", u.rol)
                    prefs.putString("usuario_residencia", u.residencia)
                    prefs.putInt("usuario_residente_id", u.idResidente ?: -1)
                    prefs.apply()
                    val destino = when (rol) {
                        "FAMILIAR" -> DashboardFamiliarActivity::class.java
                        "ADMIN" -> DashboardAdminActivity::class.java
                        else -> DashboardPersonalActivity::class.java
                    }
                    startActivity(Intent(this, destino))
                    finish()
                }
            }.start()
        }

        findViewById<TextView>(R.id.tvCrearCuenta).setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java).putExtra("ROL", rol))
        }

        findViewById<TextView>(R.id.tvOlvidePassword).setOnClickListener {
            Toast.makeText(this, "Proximamente", Toast.LENGTH_SHORT).show()
        }
    }
}
