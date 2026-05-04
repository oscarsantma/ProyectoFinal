package com.example.resiplus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.resiplus.database.DatabaseHelper
import com.example.resiplus.model.Usuario
import com.example.resiplus.model.Visita

class GestionarSolicitudesActivity : BaseActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var usuarioRol: String
    private lateinit var residencia: String
    private val solicitudes = mutableListOf<Visita>()
    private val registros = mutableListOf<Usuario>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestionar_solicitudes)

        db = DatabaseHelper(this)
        val prefs = getSharedPreferences("resiplus_prefs", MODE_PRIVATE)
        usuarioRol = prefs.getString("usuario_rol", "PERSONAL") ?: "PERSONAL"
        residencia = prefs.getString("usuario_residencia", "Residencia San Francisco") ?: "Residencia San Francisco"

        findViewById<TextView>(R.id.btnVolver).setOnClickListener { finish() }
        findViewById<RecyclerView>(R.id.rvSolicitudes).apply {
            layoutManager = LinearLayoutManager(this@GestionarSolicitudesActivity)
            adapter = SolicitudAdapter(solicitudes)
        }
        findViewById<RecyclerView>(R.id.rvRegistrosPendientes).apply {
            layoutManager = LinearLayoutManager(this@GestionarSolicitudesActivity)
            adapter = RegistroAdapter(registros)
        }
        configurarPantalla()
    }

    override fun onResume() {
        super.onResume()
        cargar()
    }

    private fun configurarPantalla() {
        val titulo = findViewById<TextView>(R.id.tvTituloSolicitudes)
        val subtituloRegistros = findViewById<TextView>(R.id.tvTituloRegistros)
        val subtituloVisitas = findViewById<TextView>(R.id.tvTituloVisitas)
        val tvSinRegistros = findViewById<TextView>(R.id.tvSinRegistros)
        val tvSinSolicitudes = findViewById<TextView>(R.id.tvSinSolicitudes)

        if (usuarioRol == "ADMIN") {
            titulo.text = "Altas pendientes del personal"
            subtituloRegistros.text = "SOLICITUDES DE EMPLEADOS"
            subtituloVisitas.visibility = View.GONE
            findViewById<RecyclerView>(R.id.rvSolicitudes).visibility = View.GONE
            tvSinSolicitudes.visibility = View.GONE
            tvSinRegistros.text = "No hay empleados pendientes de validación."
        } else {
            titulo.text = "Solicitudes pendientes en $residencia"
            subtituloRegistros.text = "FAMILIARES PENDIENTES"
            subtituloVisitas.text = "CITAS PENDIENTES"
            tvSinRegistros.text = "No hay familiares pendientes de revisión."
            tvSinSolicitudes.text = "No hay citas pendientes."
        }
    }

    private fun cargar() {
        val rvSolicitudes = findViewById<RecyclerView>(R.id.rvSolicitudes)
        val rvRegistros = findViewById<RecyclerView>(R.id.rvRegistrosPendientes)
        val tvSinSolicitudes = findViewById<TextView>(R.id.tvSinSolicitudes)
        val tvSinRegistros = findViewById<TextView>(R.id.tvSinRegistros)

        registros.clear()
        solicitudes.clear()

        if (usuarioRol == "ADMIN") {
            registros.addAll(db.getSolicitudesRegistroPendientesParaAdmin())
        } else {
            registros.addAll(db.getSolicitudesFamiliaresPendientes(residencia))
            solicitudes.addAll(db.getVisitasPendientes(residencia))
        }

        rvRegistros.adapter?.notifyDataSetChanged()
        rvSolicitudes.adapter?.notifyDataSetChanged()

        rvRegistros.visibility = if (registros.isEmpty()) View.GONE else View.VISIBLE
        tvSinRegistros.visibility = if (registros.isEmpty()) View.VISIBLE else View.GONE

        if (usuarioRol == "ADMIN") {
            rvSolicitudes.visibility = View.GONE
        } else {
            rvSolicitudes.visibility = if (solicitudes.isEmpty()) View.GONE else View.VISIBLE
            tvSinSolicitudes.visibility = if (solicitudes.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    inner class SolicitudAdapter(private val datos: MutableList<Visita>) :
        RecyclerView.Adapter<SolicitudAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvNombre: TextView = view.findViewById(R.id.tvNombreFamiliar)
            val tvFechaH: TextView = view.findViewById(R.id.tvFechaHoraSolicitud)
            val tvNota: TextView = view.findViewById(R.id.tvNotaSolicitud)
            val btnConfirmar: Button = view.findViewById(R.id.btnConfirmar)
            val btnRechazar: Button = view.findViewById(R.id.btnRechazar)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(LayoutInflater.from(parent.context).inflate(R.layout.item_solicitud, parent, false))
        }

        override fun getItemCount() = datos.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val visita = datos[position]
            holder.tvNombre.text = "${visita.nombreFamiliar} · ${visita.nombreResidente}"
            holder.tvFechaH.text = "${visita.fecha} a las ${visita.hora}"
            if (visita.nota.isBlank()) {
                holder.tvNota.visibility = View.GONE
            } else {
                holder.tvNota.visibility = View.VISIBLE
                holder.tvNota.text = "Motivo: ${visita.nota}"
            }
            holder.btnConfirmar.setOnClickListener {
                db.actualizarEstadoVisita(visita.id, "CONFIRMADA")
                Toast.makeText(this@GestionarSolicitudesActivity, "Cita aceptada", Toast.LENGTH_SHORT).show()
                cargar()
            }
            holder.btnRechazar.setOnClickListener {
                db.actualizarEstadoVisita(visita.id, "RECHAZADA")
                Toast.makeText(this@GestionarSolicitudesActivity, "Cita rechazada", Toast.LENGTH_SHORT).show()
                cargar()
            }
        }
    }

    inner class RegistroAdapter(private val datos: MutableList<Usuario>) :
        RecyclerView.Adapter<RegistroAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvNombre: TextView = view.findViewById(R.id.tvNombreRegistroPendiente)
            val tvDetalle: TextView = view.findViewById(R.id.tvDetalleRegistroPendiente)
            val btnAprobar: Button = view.findViewById(R.id.btnAprobarRegistro)
            val btnRechazar: Button = view.findViewById(R.id.btnRechazarRegistro)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(LayoutInflater.from(parent.context).inflate(R.layout.item_registro_pendiente, parent, false))
        }

        override fun getItemCount() = datos.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val usuario = datos[position]
            val residente = usuario.idResidente?.let { db.getResidente(it)?.nombre }

            holder.tvNombre.text = "${usuario.nombre} · ${usuario.rol}"
            holder.tvDetalle.text = if (usuario.rol == "FAMILIAR") {
                "${usuario.email}\nResidencia: ${usuario.residencia}\nFamiliar vinculado: ${residente ?: "Sin asignar"}"
            } else {
                "${usuario.email}\nResidencia solicitada: ${usuario.residencia}"
            }

            holder.btnAprobar.setOnClickListener {
                db.actualizarEstadoUsuario(usuario.id, "APROBADO")
                Toast.makeText(this@GestionarSolicitudesActivity, "Solicitud aprobada", Toast.LENGTH_SHORT).show()
                cargar()
            }
            holder.btnRechazar.setOnClickListener {
                db.actualizarEstadoUsuario(usuario.id, "RECHAZADO")
                Toast.makeText(this@GestionarSolicitudesActivity, "Solicitud rechazada", Toast.LENGTH_SHORT).show()
                cargar()
            }
        }
    }
}
