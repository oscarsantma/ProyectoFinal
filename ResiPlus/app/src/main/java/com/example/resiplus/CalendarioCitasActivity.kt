package com.example.resiplus

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.resiplus.database.DatabaseHelper
import com.example.resiplus.model.Visita

class CalendarioCitasActivity : BaseActivity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.activity_calendario_citas)

        val residencia = getSharedPreferences("resiplus_prefs", MODE_PRIVATE)
            .getString("usuario_residencia", "Residencia San Francisco") ?: "Residencia San Francisco"
        val db = DatabaseHelper(this)
        val visitas = db.getVisitasPorResidencia(residencia)
        val rv = findViewById<RecyclerView>(R.id.rvCalendario)
        val tvEmpty = findViewById<TextView>(R.id.tvSinCitasCalendario)

        findViewById<TextView>(R.id.btnVolverCalendario).setOnClickListener { finish() }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = CalendarioAdapter(visitas)
        tvEmpty.visibility = if (visitas.isEmpty()) View.VISIBLE else View.GONE
        rv.visibility = if (visitas.isEmpty()) View.GONE else View.VISIBLE
    }

    inner class CalendarioAdapter(private val datos: List<Visita>) : RecyclerView.Adapter<CalendarioAdapter.VH>() {
        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvFecha = v.findViewById<TextView>(R.id.tvFechaCalendario)
            val tvHora = v.findViewById<TextView>(R.id.tvHoraCalendario)
            val tvFamilia = v.findViewById<TextView>(R.id.tvFamiliaCalendario)
            val tvResidente = v.findViewById<TextView>(R.id.tvResidenteCalendario)
            val tvEstado = v.findViewById<TextView>(R.id.tvEstadoCalendario)
        }

        override fun onCreateViewHolder(p: ViewGroup, t: Int): VH {
            return VH(LayoutInflater.from(p.context).inflate(R.layout.item_calendario_cita, p, false))
        }

        override fun getItemCount() = datos.size

        override fun onBindViewHolder(h: VH, i: Int) {
            val v = datos[i]
            h.tvFecha.text = v.fecha
            h.tvHora.text = v.hora
            h.tvFamilia.text = "Familiar: ${v.nombreFamiliar}"
            h.tvResidente.text = "Residente: ${v.nombreResidente}"
            h.tvEstado.text = v.estado
            when (v.estado) {
                "CONFIRMADA" -> {
                    h.tvEstado.setBackgroundResource(R.drawable.bg_badge_confirmada)
                    h.tvEstado.setTextColor(Color.parseColor("#007A3D"))
                }
                "RECHAZADA" -> {
                    h.tvEstado.setBackgroundResource(R.drawable.bg_badge_rechazada)
                    h.tvEstado.setTextColor(Color.parseColor("#B73A3A"))
                }
                else -> {
                    h.tvEstado.setBackgroundResource(R.drawable.bg_badge_pendiente)
                    h.tvEstado.setTextColor(Color.parseColor("#E67E22"))
                }
            }
        }
    }
}
