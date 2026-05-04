package com.example.resiplus

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.resiplus.database.DatabaseHelper

class GestionResidenciasActivity : BaseActivity() {
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestion_residencias)
        db = DatabaseHelper(this)

        findViewById<TextView>(R.id.btnVolverResidencias).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnCrearResidencia).setOnClickListener { crearResidencia() }
        findViewById<Button>(R.id.btnCrearUsuarioResidencia).setOnClickListener {
            startActivity(Intent(this, AltaUsuarioActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        cargarResidencias()
    }

    private fun crearResidencia() {
        val input = findViewById<EditText>(R.id.etNombreResidencia)
        val nombre = input.text.toString().trim()
        if (nombre.isEmpty()) {
            Toast.makeText(this, "Escribe un nombre de residencia", Toast.LENGTH_SHORT).show()
            return
        }

        val ok = db.insertarResidencia(nombre)
        if (ok == -1L) {
            Toast.makeText(this, "No se pudo crear o ya existe", Toast.LENGTH_LONG).show()
        } else {
            input.text.clear()
            Toast.makeText(this, "Residencia creada", Toast.LENGTH_LONG).show()
            cargarResidencias()
        }
    }

    private fun cargarResidencias() {
        val datos = db.getResidencias()
        findViewById<TextView>(R.id.tvResumenResidencias).text =
            "${datos.size} residencias activas en la plataforma"
        findViewById<RecyclerView>(R.id.rvResidencias).apply {
            layoutManager = LinearLayoutManager(this@GestionResidenciasActivity)
            adapter = ResidenciaAdapter(datos)
        }
    }

    inner class ResidenciaAdapter(private val datos: List<String>) :
        RecyclerView.Adapter<ResidenciaAdapter.VH>() {

        inner class VH(view: View) : RecyclerView.ViewHolder(view) {
            val tvNombre: TextView = view.findViewById(R.id.tvNombreResidenciaItem)
            val tvDetalle: TextView = view.findViewById(R.id.tvDetalleResidenciaItem)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(LayoutInflater.from(parent.context).inflate(R.layout.item_residencia, parent, false))
        }

        override fun getItemCount() = datos.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val residencia = datos[position]
            holder.tvNombre.text = residencia
            holder.tvDetalle.text = "${db.contarResidentesPorResidencia(residencia)} residentes activos"
            holder.itemView.setOnClickListener {
                startActivity(
                    Intent(this@GestionResidenciasActivity, ResidentesResidenciaActivity::class.java)
                        .putExtra("RESIDENCIA", residencia)
                )
            }
        }
    }
}
