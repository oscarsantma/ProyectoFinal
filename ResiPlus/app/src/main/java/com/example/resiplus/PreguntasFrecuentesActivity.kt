package com.example.resiplus

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PreguntasFrecuentesActivity : BaseActivity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.activity_preguntas_frecuentes)
        findViewById<TextView>(R.id.btnVolverFaq).setOnClickListener { finish() }
    }
}
