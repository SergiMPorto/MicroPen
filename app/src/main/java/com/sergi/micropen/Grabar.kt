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
    private lateinit var languageManager : LanguageManager

    companion object {
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
    private lateinit var btnSwitchLanguage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grabar)

        btnTranslate = findViewById(R.id.btnTraducir)
        btnEnterLanguage = findViewById(R.id.btnEntradaTexto)
        btnOutLanguage = findViewById(R.id.btnSalidaTexto)
        edText = findViewById(R.id.textoEntrada)
        bSpeak = findViewById(R.id.hablar)
        btnSwitchLanguage = findViewById(R.id.btnSwitchLanguages)

        loadAvailableLanguages()

        //Descargar lenguajes

        languageManager = LanguageManager(this)
        languageManager.downloadAllLanguages()

        bSpeak.setOnClickListener {
            askSpeechInput()
        }
          //Traducción idioma de salida
        btnOutLanguage.setOnClickListener {
            Toast.makeText(this, "Elige el idioma de salida", Toast.LENGTH_LONG).show()
            targetLanguageChoose()
        }
          //Botón traducción
        btnTranslate.setOnClickListener {
            Toast.makeText(this, "Traducir", Toast.LENGTH_LONG).show()
            validateData()
        }
         //Traducción idioma de entrada
        btnEnterLanguage.setOnClickListener {
            Toast.makeText(this, "Elige el idioma de entrada", Toast.LENGTH_LONG).show()
            sourceLanguageChoose()
            progreDialog = ProgressDialog(this)
            progreDialog.setTitle("Por favor espere")
            progreDialog.setCanceledOnTouchOutside(false)
        }
             //Invertir opciones de traducción
        btnSwitchLanguage.setOnClickListener {
            switchLanguage()
        }

    }


    //Invertir opciones de traducción
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

     //Ver si  hay texto
    private fun validateData() {
        sourceLangugeText = edText.text.toString().trim()

        if (sourceLangugeText.isEmpty()) {
            Toast.makeText(this, "No hay texto", Toast.LENGTH_LONG).show()
        } else {
            startTranlation()
        }
    }
   //Empezar traducción
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
                Log.e(TAG, "Error al descargar el modelo de traducción: $exception")
            }
    }
   //Traducción
    private fun translateText(text: String) {
        translator.translate(text)
            .addOnSuccessListener { translatedText ->
                edText.text = translatedText
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error al traducir el texto: $exception")
            }
    }
     //Cargar los lenguajes
    private fun loadAvailableLanguages() {
        languageArrayList = ArrayList()
        val languageCodeList = TranslateLanguage.getAllLanguages()

        for (languageCode in languageCodeList) {
            val languageTitle = Locale(languageCode).displayLanguage
            val modelLanguage = Idioma(languageCode, languageTitle)
            languageArrayList.add(modelLanguage)
        }
    }
      //Botón de selección de lenguaje de entrada.
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
            false
        }
    }
      //Botón de salida de idioma
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
            false
        }
    }
      //Pasar audio a texto
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RQ_SPEECH_REC && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = result?.get(0).toString().trim()
            if (recognizedText.isNotEmpty()) {
                edText.text = recognizedText

            }
        }
    }
    //Activar el audio
    private fun askSpeechInput() {
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