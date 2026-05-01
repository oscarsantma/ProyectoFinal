package com.example.resiplus

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SectionDetailActivity : AppCompatActivity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        setContentView(R.layout.activity_section_detail)

        val profile = intent.getStringExtra("PROFILE") ?: "PERSONAL"
        val sectionKey = intent.getStringExtra("SECTION_KEY") ?: "pendientes"
        val info = SectionInfoProvider.get(profile, sectionKey)

        findViewById<TextView>(R.id.btnVolverDetalleSeccion).setOnClickListener { finish() }
        findViewById<TextView>(R.id.tvIconoDetalleSeccion).text = info.icon
        findViewById<TextView>(R.id.tvTituloDetalleSeccion).text = info.title
        findViewById<TextView>(R.id.tvResumenDetalleSeccion).text = info.summary
        findViewById<TextView>(R.id.tvInformacionDetalleSeccion).text = info.information
        findViewById<TextView>(R.id.tvAccionesDetalleSeccion).text = info.actions
        findViewById<TextView>(R.id.tvPermisosDetalleSeccion).text = info.permissions
    }
}
