package com.sergi.micropen

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class SplashMicroPen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_splash_micro_pen)

            run()
        }

        @SuppressLint("SuspiciousIndentation")
        fun run(){
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }




    }
