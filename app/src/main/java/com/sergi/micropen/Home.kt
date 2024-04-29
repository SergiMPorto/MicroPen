package com.sergi.micropen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException

class Home : AppCompatActivity() {

    private lateinit var grabar: ImageView
    private lateinit var escribir: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//Toolbar para inflar el menú y aparezcan los puntitos
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        grabar = findViewById(R.id.Record)

        grabar.setOnClickListener{
            val intent = Intent(this, Grabar::class.java)
            startActivity(intent)
        }

        escribir = findViewById(R.id.text)

        escribir.setOnClickListener{
            val intent = Intent(this, Escribir::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Manejar las acciones del menú aquí
        when (item.itemId) {
            R.id.nav_logout -> {
               val intent= Intent(this,Login::class.java)
                startActivity(intent)
                return true
            }
            R.id.nav_profile -> {
               val intent = Intent(this, Perfil::class.java)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}