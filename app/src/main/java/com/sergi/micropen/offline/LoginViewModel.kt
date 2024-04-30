package com.sergi.micropen.offline

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository(application)

    fun signIn(email: String, password: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Realizar la autenticación con Firebase
                // Aquí puedes usar FirebaseAuth para iniciar sesión
                // Luego, sincronizar los datos con Firebase Realtime Database si es necesario
                userRepository.signInWithEmailAndPassword(email, password)
            } catch (e: Exception) {
                // Manejar errores
            }
        }
    }

    fun createAccount(email: String, password: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Crear cuenta con Firebase
                // Aquí puedes usar FirebaseAuth para crear una nueva cuenta
                // Luego, sincronizar los datos con Firebase Realtime Database si es necesario
                userRepository.createAccountWithEmailAndPassword(email, password)
            } catch (e: Exception) {
                // Manejar errores
            }
        }
    }
}