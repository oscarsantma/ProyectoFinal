package com.example.resiplus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.resiplus.database.DatabaseHelper

class DashboardFamiliarActivity : BaseActivity() {
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_familiar)
        db = DatabaseHelper(this)

        findViewById<TextView>(R.id.btnVolverMenuPrincipalFamiliar).setOnClickListener { cerrarSesion() }
        findViewById<TextView>(R.id.tvCerrarSesion).setOnClickListener { cerrarSesion() }
        findViewById<LinearLayout>(R.id.cardSolicitarVisita).setOnClickListener {
            startActivity(Intent(this, SolicitarVisitaActivity::class.java))
        }
        findViewById<LinearLayout>(R.id.cardMisVisitas).setOnClickListener {
            startActivity(Intent(this, MisVisitasActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDatos()
    }

    private fun cargarDatos() {
        val prefs = getSharedPreferences("resiplus_prefs", MODE_PRIVATE)
        val nombre = prefs.getString("usuario_nombre", "Usuario") ?: "Usuario"
        val uid = prefs.getInt("usuario_id", -1)
        val residencia = prefs.getString("usuario_residencia", "") ?: ""

        val residente = db.getResidenteVinculado(uid)
        val visitas = db.getVisitasFamiliar(uid)
        val pendientes = visitas.count { it.estado == "PENDIENTE" }
        val confirmadas = visitas.count { it.estado == "CONFIRMADA" }
        // proxima visita confirmada, si la hay
        val proxima = visitas.firstOrNull { it.estado == "CONFIRMADA" }

        val nombreCorto = nombre.split(" ").first()
        findViewById<TextView>(R.id.tvSaludo).text = "Hola, $nombreCorto"
        findViewById<TextView>(R.id.tvNombreResidenteDashboard).text = residente?.nombre ?: "Vinculo pendiente"

        if (residente != null) {
            findViewById<TextView>(R.id.tvResumenResidenteDashboard).text =
                "${residente.residencia} · Hab. ${residente.habitacion} · ${residente.planta}"
            findViewById<TextView>(R.id.tvEstadoCuentaFamiliar).text = "Cuenta activa para solicitar citas"
        } else {
            findViewById<TextView>(R.id.tvResumenResidenteDashboard).text =
                "Tu solicitud está esperando la validación del personal de $residencia"
            findViewById<TextView>(R.id.tvEstadoCuentaFamiliar).text = "Cuenta creada, pendiente de aprobación"
        }

        findViewById<TextView>(R.id.tvPendientesFamilia).text = pendientes.toString()
        findViewById<TextView>(R.id.tvConfirmadasFamilia).text = confirmadas.toString()

        val badge = findViewById<TextView>(R.id.tvBadgeVisitas)
        badge.text = pendientes.toString()
        badge.visibility = if (pendientes > 0) View.VISIBLE else View.GONE

        if (proxima != null) {
            findViewById<TextView>(R.id.tvProximaVisita).text = "${proxima.fecha} a las ${proxima.hora}"
            findViewById<TextView>(R.id.tvDetalleProximaVisita).text = "Visita con ${proxima.nombreResidente}"
        } else {
            findViewById<TextView>(R.id.tvProximaVisita).text = "Todavía no tienes una visita confirmada"
            findViewById<TextView>(R.id.tvDetalleProximaVisita).text = "Puedes enviar una solicitud desde el botón principal"
        }
    }

    private fun cerrarSesion() {
        getSharedPreferences("resiplus_prefs", MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, SeleccionRolActivity::class.java))
        finishAffinity()
    }
}
