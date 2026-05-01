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
import com.example.resiplus.model.Residente

class SolicitarVisitaActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private val horas = listOf("09:00", "10:00", "11:00", "12:00", "16:00", "17:00", "18:00", "19:00")
    private var fechaSel = ""
    private var horaSel = ""
    private var residente: Residente? = null

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.activity_solicitar_visita)

        db = DatabaseHelper(this)

        val prefs = getSharedPreferences("resiplus_prefs", MODE_PRIVATE)
        val userId = prefs.getInt("usuario_id", -1)

        val tvResidente = findViewById<TextView>(R.id.tvResidenteSolicitarVisita)
        val tvFecha    = findViewById<TextView>(R.id.tvFechaSeleccionada)
        val tvHTitulo  = findViewById<TextView>(R.id.tvHorasTitle)
        val layHoras   = findViewById<LinearLayout>(R.id.layoutHoras)
        val etNota     = findViewById<EditText>(R.id.etNota)

        findViewById<TextView>(R.id.btnVolver).setOnClickListener { finish() }

        Thread {
            val res = db.getResidenteVinculado(userId)
            runOnUiThread {
                residente = res
                tvResidente.text = if (res != null) {
                    "${res.nombre} · Hab. ${res.habitacion}"
                } else {
                    "Vinculo pendiente de aprobacion"
                }
            }
        }.start()

        findViewById<Button>(R.id.btnSeleccionarFecha).setOnClickListener {
            val hoy = java.util.Calendar.getInstance().apply {
                add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
            DatePickerDialog(this, { _, y, m, d ->
                val mostrar = String.format("%02d/%02d/%04d", d, m + 1, y)
                fechaSel = String.format("%04d-%02d-%02d", y, m + 1, d)
                tvFecha.text = "Fecha seleccionada: $mostrar"
                tvFecha.visibility = View.VISIBLE
                val res = residente
                if (res != null) {
                    cargarHoras(fechaSel, layHoras, tvHTitulo, res.id)
                }
            }, hoy.get(java.util.Calendar.YEAR), hoy.get(java.util.Calendar.MONTH), hoy.get(java.util.Calendar.DAY_OF_MONTH)).apply {
                datePicker.minDate = hoy.timeInMillis
                show()
            }
        }

        findViewById<Button>(R.id.btnConfirmarVisita).setOnClickListener {
            val res = residente
            when {
                res == null    -> Toast.makeText(this, "Tu vinculo aun no ha sido aprobado", Toast.LENGTH_SHORT).show()
                fechaSel.isEmpty() -> Toast.makeText(this, "Selecciona una fecha", Toast.LENGTH_SHORT).show()
                horaSel.isEmpty()  -> Toast.makeText(this, "Selecciona una hora", Toast.LENGTH_SHORT).show()
                else -> {
                    val nota = etNota.text.toString().trim()
                    Thread {
                        val ok = db.insertarVisita(userId, fechaSel, horaSel, nota)
                        runOnUiThread {
                            if (ok != -1L) {
                                Toast.makeText(this, "Solicitud enviada. Estado: Pendiente", Toast.LENGTH_LONG).show()
                                finish()
                            } else {
                                Toast.makeText(this, "Error al enviar la solicitud", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }.start()
                }
            }
        }
    }

    private fun cargarHoras(fecha: String, lay: LinearLayout, tit: TextView, idResidente: Int) {
        lay.removeAllViews()
        horaSel = ""
        tit.visibility = View.VISIBLE

        Thread {
            val ocupadas = db.getHorasOcupadas(fecha, idResidente)
            runOnUiThread {
                var fila: LinearLayout? = null
                horas.forEachIndexed { i, hora ->
                    if (i % 2 == 0) {
                        fila = LinearLayout(this).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply { setMargins(0, 6, 0, 6) }
                        }
                        lay.addView(fila)
                    }
                    val btn = Button(this).apply {
                        text = hora
                        textSize = 13f
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                            setMargins(6, 0, 6, 0)
                        }
                        if (ocupadas.contains(hora)) {
                            isEnabled = false
                            alpha = 0.4f
                            setBackgroundResource(R.drawable.bg_card)
                        } else {
                            setBackgroundResource(R.drawable.bg_boton_verde)
                            setTextColor(getColor(R.color.blanco))
                            setOnClickListener {
                                horaSel = hora
                                resaltar(lay, hora)
                            }
                        }
                    }
                    fila?.addView(btn)
                }
            }
        }.start()
    }

    private fun resaltar(lay: LinearLayout, seleccionada: String) {
        for (i in 0 until lay.childCount) {
            val fila = lay.getChildAt(i) as? LinearLayout ?: continue
            for (j in 0 until fila.childCount) {
                val btn = fila.getChildAt(j) as? Button ?: continue
                if (!btn.isEnabled) continue
                if (btn.text == seleccionada) {
                    btn.setBackgroundColor(getColor(R.color.verde_andalucia_dark))
                } else {
                    btn.setBackgroundResource(R.drawable.bg_boton_verde)
                }
            }
        }
    }
}
