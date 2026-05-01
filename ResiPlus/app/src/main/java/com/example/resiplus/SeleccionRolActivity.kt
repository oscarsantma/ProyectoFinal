package com.example.resiplus
import android.content.Intent; import android.os.Bundle; import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
class SeleccionRolActivity : AppCompatActivity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s); setContentView(R.layout.activity_seleccion_rol)
        findViewById<LinearLayout>(R.id.cardFamiliar).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java).putExtra("ROL","FAMILIAR")) }
        findViewById<LinearLayout>(R.id.cardPersonal).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java).putExtra("ROL","PERSONAL")) }
        findViewById<LinearLayout>(R.id.cardAdmin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java).putExtra("ROL","ADMIN")) }
    }
}
