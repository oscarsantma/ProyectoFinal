package com.example.resiplus

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.resiplus.database.DatabaseHelper

class DashboardAdminActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_admin)
        db = DatabaseHelper(this)

        val prefs = getSharedPreferences("resiplus_prefs", MODE_PRIVATE)
        val nombre = prefs.getString("usuario_nombre", "Admin") ?: "Admin"

        findViewById<TextView>(R.id.tvSaludoAdmin).text = "Panel general de ${nombre.split(" ").first()}"
        findViewById<TextView>(R.id.btnVolverMenuPrincipalAdmin).setOnClickListener { cerrarSesion() }
        findViewById<TextView>(R.id.tvCerrarSesionAdmin).setOnClickListener { cerrarSesion() }

        findViewById<LinearLayout>(R.id.cardGestionarEquipoAdmin).setOnClickListener {
            startActivity(Intent(this, GestionarSolicitudesActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.cardGestionarResidenciasAdmin).setOnClickListener {
            startActivity(Intent(this, GestionResidenciasActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.cardCrearUsuarioAdmin).setOnClickListener {
            startActivity(Intent(this, AltaUsuarioActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        cargarResumen()
    }

    private fun cargarResumen() {
        findViewById<TextView>(R.id.tvTotalResidenciasAdmin).text = db.contarResidencias().toString()
        findViewById<TextView>(R.id.tvTotalResidentesAdmin).text = db.contarResidentesTotales().toString()
        findViewById<TextView>(R.id.tvPersonalPendienteAdmin).text = db.contarSolicitudesPersonalPendientes().toString()
        findViewById<TextView>(R.id.tvDetalleAdmin).text =
            "Aprueba al personal nuevo y mantén al día las residencias y sus residentes."
    }

    private fun cerrarSesion() {
        getSharedPreferences("resiplus_prefs", MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, SeleccionRolActivity::class.java))
        finishAffinity()
    }
}
