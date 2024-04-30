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
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import com.sergi.micropen.idioma.Idioma
import java.util.Locale



class Grabar : AppCompatActivity() {



    var RQ_SPEECH_REC = 102
    lateinit var bSpeak: ImageButton
    lateinit var btnTranslate: Button
    lateinit var  btnEnterLanguage: Button
    lateinit var btnOutLanguage: Button
    lateinit var edText: TextView
    private lateinit var languageArrayList: ArrayList<Idioma>

    companion object {
        //for logs
        private const val TAG = "MAIN_TAG"
    }

    private var sourceLanguageCode = "es"
    private var sourceLanguageTitle = "Español"
    private var targetLanguageCode = "en"
    private var targetLanguageTitle = "Inglés"
    private lateinit var translatorOptions: TranslatorOptions
    private lateinit var translator: Translator
    private lateinit var progreDialog: ProgressDialog
    private var sourceLangugeText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grabar)

        btnTranslate = findViewById(R.id.btnTraducir)
        btnEnterLanguage = findViewById(R.id.btnEntradaTexto)
        btnOutLanguage = findViewById(R.id.btnSalidaTexto)
        edText = findViewById(R.id.textoEntrada)
        bSpeak = findViewById(R.id.hablar)

        loadAvailableLanguages()

        bSpeak.setOnClickListener {
            askSpeechInput()
        }

        btnOutLanguage.setOnClickListener {
            Toast.makeText(this, "Elige el idioma de salida", Toast.LENGTH_LONG).show()
            sourceLanguageChoose()
        }

        btnTranslate.setOnClickListener {
            Toast.makeText(this, "Traducir", Toast.LENGTH_LONG).show()
            validateData()
        }

        btnEnterLanguage.setOnClickListener {
            Toast.makeText(this, "Elige el idioma de entrada", Toast.LENGTH_LONG).show()
            targetLanguageChoose()
            progreDialog = ProgressDialog(this)
            progreDialog.setTitle("Por favor espere")
            progreDialog.setCanceledOnTouchOutside(false)
        }
    }

    private fun validateData() {
        sourceLangugeText = edText.text.toString().trim()

        if (sourceLangugeText.isEmpty()) {
            Toast.makeText(this, "No hay texto", Toast.LENGTH_LONG).show()
        } else {
            startTranlation()
        }
    }

    private fun startTranlation() {
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
                translateText(sourceLangugeText)
            }
            .addOnFailureListener { exception ->
                // Manejar la falla al descargar el modelo
                Log.e(TAG, "Error al descargar el modelo de traducción: $exception")
            }
    }

    private fun translateText(text: String) {
        translator.translate(text)
            .addOnSuccessListener { translatedText ->
                // Mostrar el texto traducido en el TextView
                edText.text = translatedText
            }
            .addOnFailureListener { exception ->
                // Manejar la falla en la traducción
                Log.e(TAG, "Error al traducir el texto: $exception")
            }
    }

    private fun loadAvailableLanguages() {
        languageArrayList = ArrayList()
        val languageCodeList = TranslateLanguage.getAllLanguages()

        for (languageCode in languageCodeList) {
            val languageTitle = Locale(languageCode).displayLanguage
            Log.d(TAG, "CargandoLenguajes : LanguageCode: $languageCode")
            Log.d(TAG, "loadAvailableLanguages: languageTitle: $languageTitle")
            val modelLanguage = Idioma(languageCode, languageTitle)
            languageArrayList.add(modelLanguage)
        }
    }

    private fun sourceLanguageChoose() {
        val popupMenu = PopupMenu(this, btnEnterLanguage)

        for (i in languageArrayList.indices) {
            popupMenu.menu.add(Menu.NONE, i, i, languageArrayList[i].languageTitle)
        }
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { menuItem ->
            val position = menuItem.itemId
            sourceLanguageCode = languageArrayList[position].languageCode
            sourceLanguageTitle = languageArrayList[position].languageTitle
            btnEnterLanguage.text = sourceLanguageTitle
            Log.d(TAG, "sourceLanguageChoose : sourceLanguageCode: $sourceLanguageCode")
            Log.d(TAG, "sourceLanguageChoose : sourceLanguageTitle: $sourceLanguageTitle")
            false
        }
    }

    private fun targetLanguageChoose() {
        val popupMenu = PopupMenu(this, btnOutLanguage)

        for (i in languageArrayList.indices) {
            popupMenu.menu.add(Menu.NONE, i, i, languageArrayList[i].languageTitle)
        }

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { menuItem ->
            val position = menuItem.itemId
            targetLanguageCode = languageArrayList[position].languageCode
            targetLanguageTitle = languageArrayList[position].languageTitle
            btnOutLanguage.text = targetLanguageTitle
            Log.d(TAG, "targetLanguageChoose : targetLanguageCode: $targetLanguageCode")
            Log.d(TAG, "targetLanguageChoose : targetLanguageTitle: $targetLanguageTitle")
            false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RQ_SPEECH_REC && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = result?.get(0).toString().trim()
            if (recognizedText.isNotEmpty()) {
                edText.text = recognizedText
                startTranlation()
            }
        }
    }

    fun askSpeechInput() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "No reconoce la voz", Toast.LENGTH_LONG).show()
        } else {
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Di algo")
            startActivityForResult(i, RQ_SPEECH_REC)
        }
    }
}