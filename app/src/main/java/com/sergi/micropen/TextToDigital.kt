package com.sergi.micropen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.*

class TextToDigital : AppCompatActivity() {

    private lateinit var drawingView: DrawingView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_to_digital)

        drawingView = findViewById(R.id.DrawView)
        val recognizeButton: Button = findViewById(R.id.BtnRecognize)

        recognizeButton.setOnClickListener {
            recognizeText()
        }
    }
 // Descargar modelo para traduccir los trazos

    private fun downloadModel(modelIdentifier: DigitalInkRecognitionModelIdentifier) {
        val model = DigitalInkRecognitionModel.builder(modelIdentifier).build()
        val remoteModelManager = RemoteModelManager.getInstance()

        remoteModelManager.isModelDownloaded(model)
            .addOnSuccessListener { isDownloaded ->
                if (isDownloaded) {
                    Log.d("TextToDigital", "El modelo ya está descargado.")
                    performRecognition(model)
                } else {
                    val conditions = DownloadConditions.Builder()
                        .requireWifi()
                        .build()

                    remoteModelManager.download(model, conditions)
                        .addOnSuccessListener {
                            Log.d("TextToDigital", "Modelo descargado con éxito.")
                            performRecognition(model)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Error al descargar el modelo: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            Log.e("TextToDigital", "Error al descargar el modelo: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al verificar el modelo: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("TextToDigital", "Error al verificar el modelo: ${e.message}")
            }
    }
    //En caso de que el módelo no esté descargado gestiona la descarga.
    private fun performRecognition(model: DigitalInkRecognitionModel) {
        val inkBuilder = Ink.builder()
        for (stroke in drawingView.getStrokes()) {
            inkBuilder.addStroke(stroke)
        }
        val ink = inkBuilder.build()

        val recognizer = DigitalInkRecognition.getClient(DigitalInkRecognizerOptions.builder(model).build())
        recognizer.recognize(ink)
            .addOnSuccessListener { result ->
                val recognizedText = result.candidates.firstOrNull()?.text ?: "No texto reconocido"
                Log.d("TextToDigital", "Texto reconocido: $recognizedText")

                val intent = Intent(this, Escribir::class.java).apply {
                    putExtra("RECOGNIZED_TEXT", recognizedText)
                }
                startActivity(intent)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error de reconocimiento: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("TextToDigital", "Error de reconocimiento: ${e.message}")
            }
    }


    //Construcción de un objeto Ink a través de la API de Google
    private fun recognizeText() {
        val modelIdentifier: DigitalInkRecognitionModelIdentifier?
        try {
            //modelo de idioma español llama a download para verificar su descarga
            modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("es")
            Log.d("TextToDigital", "Modelo de identificación obtenido.")
        } catch (e: MlKitException) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("TextToDigital", "Error al obtener el modelo de identificación: ${e.message}")
            return
        }
        if (modelIdentifier == null) {
            Toast.makeText(this, "Error: modelo no encontrado", Toast.LENGTH_LONG).show()
            Log.e("TextToDigital", "Error: modelo no encontrado.")
            return
        }

        downloadModel(modelIdentifier)
    }
}
