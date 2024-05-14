package com.sergi.micropen

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.Menu
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
    var RQ_SPEECH_REC = 102
    lateinit var bSpeak: ImageButton
    lateinit var btnTranslate: Button
    lateinit var btnEnterLanguage: Button
    lateinit var btnOutLanguage: Button
    lateinit var edText: TextView
    private lateinit var mainLanguageList: ArrayList<Idioma>
    private lateinit var additionalLanguageList: ArrayList<Idioma>
    private lateinit var languageManager: LanguageManager

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


        //Llamar a la clase
        languageManager = LanguageManager(this)
//Iniciar variables
        btnTranslate = findViewById(R.id.btnTraducir)
        btnEnterLanguage = findViewById(R.id.btnEntradaTexto)
        btnOutLanguage = findViewById(R.id.btnSalidaTexto)
        edText = findViewById(R.id.textoEntrada)
        bSpeak = findViewById(R.id.hablar)
        btnSwitchLanguage = findViewById(R.id.btnSwitchLanguages)
        btnDownloadLanguage = findViewById(R.id.btnDownloadLanguage)


        //LLamar métodos para cargar los lenguajes predeterminados y los adiccionales
        loadMainLanguages()
        loadAdditionalLanguages()


        // Descargar lenguajes
        languageManager.downloadAllLanguages()


        //Listeners
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
    }
//agrega los idiomas disponibles siempre
    private fun loadMainLanguages() {
        //Idiomas predeterminados que se van a descargar
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


    //Cargar lista de idiomas no predeterminados
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


    //Elección del idioma y cargarlo en los botones de idioma de entrada de idioma de salida
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
            languageManager.downloadLanguage(languageCode)

            // Agregar idioma descargado a las listas de selección de entrada y salida
            val downloadedLanguage = additionalLanguageList[position]
            mainLanguageList.add(downloadedLanguage)
            additionalLanguageList.removeAt(position)

            false
        }
    }
//Cambio de idioma de entrada a salida y viceversa
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
//Comenzar la traducción cargando el idioma de entrada y el idioma de salida elegido
    private fun startTranslation() {
        translatorOptions = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguageCode)
            .setTargetLanguage(targetLanguageCode)
            .build()

        translator = Translation.getClient(translatorOptions)
// descargar y que funcione tanto con Wifi como datos
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

    //Método de traducir para llamarlo
    private fun translateText(text: String) {
        translator.translate(text)
            .addOnSuccessListener { translatedText ->
                edText.text = translatedText
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error al traducir el texto: $exception")
            }
    }


    //Método para seleccionar el idioma de entrada para la traducción
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
//Método para seleccionar el idioma de salida para traduccir
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


    //Función para hablar
    private fun askSpeechInput() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "No reconoce la voz", Toast.LENGTH_LONG).show()
        } else {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Di algo")
            startActivityForResult(intent, RQ_SPEECH_REC)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RQ_SPEECH_REC && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (result != null && result.isNotEmpty()) {
                val recognizedText = result[0]
                Log.d("RecognizedSpeech", "Original: $recognizedText")
                val normalizedText = Normalizer.normalize(recognizedText, Normalizer.Form.NFD)
                val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
                val cleanText = pattern.matcher(normalizedText).replaceAll("")
                edText.text =  normalizedText  //para que tenga en cuenta las tildes y la ñ
            }
        }
    }
}