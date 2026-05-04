package com.example.resiplus
import android.content.Intent; import android.os.Bundle; import android.os.Handler; import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
class SplashActivity : BaseActivity() {
    override fun onCreate(s: Bundle?) {
        super.onCreate(s); setContentView(R.layout.activity_splash)
        Handler(Looper.getMainLooper()).postDelayed({ startActivity(Intent(this, SeleccionRolActivity::class.java)); finish() }, 2000)
    }
}