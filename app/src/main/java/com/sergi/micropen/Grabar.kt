package com.sergi.micropen

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.sergi.micropen.idioma.Idioma
import java.text.Normalizer
import java.util.Locale
import java.util.regex.Pattern

class Grabar : AppCompatActivity() {
    private lateinit var bSpeak: ImageButton
    private lateinit var btnTranslate: Button
    private lateinit var btnEnterLanguage: Button
    private lateinit var btnOutLanguage: Button
    private lateinit var edText: TextView
    private lateinit var mainLanguageList: ArrayList<Idioma>
    private lateinit var additionalLanguageList: ArrayList<Idioma>
    private lateinit var languageManager: LanguageManager

    private var downloadedLanguages: MutableSet<String> = mutableSetOf()
    companion object {
        private const val TAG = "GrabarActivity"
        private const val REQUEST_CODE_SPEECH_INPUT = 100
    }

    private var sourceLanguageCode = TranslateLanguage.SPANISH
    private var sourceLanguageTitle = "Español"
    private var targetLanguageCode = TranslateLanguage.ENGLISH
    private var targetLanguageTitle = "Inglés"
    private lateinit var translatorOptions: TranslatorOptions
    private lateinit var translator: Translator
    private lateinit var progressDialog: ProgressDialog
    private var sourceLanguageText = ""
    private lateinit var btnSwitchLanguage: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnDownloadLanguage: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grabar)

        initUI()
        setupListeners()

        languageManager = LanguageManager(this)
        loadLanguages()
        updateLanguageMenus()
    }

    private fun initUI() {
        bSpeak = findViewById(R.id.hablar)
        btnTranslate = findViewById(R.id.btnTraducir)
        btnEnterLanguage = findViewById(R.id.btnEntradaTexto)
        btnOutLanguage = findViewById(R.id.btnSalidaTexto)
        edText = findViewById(R.id.textoEntrada)
        btnSwitchLanguage = findViewById(R.id.btnSwitchLanguages)
        btnDownloadLanguage = findViewById(R.id.btnDownloadLanguage)
        progressBar = findViewById(R.id.progressBar)
        progressBar.max = 100

        progressDialog = ProgressDialog(this).apply {
            setTitle("Please wait...")
            setCancelable(false)
        }
    }

    private fun setupListeners() {
        bSpeak.setOnClickListener { promptSpeechInput() }
        btnTranslate.setOnClickListener { validateAndTranslateText() }
        btnEnterLanguage.setOnClickListener { showInputLanguageMenu() }
        btnOutLanguage.setOnClickListener { showOutputLanguageMenu() }
        btnSwitchLanguage.setOnClickListener { switchLanguages() }
        btnDownloadLanguage.setOnClickListener { showLanguageDownloadDialog() }
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

    private fun promptSpeechInput() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
            }
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        } else {
            Toast.makeText(this, "Speech recognition is not available on this device.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateAndTranslateText() {
        val text = edText.text.toString().trim()
        if (text.isEmpty()) {
            Toast.makeText(this, "Please enter text to translate.", Toast.LENGTH_SHORT).show()
        } else {
            translateText(text)
        }
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

    private fun switchLanguages() {
        val tempCode = sourceLanguageCode
        val tempTitle = sourceLanguageTitle
        sourceLanguageCode = targetLanguageCode
        sourceLanguageTitle = targetLanguageTitle
        targetLanguageCode = tempCode
        targetLanguageTitle = tempTitle

        btnEnterLanguage.text = sourceLanguageTitle
        btnOutLanguage.text = targetLanguageTitle
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

    private fun translateText(text: String) {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguageCode)
            .setTargetLanguage(targetLanguageCode)
            .build()
        translator = Translation.getClient(options)

        translator.downloadModelIfNeeded(DownloadConditions.Builder().requireWifi().build())
            .addOnSuccessListener { translator.translate(text).addOnSuccessListener { translatedText -> edText.text = translatedText } }
            .addOnFailureListener { e -> Log.e(TAG, "Failed to download translation model", e) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = result?.get(0)?.trim() ?: ""
            if (recognizedText.isNotEmpty()) {
                edText.text = recognizedText
                Log.i(TAG, "Texto reconocido: $recognizedText")
            }
        }
    }
}