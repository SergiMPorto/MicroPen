package com.sergi.micropen

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashMicroPen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_micro_pen)

        run()
    }

    fun run() {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
    }
}
