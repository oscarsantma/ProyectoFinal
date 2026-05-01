package com.example.resiplus

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.resiplus.database.DatabaseHelper
import com.example.resiplus.model.Residente

class ResidentesResidenciaActivity : AppCompatActivity() {
    private lateinit var residencia: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_residentes_residencia)

        residencia = intent.getStringExtra("RESIDENCIA")
            ?: getSharedPreferences("resiplus_prefs", MODE_PRIVATE)
                .getString("usuario_residencia", "Residencia San Francisco")
            ?: "Residencia San Francisco"

        findViewById<TextView>(R.id.tvTituloResidentes).text = "Residentes de $residencia"
        findViewById<TextView>(R.id.btnVolverResidentes).setOnClickListener { finish() }
        findViewById<TextView>(R.id.btnNuevoResidente).setOnClickListener {
            startActivity(
                Intent(this, AltaResidenteActivity::class.java)
                    .putExtra("RESIDENCIA", residencia)
            )
        }
    }

    override fun onResume() {
        super.onResume()
        cargarResidentes()
    }

    private fun cargarResidentes() {
        val residentes = DatabaseHelper(this).getResidentesPorResidencia(residencia, true)
        val rv = findViewById<RecyclerView>(R.id.rvResidentesResidencia)
        val tvEmpty = findViewById<TextView>(R.id.tvSinResidentes)

        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = ResidenteAdapter(residentes)
        tvEmpty.visibility = if (residentes.isEmpty()) View.VISIBLE else View.GONE
        rv.visibility = if (residentes.isEmpty()) View.GONE else View.VISIBLE
    }

    inner class ResidenteAdapter(private val datos: List<Residente>) :
        RecyclerView.Adapter<ResidenteAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvNombre: TextView = view.findViewById(R.id.tvNombreResidenteItem)
            val tvHabitacion: TextView = view.findViewById(R.id.tvHabitacionResidenteItem)
            val tvNecesidades: TextView = view.findViewById(R.id.tvNecesidadesResidenteItem)
            val tvEstado: TextView = view.findViewById(R.id.tvEstadoResidenteItem)
            val btnEditar: TextView = view.findViewById(R.id.btnEditarResidente)
            val btnBaja: TextView = view.findViewById(R.id.btnBajaResidente)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(LayoutInflater.from(parent.context).inflate(R.layout.item_residente_residencia, parent, false))
        }

        override fun getItemCount() = datos.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val residente = datos[position]
            holder.tvNombre.text = residente.nombre
            holder.tvHabitacion.text = "Hab. ${residente.habitacion} · ${residente.planta}"
            holder.tvNecesidades.text = residente.necesidades
            holder.tvEstado.text = if (residente.activo) "Activo" else "De baja"
            holder.btnEditar.setOnClickListener {
                startActivity(
                    Intent(this@ResidentesResidenciaActivity, AltaResidenteActivity::class.java)
                        .putExtra("RESIDENTE_ID", residente.id)
                        .putExtra("RESIDENCIA", residencia)
                )
            }
            holder.btnBaja.text = if (residente.activo) "Dar de baja" else "Reactivar"
            holder.btnBaja.setOnClickListener {
                DatabaseHelper(this@ResidentesResidenciaActivity).actualizarEstadoResidente(residente.id, !residente.activo)
                cargarResidentes()
            }
        }
    }
}
