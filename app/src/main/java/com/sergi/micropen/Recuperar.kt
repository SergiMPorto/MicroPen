package com.sergi.micropen

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Recuperar : AppCompatActivity() {
    private lateinit var restablecer : ImageButton
    private lateinit var email : EditText
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar)

        restablecer = findViewById(R.id.restablecer)
        email = findViewById(R.id.emailR)

        firebaseAuth = Firebase.auth

        restablecer.setOnClickListener {
            enviarPassword()
        }


    }

    private fun enviarPassword()
    {
        val userEmail = email.text.toString()
        firebaseAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener {
                task ->
            if(task.isSuccessful){
                Toast.makeText(baseContext, "Correo de Cambio de Contraseña Envidado", Toast.LENGTH_LONG).show()
            }
            else{
                Toast.makeText(baseContext,"Error al recuperar la contraseña", Toast.LENGTH_LONG).show()
            }
        }

    }





}