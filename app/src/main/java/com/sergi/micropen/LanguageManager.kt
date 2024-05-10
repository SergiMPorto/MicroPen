
package com.sergi.micropen

import android.app.ProgressDialog
import android.content.Context
import android.widget.Toast
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class LanguageManager(private val context: Context) {
    private val languageCodeList = TranslateLanguage.getAllLanguages()
    private val totalLanguages = languageCodeList.size
    private var languagesDownloaded = 0  // Se inicializa el contador de idiomas descargados
        private val progressDialog = ProgressDialog(context)


    init {
        progressDialog.setMessage("Descargando paquetes de idioma...")
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    fun downloadAllLanguages() {
        languageCodeList.forEach { languageCode ->
            downloadLanguage(languageCode)
        }
    }

    private fun downloadLanguage(languageCode: String) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.SPANISH)
            .setTargetLanguage(languageCode)
            .build()

        val translator = Translation.getClient(options)

        translator.downloadModelIfNeeded().addOnSuccessListener {
            onLanguageDownloaded(true, languageCode)
        }.addOnFailureListener {
            onLanguageDownloaded(false, languageCode)
        }
    }

    private fun onLanguageDownloaded(success: Boolean, languageCode: String) {
        if (success) {
            languagesDownloaded++
            if (languagesDownloaded == totalLanguages) {
                progressDialog.dismiss()
                Toast.makeText(context, "Todos los paquetes de idioma se han descargado", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Error al descargar el paquete de $languageCode", Toast.LENGTH_SHORT).show()
        }
    }
}