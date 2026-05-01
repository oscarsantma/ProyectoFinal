package com.example.resiplus

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.resiplus.database.DatabaseHelper

class DashboardPersonalActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_personal)
        db = DatabaseHelper(this)

        findViewById<TextView>(R.id.btnVolverMenuPrincipalPersonal).setOnClickListener { cerrarSesion() }
        findViewById<TextView>(R.id.tvCerrarSesionPersonal).setOnClickListener { cerrarSesion() }
        findViewById<LinearLayout>(R.id.cardGestionarSolicitudes).setOnClickListener {
            startActivity(Intent(this, GestionarSolicitudesActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.cardCalendarioCitas).setOnClickListener {
            startActivity(Intent(this, CalendarioCitasActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.cardResidentesPersonal).setOnClickListener {
            startActivity(Intent(this, ResidentesResidenciaActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        cargarResumen()
    }

    private fun cargarResumen() {
        val prefs = getSharedPreferences("resiplus_prefs", MODE_PRIVATE)
        val nombre = prefs.getString("usuario_nombre", "Personal") ?: "Personal"
        val residencia = prefs.getString("usuario_residencia", "Residencia San Francisco") ?: "Residencia San Francisco"

        findViewById<TextView>(R.id.tvSaludoPersonal).text = "Turno de ${nombre.split(" ").first()}"
        findViewById<TextView>(R.id.tvResidenciaPersonal).text = residencia
        findViewById<TextView>(R.id.tvNumPendientes).text = db.contarPendientes(residencia).toString()
        findViewById<TextView>(R.id.tvNumHoy).text = db.contarVisitasHoy(residencia).toString()
        findViewById<TextView>(R.id.tvNumRegistros).text = db.contarSolicitudesFamiliaresPendientes(residencia).toString()
        findViewById<TextView>(R.id.tvResumenOperativo).text =
            "Gestiona aprobaciones de familiares, revisa citas pendientes y consulta el listado de residentes."
    }

    private fun cerrarSesion() {
        getSharedPreferences("resiplus_prefs", MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, SeleccionRolActivity::class.java))
        finishAffinity()
    }
}
