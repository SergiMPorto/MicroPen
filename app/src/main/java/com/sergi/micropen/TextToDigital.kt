package com.sergi.micropen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.*

class TextToDigital : AppCompatActivity() {

    private lateinit var drawingView: DrawingView
    private lateinit var btndelete: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var progressText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_to_digital)

        drawingView = findViewById(R.id.DrawView)
        val recognizeButton: Button = findViewById(R.id.BtnRecognize)
        progressBar = findViewById(R.id.progressBar)
        progressText = findViewById(R.id.progressText)

        recognizeButton.setOnClickListener {
            recognizeText()
        }

        btndelete = findViewById(R.id.btnBorrar)
        btndelete.setOnClickListener {
          drawingView.deleteTrazos()
        }

    }


    // Descargar modelo para traduccir los trazos

    private fun downloadModel(modelIdentifier: DigitalInkRecognitionModelIdentifier) {
        val model = DigitalInkRecognitionModel.builder(modelIdentifier).build()
        val remoteModelManager = RemoteModelManager.getInstance()

        progressBar.visibility = View.VISIBLE
        progressText.visibility = View.VISIBLE
        progressBar.progress = 0
        progressText.text = "0%"

        remoteModelManager.isModelDownloaded(model)
            .addOnSuccessListener { isDownloaded ->
                if (isDownloaded) {
                    Log.d("TextToDigital", "El modelo ya está descargado.")
                    progressBar.visibility = View.GONE
                    progressText.visibility = View.GONE
                    performRecognition(model)
                } else {
                    val conditions = DownloadConditions.Builder()
                        .requireWifi()
                        .build()

                    // Simulate progress for the demo
                    Thread(Runnable {
                        for (i in 1..100 step 10) {
                            runOnUiThread {
                                progressBar.progress = i
                                progressText.text = "$i%"
                            }
                            Thread.sleep(500)
                        }
                    }).start()

                    remoteModelManager.download(model, conditions)
                        .addOnSuccessListener {
                            Log.d("TextToDigital", "Modelo descargado con éxito.")
                            progressBar.visibility = View.GONE
                            progressText.visibility = View.GONE
                            performRecognition(model)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Error al descargar el modelo: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            Log.e("TextToDigital", "Error al descargar el modelo: ${e.message}")
                            progressBar.visibility = View.GONE
                            progressText.visibility = View.GONE
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
                progressBar.visibility = View.GONE
                progressText.visibility = View.GONE
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
