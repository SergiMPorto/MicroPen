package com.sergi.micropen


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.sergi.micropen.offline.LoginViewModel


class Login : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var facebookLogin : FacebookAuthProvider
    private lateinit var viewModel: LoginViewModel
    private lateinit var facebook : LoginButton
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        firebaseAuth = FirebaseAuth.getInstance()

        //facebook
        callbackManager = CallbackManager.Factory.create()
        facebook = findViewById(R.id.login_buttonFacebook)

        // Inicializar el SDK de Facebook
        FacebookSdk.sdkInitialize(applicationContext)

        val btnLogin: ImageView = findViewById(R.id.login)
        val btnRegistro: Button = findViewById(R.id.botonRegistro)
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

        //facebook
        facebook.setOnClickListener {
            signInWithFacebook()
        }
    }

    //facebook
    private fun signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Toast.makeText(this@Login, "Inicio de sesión con Facebook cancelado", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException) {
                Toast.makeText(this@Login, "Error al iniciar sesión con Facebook: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun handleSuccessfulSignIn() {
        // No se necesita descargar los modelos de idioma aquí
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
                    Toast.makeText(this,"Descargando idiomas", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, Home::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Email o Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                }
            }
    }

    //Registrarse
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

    //Facebook
    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        Log.d("FacebookToken", token.token) // Imprimir el token en la consola
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    Toast.makeText(this, "Inicio de sesión con Facebook exitoso", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, Home::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(baseContext, "Error al iniciar sesión con Facebook", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }

}