package com.example.resiplus

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// para que no se tape el contenido con la barra de navegacion en android 15+
// sin esto los botones de abajo quedaban ocultos en algunos moviles
abstract class BaseActivity : AppCompatActivity() {

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        val root = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, ins ->
            val nav = ins.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(0, 0, 0, nav.bottom)
            ins
        }
    }
}
