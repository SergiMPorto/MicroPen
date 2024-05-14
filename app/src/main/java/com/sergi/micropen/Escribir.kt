package com.sergi.micropen

import android.app.ProgressDialog
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
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
    private lateinit var btnTranslate: Button  //  Botón para traducir
    private lateinit var btnEnterLanguage: Button //Boton de entrada del idioma elegido
    private lateinit var btnOutLanguage: Button  //Botón de idioma de salida elegido
    private lateinit var mainLanguageList: ArrayList<Idioma>  //Idiomas principales
    private lateinit var additionalLanguageList: ArrayList<Idioma> //idiomas a seleccionar
    private var sourceLanguageCode = TranslateLanguage.SPANISH   // Código del idioma de entrada
    private var sourceLanguageTitle = "Español"  // Título del idioma de entrada
    private var targetLanguageCode = TranslateLanguage.ENGLISH   // Código del idioma de salida
    private var targetLanguageTitle = "Inglés" // Título del idioma de salida
    private lateinit var translatorOptions: TranslatorOptions
    private lateinit var translator: Translator
    private lateinit var progressDialog: ProgressDialog
    private var sourceLanguageText = ""
    private lateinit var btnSwitchLanguagesWriter: ImageView
    private lateinit var languageManager: LanguageManager
    private lateinit var btnDownloadAdditionalLanguage: Button //Añadir idiomas

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


        // Inicialización del administrador de idiomas y descarga de idiomas automáticos
        languageManager = LanguageManager(this)
        languageManager.downloadAllLanguages()

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
        loadMainLanguages()  //idiomas que se descargan de forma predeterminada. Los europeos y los otros
        loadAdditionalLanguages()

        // Listeners para la funcionalidad de traducción
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

        // Cambio de idiomas de entrada a salida o de salida a entrada
        btnSwitchLanguagesWriter.setOnClickListener {
            switchLanguages()
        }

        // Descargar idiomas adicionales
        btnDownloadAdditionalLanguage.setOnClickListener {
            Toast.makeText(this, "Elige el idioma que quieres descargar", Toast.LENGTH_LONG).show()
            downloadLanguageChoose()
        }
    }

    // Seleccionar idioma adicional para descargar
    private fun downloadLanguageChoose() {
        val popupMenu = PopupMenu(this, btnDownloadAdditionalLanguage)
        val autoDownloadLanguages = languageManager.getAutoDownloadLanguages() //Obtener la lista de los idiomas predeterminados

        for (i in additionalLanguageList.indices) {
            if (!autoDownloadLanguages.contains(additionalLanguageList[i].languageCode))  {
                popupMenu.menu.add(Menu.NONE, i, i, additionalLanguageList[i].languageTitle)
            }
        }

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { menuItem ->
            val position = menuItem.itemId
            val languageCode = additionalLanguageList[position].languageCode
            languageManager.downloadLanguage(languageCode)

            // añadir el idioma a la lista principal y borro el idioma de la lista selección
            val downloadedLanguage = additionalLanguageList[position]
            mainLanguageList.add(downloadedLanguage)
            additionalLanguageList.removeAt(position)

            false
        }
    }

    // Método para cambiar los idiomas de entrada y salida
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
//Probar si hay texto
    private fun validateData() {
        sourceLanguageText = textEscrito.text.toString().trim()

        if (sourceLanguageText.isEmpty()) {
            Toast.makeText(this, "No hay texto", Toast.LENGTH_LONG).show()
        } else {
            startTranslation()
        }
    }
  //Empezar la traducción
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
  //Método para accionar la traducción.
    private fun translateText(text: String) {
        translator.translate(text)
            .addOnSuccessListener { translatedText ->
                textEscrito.text = Editable.Factory.getInstance().newEditable(translatedText)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error al traducir el texto: $exception")
            }
    }

    // Elegir idioma de entrada
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

    // Elegir idioma de salida
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

    // Cargar los idiomas predeterminados
    private fun loadMainLanguages() {
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


    //Cargar los idiomas addionales
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
}