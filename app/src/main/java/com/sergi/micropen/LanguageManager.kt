
package com.sergi.micropen

import android.app.ProgressDialog
import android.content.Context
import android.widget.Toast
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class LanguageManager(private val context: Context) {
    private val autoDownloadLanguageCodeList = listOf(
        TranslateLanguage.ARABIC,
        TranslateLanguage.ENGLISH,
        TranslateLanguage.CHINESE,
        TranslateLanguage.JAPANESE,
        TranslateLanguage.GERMAN,
        TranslateLanguage.FRENCH,
        TranslateLanguage.ITALIAN,
        TranslateLanguage.SPANISH,
        TranslateLanguage.PORTUGUESE,
        TranslateLanguage.NORWEGIAN,
        TranslateLanguage.GALICIAN,
        TranslateLanguage.RUSSIAN,
        TranslateLanguage.CATALAN,
        TranslateLanguage.FINNISH,
        TranslateLanguage.GREEK,
        TranslateLanguage.BULGARIAN,
        TranslateLanguage.HUNGARIAN,
        TranslateLanguage.DUTCH,
        TranslateLanguage.SWEDISH,
        TranslateLanguage.DANISH,
        TranslateLanguage.POLISH,
        TranslateLanguage.ROMANIAN
    )
    private val totalLanguages = autoDownloadLanguageCodeList.size
    private var languagesDownloaded = 0
    private val progressDialog = ProgressDialog(context)

    init {
        progressDialog.setMessage("Descargando paquetes de idioma...")
        progressDialog.setCancelable(false)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.max = totalLanguages
        progressDialog.show()
    }

    fun downloadAllLanguages() {
        autoDownloadLanguageCodeList.forEach { languageCode ->
            downloadLanguage(languageCode)
        }
    }

    fun downloadLanguage(languageCode: String) {
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
            progressDialog.progress = languagesDownloaded
            if (languagesDownloaded == totalLanguages) {
                progressDialog.dismiss()
                Toast.makeText(context, "Todos los paquetes de idioma se han descargado", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Error al descargar el paquete de $languageCode", Toast.LENGTH_SHORT).show()
        }
    }

    fun getAutoDownloadLanguages(): List<String>{
        return autoDownloadLanguageCodeList
    }
}