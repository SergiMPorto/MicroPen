package com.sergi.micropen

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
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
    private val RQ_SPEECH_REC = 102
    private lateinit var bSpeak: ImageButton
    private lateinit var btnTranslate: Button
    private lateinit var btnEnterLanguage: Button
    private lateinit var btnOutLanguage: Button
    private lateinit var edText: TextView
    private lateinit var mainLanguageList: ArrayList<Idioma>
    private lateinit var additionalLanguageList: ArrayList<Idioma>
    private lateinit var languageManager: LanguageManager
    private val REQUEST_CODE_SPEECH_INPUT = 100

    companion object {
        private const val TAG = "MAIN_TAG"
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

        // Inicializar variables
        btnTranslate = findViewById(R.id.btnTraducir)
        btnEnterLanguage = findViewById(R.id.btnEntradaTexto)
        btnOutLanguage = findViewById(R.id.btnSalidaTexto)
        edText = findViewById(R.id.textoEntrada)
        bSpeak = findViewById(R.id.hablar)
        btnSwitchLanguage = findViewById(R.id.btnSwitchLanguages)
        btnDownloadLanguage = findViewById(R.id.btnDownloadLanguage)

        // Llamar métodos para cargar los lenguajes predeterminados y los adicionales
        loadMainLanguages()
        loadAdditionalLanguages()

        // Descargar lenguajes
        languageManager = LanguageManager(this)


        // Listeners
        bSpeak.setOnClickListener {
            askSpeechInput()
        }

        btnOutLanguage.setOnClickListener {
            Toast.makeText(this, "Elige el idioma de salida", Toast.LENGTH_LONG).show()
            targetLanguageChoose()
        }

        btnTranslate.setOnClickListener {
            Toast.makeText(this, "Traducir", Toast.LENGTH_LONG).show()
            validateData()
        }

        btnEnterLanguage.setOnClickListener {
            Toast.makeText(this, "Elige el idioma de entrada", Toast.LENGTH_LONG).show()
            sourceLanguageChoose()
            progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Por favor espere")
            progressDialog.setCanceledOnTouchOutside(false)
        }

        btnSwitchLanguage.setOnClickListener {
            switchLanguage()
        }

        btnDownloadLanguage.setOnClickListener {
            Toast.makeText(this, "Elige idioma para descargar", Toast.LENGTH_LONG).show()
            downloadLanguageChoose()
        }

        //Inicializar progressbar
        // Inicializar el ProgressBar
        progressBar = findViewById(R.id.progressBar)
        progressBar.max = 100
        progressBar.progress = 0

        languageManager = LanguageManager(this)
    }

    private fun loadMainLanguages() {
        // Idiomas predeterminados que se van a descargar
        mainLanguageList = arrayListOf(
            Idioma(TranslateLanguage.ARABIC, Locale(TranslateLanguage.ARABIC).displayLanguage),
            Idioma(TranslateLanguage.ENGLISH, Locale(TranslateLanguage.ENGLISH).displayLanguage),
            Idioma(TranslateLanguage.CHINESE, Locale(TranslateLanguage.CHINESE).displayLanguage),
            Idioma(TranslateLanguage.JAPANESE, Locale(TranslateLanguage.JAPANESE).displayLanguage),
            Idioma(TranslateLanguage.GERMAN, Locale(TranslateLanguage.GERMAN).displayLanguage),
            Idioma(TranslateLanguage.FRENCH, Locale(TranslateLanguage.FRENCH).displayLanguage),
            Idioma(TranslateLanguage.ITALIAN, Locale(TranslateLanguage.ITALIAN).displayLanguage),
            Idioma(TranslateLanguage.SPANISH, Locale(TranslateLanguage.SPANISH).displayLanguage),
            Idioma(TranslateLanguage.PORTUGUESE, Locale(TranslateLanguage.PORTUGUESE).displayLanguage),
            Idioma(TranslateLanguage.NORWEGIAN, Locale(TranslateLanguage.NORWEGIAN).displayLanguage),
            Idioma(TranslateLanguage.GALICIAN, Locale(TranslateLanguage.GALICIAN).displayLanguage),
            Idioma(TranslateLanguage.RUSSIAN, Locale(TranslateLanguage.RUSSIAN).displayLanguage),
            Idioma(TranslateLanguage.CATALAN, Locale(TranslateLanguage.CATALAN).displayLanguage),
            Idioma(TranslateLanguage.FINNISH, Locale(TranslateLanguage.FINNISH).displayLanguage),
            Idioma(TranslateLanguage.GREEK, Locale(TranslateLanguage.GREEK).displayLanguage),
            Idioma(TranslateLanguage.BULGARIAN, Locale(TranslateLanguage.BULGARIAN).displayLanguage),
            Idioma(TranslateLanguage.HUNGARIAN, Locale(TranslateLanguage.HUNGARIAN).displayLanguage),
            Idioma(TranslateLanguage.DUTCH, Locale(TranslateLanguage.DUTCH).displayLanguage),
            Idioma(TranslateLanguage.SWEDISH, Locale(TranslateLanguage.SWEDISH).displayLanguage),
            Idioma(TranslateLanguage.DANISH, Locale(TranslateLanguage.DANISH).displayLanguage),
            Idioma(TranslateLanguage.POLISH, Locale(TranslateLanguage.POLISH).displayLanguage),
            Idioma(TranslateLanguage.ROMANIAN, Locale(TranslateLanguage.ROMANIAN).displayLanguage)
        )
    }

    private fun loadAdditionalLanguages() {
        additionalLanguageList = ArrayList()
        val allLanguageCodeList = TranslateLanguage.getAllLanguages()
        val mainLanguageCodes = mainLanguageList.map { it.languageCode }

        for (languageCode in allLanguageCodeList) {
            if (!mainLanguageCodes.contains(languageCode)) {
                val languageTitle = Locale(languageCode).displayLanguage
                val modelLanguage = Idioma(languageCode, languageTitle)
                additionalLanguageList.add(modelLanguage)
            }
        }
    }

    private fun downloadAllLanguages() {
        progressBar.visibility = View.VISIBLE

        languageManager.downloadAllLanguages(
            { success, downloadedLanguages ->
                if (success) {
                    if (downloadedLanguages == languageManager.getAutoDownloadLanguages().size) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Todos los paquetes de idioma se han descargado", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Error al descargar los paquetes de idioma", Toast.LENGTH_SHORT).show()
                }
            },
            { progress ->
                progressBar.progress = progress
            }
        )
    }

    private fun downloadLanguageChoose() {
        val popupMenu = PopupMenu(this, btnDownloadLanguage)
        val autoDownloadLanguages = languageManager.getAutoDownloadLanguages()

        for (i in additionalLanguageList.indices) {
            if (!autoDownloadLanguages.contains(additionalLanguageList[i].languageCode)) {
                popupMenu.menu.add(Menu.NONE, i, i, additionalLanguageList[i].languageTitle)
            }
        }
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { menuItem ->
            val position = menuItem.itemId
            val languageCode = additionalLanguageList[position].languageCode

            progressBar.visibility = View.VISIBLE  // Mostrar el ProgressBar

            languageManager.downloadLanguage(languageCode) { success ->
                progressBar.visibility = View.GONE  // Ocultar el ProgressBar
                if (success) {
                    Toast.makeText(this, "Idioma descargado", Toast.LENGTH_SHORT).show()
                    // Agregar idioma descargado a las listas de selección de entrada y salida
                    val downloadedLanguage = additionalLanguageList[position]
                    mainLanguageList.add(downloadedLanguage)
                    additionalLanguageList.removeAt(position)
                } else {
                    Toast.makeText(this, "Error al descargar el idioma", Toast.LENGTH_SHORT).show()
                }
            }

            false
        }
    }

    private fun switchLanguage() {
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
        sourceLanguageText = edText.text.toString().trim()

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
                edText.text = translatedText
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error al traducir el texto: $exception")
            }
    }

    private fun sourceLanguageChoose() {
        val popupMenu = PopupMenu(this, btnEnterLanguage)

        for (i in mainLanguageList.indices) {
            popupMenu.menu.add(Menu.NONE, i, i, mainLanguageList[i].languageTitle)
        }
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { menuItem ->
            val position = menuItem.itemId
            sourceLanguageCode = mainLanguageList[position].languageCode
            sourceLanguageTitle = mainLanguageList[position].languageTitle
            btnEnterLanguage.text = sourceLanguageTitle
            false
        }
    }

    private fun targetLanguageChoose() {
        val popupMenu = PopupMenu(this, btnOutLanguage)

        for (i in mainLanguageList.indices) {
            popupMenu.menu.add(Menu.NONE, i, i, mainLanguageList[i].languageTitle)
        }

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { menuItem ->
            val position = menuItem.itemId
            targetLanguageCode = mainLanguageList[position].languageCode
            targetLanguageTitle = mainLanguageList[position].languageTitle
            btnOutLanguage.text = targetLanguageTitle
            false
        }
    }

    private fun askSpeechInput() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "No reconoce la voz", Toast.LENGTH_LONG).show()
        } else {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Di algo")
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = result?.get(0)?.trim() ?: ""
            if (recognizedText.isNotEmpty()) {
                edText.text = recognizedText  // Aquí se establece el texto en el TextView
                Log.i(TAG, "Texto reconocido: $recognizedText")
            }
        }
    }
}