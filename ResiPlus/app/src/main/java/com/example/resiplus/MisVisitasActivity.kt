package com.example.resiplus
import android.graphics.Color; import android.os.Bundle; import android.view.LayoutInflater; import android.view.View; import android.view.ViewGroup
import android.widget.*; import androidx.appcompat.app.AppCompatActivity; import androidx.recyclerview.widget.LinearLayoutManager; import androidx.recyclerview.widget.RecyclerView
import com.example.resiplus.database.DatabaseHelper; import com.example.resiplus.model.Visita
class MisVisitasActivity : BaseActivity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s); setContentView(R.layout.activity_mis_visitas)
        val id = getSharedPreferences("resiplus_prefs",MODE_PRIVATE).getInt("usuario_id",-1)
        val db = DatabaseHelper(this)
        findViewById<TextView>(R.id.btnVolver).setOnClickListener { finish() }
        val lista = db.getVisitasFamiliar(id)
        val rv    = findViewById<RecyclerView>(R.id.rvVisitas)
        val tvSin = findViewById<TextView>(R.id.tvSinVisitas)
        if (lista.isEmpty()) { rv.visibility=View.GONE; tvSin.visibility=View.VISIBLE }
        else { rv.layoutManager=LinearLayoutManager(this); rv.adapter=VisitaAdapter(lista) }
    }
    inner class VisitaAdapter(private val datos: List<Visita>) : RecyclerView.Adapter<VisitaAdapter.VH>() {
        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tvFecha: TextView = v.findViewById(R.id.tvFechaVisita)
            val tvHora:  TextView = v.findViewById(R.id.tvHoraVisita)
            val tvNota:  TextView = v.findViewById(R.id.tvNotaVisita)
            val tvEst:   TextView = v.findViewById(R.id.tvEstadoVisita)
            val indic:   View     = v.findViewById(R.id.indicadorEstado)
        }
        override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(LayoutInflater.from(p.context).inflate(R.layout.item_visita,p,false))
        override fun getItemCount() = datos.size
        override fun onBindViewHolder(h: VH, i: Int) {
            val v = datos[i]
            h.tvFecha.text = v.fecha; h.tvHora.text = "Hora: ${v.hora}"
            if (v.nota.isNotEmpty()) { h.tvNota.text = "${v.nombreResidente}: ${v.nota}"; h.tvNota.visibility=View.VISIBLE } else { h.tvNota.text = v.nombreResidente; h.tvNota.visibility = View.VISIBLE }
            val (color,bg,txt) = when(v.estado) {
                "CONFIRMADA" -> Triple(Color.parseColor("#27AE60"), R.drawable.bg_badge_confirmada, "CONFIRMADA")
                "RECHAZADA"  -> Triple(Color.parseColor("#C0392B"), R.drawable.bg_badge_rechazada,  "RECHAZADA")
                else         -> Triple(Color.parseColor("#E67E22"), R.drawable.bg_badge_pendiente,  "PENDIENTE")
            }
            h.tvEst.text=txt; h.tvEst.setTextColor(color); h.tvEst.setBackgroundResource(bg); h.indic.setBackgroundColor(color)
        }
    }
}
