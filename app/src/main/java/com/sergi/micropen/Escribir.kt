package com.sergi.micropen

import android.app.ProgressDialog
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.EditText
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

    //variables translater
    private lateinit var btnTranslate: Button
    private lateinit var btnEnterLanguage: Button
    private lateinit var btnOutLanguage: Button
    private lateinit var languageArrayList: ArrayList<Idioma>
    private var sourceLanguageCode = "es"
    private var sourceLanguageTitle = "Español"
    private var targetLanguageCode = "en"
    private var targetLanguageTitle = "Inglés"
    private lateinit var translatorOptions: TranslatorOptions
    private lateinit var translator: Translator
    private lateinit var progreDialog: ProgressDialog
    private var sourceLangugeText = ""

    companion object {
        private const val TAG = "MAIN_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_escribir)


        //Logícal text---Audio

        textEscrito = findViewById(R.id.TextoEscrito)
        btnWrite = findViewById(R.id.button)

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

        //Logical translater

        //Start variables
        btnTranslate = findViewById(R.id.btnTraducirText)
        btnEnterLanguage = findViewById(R.id.btnEnterTextText)
        btnOutLanguage = findViewById(R.id.btnOutTextText)

        loadAvailableLanguages()

        //Listerners
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
            progreDialog = ProgressDialog(this)
            progreDialog.setTitle("Por favor espere")
            progreDialog.setCanceledOnTouchOutside(false)
        }
    }

    private fun validateData() {
        sourceLangugeText = textEscrito.text.toString().trim()

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
                Log.e(TAG, "Error al descargar el modelo de traducción: $exception")
            }
    }

    private fun translateText(text: String) {
        translator.translate(text)
            .addOnSuccessListener { translatedText: String? ->
                textEscrito.text = Editable.Factory.getInstance().newEditable(translatedText)


            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error al traducir el texto: $exception")
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
            false
        }
    }

    private fun loadAvailableLanguages() {
        languageArrayList = ArrayList()
        val languageCodeList = TranslateLanguage.getAllLanguages()

        for (languageCode in languageCodeList) {
            val languageTitle = Locale(languageCode).displayLanguage
            val modelLanguage = Idioma(languageCode, languageTitle)
            languageArrayList.add(modelLanguage)
        }
    }
}
