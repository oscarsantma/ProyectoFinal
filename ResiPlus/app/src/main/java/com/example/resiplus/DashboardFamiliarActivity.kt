package com.example.resiplus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.resiplus.database.DatabaseHelper

class DashboardFamiliarActivity : AppCompatActivity() {
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
        val usuarioId = prefs.getInt("usuario_id", -1)
        val residencia = prefs.getString("usuario_residencia", "") ?: ""
        val residente = db.getResidenteVinculado(usuarioId)
        val visitas = db.getVisitasFamiliar(usuarioId)
        val pendientes = visitas.count { it.estado == "PENDIENTE" }
        val confirmadas = visitas.count { it.estado == "CONFIRMADA" }
        val proxima = visitas.firstOrNull { it.estado == "CONFIRMADA" }

        findViewById<TextView>(R.id.tvSaludo).text = "Hola, ${nombre.split(" ").first()}"
        findViewById<TextView>(R.id.tvNombreResidenteDashboard).text = residente?.nombre ?: "Vinculo pendiente"
        findViewById<TextView>(R.id.tvResumenResidenteDashboard).text = if (residente != null) {
            "${residente.residencia} · Hab. ${residente.habitacion} · ${residente.planta}"
        } else {
            "Tu solicitud está esperando la validación del personal de $residencia"
        }
        findViewById<TextView>(R.id.tvPendientesFamilia).text = pendientes.toString()
        findViewById<TextView>(R.id.tvConfirmadasFamilia).text = confirmadas.toString()
        findViewById<TextView>(R.id.tvBadgeVisitas).apply {
            text = if (pendientes > 0) pendientes.toString() else ""
            visibility = if (pendientes > 0) View.VISIBLE else View.GONE
        }
        findViewById<TextView>(R.id.tvEstadoCuentaFamiliar).text = if (residente != null) {
            "Cuenta activa para solicitar citas"
        } else {
            "Cuenta creada, pendiente de aprobación"
        }
        findViewById<TextView>(R.id.tvProximaVisita).text = if (proxima != null) {
            "${proxima.fecha} a las ${proxima.hora}"
        } else {
            "Todavía no tienes una visita confirmada"
        }
        findViewById<TextView>(R.id.tvDetalleProximaVisita).text = if (proxima != null) {
            "Visita con ${proxima.nombreResidente}"
        } else {
            "Puedes enviar una solicitud desde el botón principal"
        }
    }

    private fun cerrarSesion() {
        getSharedPreferences("resiplus_prefs", MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, SeleccionRolActivity::class.java))
        finishAffinity()
    }
}
