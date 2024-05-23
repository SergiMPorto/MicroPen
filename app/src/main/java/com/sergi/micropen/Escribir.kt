package com.sergi.micropen

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.sergi.micropen.idioma.Idioma
import java.util.Locale

class Escribir : AppCompatActivity() {

    private lateinit var textEscrito: EditText
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var btnWrite: Button

    // Variables para el traductor
    private lateinit var btnTranslate: Button
    private lateinit var btnEnterLanguage: Button
    private lateinit var btnOutLanguage: Button
    private lateinit var mainLanguageList: ArrayList<Idioma>
    private lateinit var additionalLanguageList: ArrayList<Idioma>
    private var sourceLanguageCode = TranslateLanguage.SPANISH
    private var sourceLanguageTitle = "Español"
    private var targetLanguageCode = TranslateLanguage.ENGLISH
    private var targetLanguageTitle = "Inglés"
    private lateinit var translatorOptions: TranslatorOptions
    private lateinit var translator: Translator
    private lateinit var progressDialog: ProgressDialog
    private var sourceLanguageText = ""
    private lateinit var btnSwitchLanguagesWriter: ImageView
    private lateinit var languageManager: LanguageManager
    private lateinit var btnDownloadAdditionalLanguage: Button
    private lateinit var progressBar: ProgressBar
    private var downloadedLanguages: MutableSet<String> = mutableSetOf()
    private lateinit var escrituraTactil : Button


    companion object {
        private const val TAG = "MAIN_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_escribir)

        // Inicialización de variables
        textEscrito = findViewById(R.id.TextoEscrito)
        btnWrite = findViewById(R.id.button)
        btnDownloadAdditionalLanguage = findViewById(R.id.btnIdiomaAdiccional)
        btnTranslate = findViewById(R.id.btnTraducirText)
        btnEnterLanguage = findViewById(R.id.btnEnterTextText)
        btnOutLanguage = findViewById(R.id.btnOutTextText)
        btnSwitchLanguagesWriter = findViewById(R.id.btnSwitchLanguagesWriter)
        progressBar = findViewById(R.id.progressBar)
        escrituraTactil = findViewById(R.id.escrituraTactil)
        progressBar.max = 100
        progressBar.progress = 0

        //Obtener el texto escrito a dedo por un intent
        val recognizedText = intent.getStringExtra("RECOGNIZED_TEXT")
        textEscrito.setText(recognizedText)

        // Inicialización del administrador de idiomas y descarga de idiomas automáticos
        languageManager = LanguageManager(this)

        // Inicialización de TextToSpeech
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Error de escritura", Toast.LENGTH_LONG).show()
                }
            }
        }

        btnWrite.setOnClickListener {
            if (textEscrito.text.toString().trim().isNotEmpty()) {
                textToSpeech.speak(
                    textEscrito.text.toString().trim(),
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    null
                )
            } else {
                Toast.makeText(this, "Requerido", Toast.LENGTH_LONG).show()
            }
        }



        // Inicialización de la lista de idiomas
        loadLanguages()

      // Pasar a la pantalla para escribir en tactil

        escrituraTactil.setOnClickListener {
            val intent = Intent(this, TextToDigital::class.java)
            startActivity(intent)
        }

        // Listeners para la funcionalidad de traducción
        btnOutLanguage.setOnClickListener {
            Toast.makeText(this, "Elige el idioma de salida", Toast.LENGTH_LONG).show()
            showOutputLanguageMenu()
        }

        btnTranslate.setOnClickListener {
            Toast.makeText(this, "Traducir", Toast.LENGTH_LONG).show()
            validateData()
        }

        btnEnterLanguage.setOnClickListener {
            Toast.makeText(this, "Elige el idioma de entrada", Toast.LENGTH_LONG).show()
            showInputLanguageMenu()
            progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Por favor espere")
            progressDialog.setCanceledOnTouchOutside(false)
        }

        btnSwitchLanguagesWriter.setOnClickListener {
            switchLanguages()
        }

        btnDownloadAdditionalLanguage.setOnClickListener {
            Toast.makeText(this, "Elige el idioma que quieres descargar", Toast.LENGTH_LONG).show()
            showLanguageDownloadDialog()
        }
    }

    private fun loadLanguages() {
        mainLanguageList = arrayListOf(
            Idioma(TranslateLanguage.ENGLISH, "English"),
            Idioma(TranslateLanguage.SPANISH, "Español")
        )

        val allLanguageCodes = TranslateLanguage.getAllLanguages()
        additionalLanguageList = ArrayList()

        allLanguageCodes.forEach { languageCode ->
            if (mainLanguageList.none { it.languageCode == languageCode }) {
                val languageTitle = Locale(languageCode).displayLanguage
                additionalLanguageList.add(Idioma(languageCode, languageTitle, false))
            }
        }
        Log.d(TAG, "mainLanguageList: $mainLanguageList")
        Log.d(TAG, "additionalLanguageList: $additionalLanguageList")
    }

    private fun showInputLanguageMenu() {
        val popupMenu = PopupMenu(this, btnEnterLanguage)
        mainLanguageList.forEach { lang ->
            popupMenu.menu.add(lang.languageTitle)
        }
        popupMenu.setOnMenuItemClickListener { item ->
            val selectedLang = mainLanguageList.first { it.languageTitle == item.title }
            sourceLanguageCode = selectedLang.languageCode
            sourceLanguageTitle = selectedLang.languageTitle
            btnEnterLanguage.text = selectedLang.languageTitle
            true
        }
        popupMenu.show()
    }

    private fun showOutputLanguageMenu() {
        val popupMenu = PopupMenu(this, btnOutLanguage)
        mainLanguageList.forEach { lang ->
            popupMenu.menu.add(lang.languageTitle)
        }
        popupMenu.setOnMenuItemClickListener { item ->
            val selectedLang = mainLanguageList.first { it.languageTitle == item.title }
            targetLanguageCode = selectedLang.languageCode
            targetLanguageTitle = selectedLang.languageTitle
            btnOutLanguage.text = selectedLang.languageTitle
            true
        }
        popupMenu.show()
    }

    private fun showLanguageDownloadDialog() {
        if (additionalLanguageList.isEmpty()) {
            Toast.makeText(this, "No hay idiomas disponibles para descargar", Toast.LENGTH_LONG).show()
            return
        }
        val adapter = LanguageAdapter(this, additionalLanguageList, downloadedLanguages)
        val listView = ListView(this)
        listView.adapter = adapter

        AlertDialog.Builder(this).apply {
            setTitle("Download Languages")
            setView(listView)
            setNegativeButton("Cancel", null)
            create()
            show()
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedLanguage = additionalLanguageList[position]
            downloadLanguage(selectedLanguage, position, adapter)
        }
    }

    private fun downloadLanguage(language: Idioma, position: Int, adapter: LanguageAdapter) {
        if (!downloadedLanguages.contains(language.languageCode)) {
            progressDialog.show()
            languageManager.downloadLanguage(language.languageCode) { success ->
                progressDialog.dismiss()
                if (success) {
                    downloadedLanguages.add(language.languageCode)
                    language.isDownloaded = true
                    mainLanguageList.add(language) // <- Agregar a mainLanguageList
                    runOnUiThread {
                        adapter.notifyDataSetChanged()
                        updateLanguageMenus() // <- Cambio aquí
                    }
                    Toast.makeText(this, "${language.languageTitle} downloaded", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to download ${language.languageTitle}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Método para actualizar los menús de selección de idiomas
    private fun updateLanguageMenus() {
        btnEnterLanguage.setOnClickListener { showInputLanguageMenu() }
        btnOutLanguage.setOnClickListener { showOutputLanguageMenu() }
    }

    private fun switchLanguages() {
        val tempLanguageCode = sourceLanguageCode
        val tempLanguageTitle = sourceLanguageTitle

        sourceLanguageCode = targetLanguageCode
        sourceLanguageTitle = targetLanguageTitle
        targetLanguageCode = tempLanguageCode
        targetLanguageTitle = tempLanguageTitle

        btnEnterLanguage.text = sourceLanguageTitle
        btnOutLanguage.text = targetLanguageTitle
    }

    private fun validateData() {
        sourceLanguageText = textEscrito.text.toString().trim()

        if (sourceLanguageText.isEmpty()) {
            Toast.makeText(this, "No hay texto", Toast.LENGTH_LONG).show()
        } else {
            startTranslation()
        }
    }

    private fun startTranslation() {
        translatorOptions = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguageCode)
            .setTargetLanguage(targetLanguageCode)
            .build()

        translator = Translation.getClient(translatorOptions)

        val downloadConditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        translator.downloadModelIfNeeded(downloadConditions)
            .addOnSuccessListener {
                translateText(sourceLanguageText)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error al descargar el modelo de traducción: $exception")
            }
    }

    private fun translateText(text: String) {
        translator.translate(text)
            .addOnSuccessListener { translatedText ->
                textEscrito.text = Editable.Factory.getInstance().newEditable(translatedText)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error al traducir el texto: $exception")
            }


    }
}