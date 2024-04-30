package com.sergi.micropen

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.sergi.micropen.offline.LoginViewModel


class ºLogin : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var facebookLogin : FacebookAuthProvider
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        firebaseAuth = FirebaseAuth.getInstance()




        val btnLogin: ImageView = findViewById(R.id.login)
        val btnRegistro: TextView = findViewById(R.id.botonRegistro)
        val recuperar: TextView= findViewById(R.id.recuperar)

        btnLogin.setOnClickListener {
            signIn(
                findViewById<EditText>(R.id.email).text.toString(),
                findViewById<EditText>(R.id.password).text.toString()
            )
        }

        btnRegistro.setOnClickListener {
            create(
                findViewById<EditText>(R.id.email).text.toString(),
                findViewById<EditText>(R.id.password).text.toString()
            )
        }

        //Recuperar
        recuperar.setOnClickListener {
            val intent = Intent(this, Recuperar::class.java)
            startActivity(intent)
        }

        // Configuración de Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        val signInButton = findViewById<ImageView>(R.id.loginGoogle)
        signInButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signIn(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Introduzca contraseña y correo electrónico", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.signIn(email, password)


        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Home::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Email o Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                }
            }
    }
     //Registrase
    private fun create(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Introduzca contraseña y correo electrónico", Toast.LENGTH_SHORT).show()
            return
        }
         viewModel.createAccount(email, password)

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    Toast.makeText(this, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Home::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Error al crear la cuenta", Toast.LENGTH_SHORT).show()
                }
            }
    }
     //Conectarse con google
    private fun signInWithGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    Toast.makeText(this, "Inicio de sesión con Google exitoso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Home::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Handle Google Sign In failure
                    Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account?.idToken!!)
            } catch (e: ApiException) {
                // Handle Google Sign In failure
                Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}