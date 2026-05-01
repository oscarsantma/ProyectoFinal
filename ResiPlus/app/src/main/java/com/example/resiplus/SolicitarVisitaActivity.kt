package com.example.resiplus

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.resiplus.database.DatabaseHelper

class SolicitarVisitaActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper
    private val horas = listOf("09:00","10:00","11:00","12:00","16:00","17:00","18:00","19:00")
    private var fechaSel = ""; private var horaSel = ""
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.activity_solicitar_visita)

        db = DatabaseHelper(this)
        val id = getSharedPreferences("resiplus_prefs",MODE_PRIVATE).getInt("usuario_id",-1)
        val residente = db.getResidenteVinculado(id)

        findViewById<TextView>(R.id.btnVolver).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvResidenteSolicitarVisita).text = if (residente != null) {
            "${residente.nombre} · Hab. ${residente.habitacion}"
        } else {
            "Vinculo pendiente de aprobacion"
        }

        val tvFecha   = findViewById<TextView>(R.id.tvFechaSeleccionada)
        val tvHTitulo = findViewById<TextView>(R.id.tvHorasTitle)
        val layHoras  = findViewById<LinearLayout>(R.id.layoutHoras)
        val etNota    = findViewById<EditText>(R.id.etNota)
        findViewById<Button>(R.id.btnSeleccionarFecha).setOnClickListener {
            val hoy = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_MONTH,1) }
            DatePickerDialog(this,{ _,y,m,d ->
                val mostrar = String.format("%02d/%02d/%04d",d,m+1,y)
                fechaSel = String.format("%04d-%02d-%02d",y,m+1,d)
                tvFecha.text="Fecha seleccionada: $mostrar"; tvFecha.visibility=View.VISIBLE
                if (residente != null) {
                    cargarHoras(fechaSel,layHoras,tvHTitulo, residente.id)
                }
            },hoy.get(java.util.Calendar.YEAR),hoy.get(java.util.Calendar.MONTH),hoy.get(java.util.Calendar.DAY_OF_MONTH)).apply{
                datePicker.minDate=hoy.timeInMillis; show() }
        }
        findViewById<Button>(R.id.btnConfirmarVisita).setOnClickListener {
            when {
                residente == null -> Toast.makeText(this,"Tu vinculo aun no ha sido aprobado",Toast.LENGTH_SHORT).show()
                fechaSel.isEmpty() -> Toast.makeText(this,"Selecciona una fecha",Toast.LENGTH_SHORT).show()
                horaSel.isEmpty()  -> Toast.makeText(this,"Selecciona una hora",Toast.LENGTH_SHORT).show()
                else -> { val ok=db.insertarVisita(id,fechaSel,horaSel,etNota.text.toString().trim())
                    if (ok!=-1L){ Toast.makeText(this,"Solicitud enviada. Estado: Pendiente",Toast.LENGTH_LONG).show(); finish() }
                    else Toast.makeText(this,"Error al enviar",Toast.LENGTH_SHORT).show() }
            }
        }
    }
    private fun cargarHoras(fecha:String,lay:LinearLayout,tit:TextView,idResidente:Int){
        lay.removeAllViews(); horaSel=""; tit.visibility=View.VISIBLE
        val ocup=db.getHorasOcupadas(fecha, idResidente)
        var fila:LinearLayout?=null
        horas.forEachIndexed { i,h ->
            if (i%2==0){ fila=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL
                layoutParams=LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT).apply{setMargins(0,6,0,6)} }
                lay.addView(fila) }
            val btn=Button(this).apply{ text=h; textSize=13f
                layoutParams=LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1f).apply{setMargins(6,0,6,0)}
                if(ocup.contains(h)){isEnabled=false;alpha=0.4f;setBackgroundResource(R.drawable.bg_card)}
                else{setBackgroundResource(R.drawable.bg_boton_verde);setTextColor(getColor(R.color.blanco))
                    setOnClickListener{horaSel=h;resaltar(lay,h)}} }
            fila?.addView(btn)
        }
    }
    private fun resaltar(lay:LinearLayout,el:String){
        for(i in 0 until lay.childCount){ val f=lay.getChildAt(i) as? LinearLayout?:continue
            for(j in 0 until f.childCount){ val b=f.getChildAt(j) as? Button?:continue; if(!b.isEnabled)continue
                if(b.text==el) b.setBackgroundColor(getColor(R.color.verde_andalucia_dark)) else b.setBackgroundResource(R.drawable.bg_boton_verde) } }
    }
}
