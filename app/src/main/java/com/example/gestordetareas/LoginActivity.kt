package com.example.gestordetareas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // Inicializa la pantalla de inicio de sesión.
    // Configura Firebase Authentication, valida si ya existe una sesión activa
    // y prepara los eventos para iniciar sesión o ir al registro de usuario.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            goToMain()
            return
        }

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGoRegister = findViewById<Button>(R.id.btnGoRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa correo y contraseña", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(email, password)
            }
        }

        btnGoRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    // Realiza la autenticación del usuario mediante Firebase Authentication.
    // Si las credenciales son correctas permite el acceso al gestor;
    // si fallan, muestra un mensaje de error y bloquea el ingreso.
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    goToMain()
                } else {
                    Toast.makeText(
                        this,
                        "No se pudo iniciar sesión. Verifica tus datos",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    // Redirige al usuario autenticado hacia la pantalla principal del gestor.
    // Limpia la pila de pantallas para evitar que el usuario regrese al login
    // usando el botón atrás después de iniciar sesión correctamente.
    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}