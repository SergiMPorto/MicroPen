package com.sergi.micropen

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.Date

class Perfil : AppCompatActivity() {

    private lateinit var enviar: Button
    private val database = FirebaseFirestore.getInstance()
    private lateinit var name : EditText
    private lateinit var fechaNacimiento : EditText
    private lateinit var email: EditText



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        name=findViewById(R.id.name)
        fechaNacimiento=findViewById(R.id.editTextDate)
        email=findViewById(R.id.editTextTextEmailAddress)

        enviar=findViewById(R.id.enviar)

        enviar.setOnClickListener {

            database.collection("user").document().set(
                hashMapOf(
                    "name" to name.text.toString(),
                     "fechanacimeinto" to fechaNacimiento.text.toString(),
                    "email" to email.text.toString()
                )
            )

            Toast.makeText(this, "Enviado", Toast.LENGTH_SHORT).show()
        }

    }
}