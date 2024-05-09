package com.sergi.micropen

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class Home : AppCompatActivity() {

    private lateinit var grabar: ImageView
    private lateinit var escribir: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Toolbar para inflar el menú y que aparezcan los puntitos
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        grabar = findViewById(R.id.Record)

        grabar.setOnClickListener {
            val intent = Intent(this, Grabar::class.java)
            startActivity(intent)
        }

        escribir = findViewById(R.id.text)

        escribir.setOnClickListener {
            val intent = Intent(this, Escribir::class.java)
            startActivity(intent)
        }

    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Manejar las acciones del menú aquí
        when (item.itemId) {
            R.id.nav_logout -> {
                val intent = Intent(this, Login::class.java)
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

    // Dentro de la función downloadLanguageModels()
    private fun downloadLanguageModels() {
        val languageCodeList = TranslateLanguage.getAllLanguages()
        val totalLanguages = languageCodeList.size
        var languagesDownloaded = 0

        // Mostrar un diálogo de carga
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Descargando paquetes de idioma...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        languageCodeList.forEach { languageCode ->
            downloadModel(languageCode) { success ->
                if (success) {
                    languagesDownloaded++
                    // Actualizar la barra de progreso
                    progressDialog.progress = (languagesDownloaded.toFloat() / totalLanguages * 100).toInt()

                    if (languagesDownloaded == totalLanguages) {
                        progressDialog.dismiss()
                        Toast.makeText(
                            this@Home,
                            "Todos los paquetes de idioma se han descargado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }


    private fun downloadModel(languageCode: String, onComplete: (Boolean) -> Unit) {
        val translatorOptions = TranslatorOptions.Builder()
            .setSourceLanguage(languageCode)
            .build()
        val translator = Translation.getClient(translatorOptions)

        val downloadConditions = DownloadConditions.Builder()
            .build()

        translator.downloadModelIfNeeded(downloadConditions)
            .addOnSuccessListener {
                Toast.makeText(
                    this@Home,
                    "Paquete de idioma para $languageCode descargado con éxito",
                    Toast.LENGTH_SHORT
                ).show()
                onComplete(true)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this@Home,
                    "Error al descargar paquete de idioma para $languageCode",
                    Toast.LENGTH_SHORT
                ).show()
                onComplete(false)
            }
    }
}