package com.example.resiplus
import android.os.Bundle; import android.view.LayoutInflater; import android.view.View; import android.view.ViewGroup
import android.widget.*; import androidx.appcompat.app.AppCompatActivity; import androidx.recyclerview.widget.LinearLayoutManager; import androidx.recyclerview.widget.RecyclerView
import com.example.resiplus.database.DatabaseHelper; import com.example.resiplus.model.Mensaje
import java.text.SimpleDateFormat; import java.util.*
class MensajeriaActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private lateinit var adapter: MensajeAdapter
    private val mensajes = mutableListOf<Mensaje>()
    private var idUsuario = -1; private var idReceptor = 2
    override fun onCreate(s: Bundle?) {
        super.onCreate(s); setContentView(R.layout.activity_mensajeria)
        db = DatabaseHelper(this)
        val prefs = getSharedPreferences("resiplus_prefs",MODE_PRIVATE)
        idUsuario = prefs.getInt("usuario_id",-1)
        val rol   = prefs.getString("usuario_rol","FAMILIAR") ?: "FAMILIAR"
        idReceptor = if (rol=="FAMILIAR") 2 else 1
        findViewById<TextView>(R.id.btnVolver).setOnClickListener { finish() }
        val rv = findViewById<RecyclerView>(R.id.rvMensajes)
        adapter = MensajeAdapter(mensajes)
        rv.layoutManager = LinearLayoutManager(this).apply { stackFromEnd=true }
        rv.adapter = adapter
        cargarMensajes(rv)
        val etMsg   = findViewById<EditText>(R.id.etMensaje)
        val btnEnv  = findViewById<TextView>(R.id.btnEnviar)
        btnEnv.setOnClickListener {
            val txt = etMsg.text.toString().trim()
            if (txt.isEmpty()) return@setOnClickListener
            val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            db.insertarMensaje(idUsuario,idReceptor,txt,hora)
            etMsg.text.clear(); cargarMensajes(rv)
        }
    }
    private fun cargarMensajes(rv: RecyclerView) {
        mensajes.clear()
        db.getMensajes(idUsuario,idReceptor).forEach { (txt,hora,esEnv) -> mensajes.add(Mensaje(txt,hora,esEnv)) }
        adapter.notifyDataSetChanged(); rv.scrollToPosition(mensajes.size-1)
    }
    inner class MensajeAdapter(private val datos: List<Mensaje>) : RecyclerView.Adapter<MensajeAdapter.VH>() {
        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val layEnv: View    = v.findViewById(R.id.layoutEnviado)
            val layRec: View    = v.findViewById(R.id.layoutRecibido)
            val tvEnv:  TextView= v.findViewById(R.id.tvTextoEnviado)
            val tvRec:  TextView= v.findViewById(R.id.tvTextoRecibido)
            val tvHEnv: TextView= v.findViewById(R.id.tvHoraEnviado)
            val tvHRec: TextView= v.findViewById(R.id.tvHoraRecibido)
        }
        override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(LayoutInflater.from(p.context).inflate(R.layout.item_mensaje,p,false))
        override fun getItemCount() = datos.size
        override fun onBindViewHolder(h: VH, i: Int) {
            val m = datos[i]
            if (m.esEnviado) {
                h.layEnv.visibility=View.VISIBLE; h.layRec.visibility=View.GONE
                h.tvEnv.text=m.texto; h.tvHEnv.text=m.hora
            } else {
                h.layRec.visibility=View.VISIBLE; h.layEnv.visibility=View.GONE
                h.tvRec.text=m.texto; h.tvHRec.text=m.hora
            }
        }
    }
}