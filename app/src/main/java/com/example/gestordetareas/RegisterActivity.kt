package com.example.gestordetareas

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // Inicializa la pantalla de registro de usuario.
    // Configura Firebase Authentication, conecta los campos del formulario
    // y valida que el correo y la contraseña cumplan las condiciones mínimas antes de registrar.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val etRegisterEmail = findViewById<EditText>(R.id.etRegisterEmail)
        val etRegisterPassword = findViewById<EditText>(R.id.etRegisterPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBackLogin = findViewById<Button>(R.id.btnBackLogin)

        btnRegister.setOnClickListener {
            val email = etRegisterEmail.text.toString().trim()
            val password = etRegisterPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(this, "La contraseña debe tener mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(email, password)
            }
        }

        btnBackLogin.setOnClickListener {
            Toast.makeText(this, "Volviendo al inicio de sesión", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // Registra un nuevo usuario mediante Firebase Authentication.
    // Si el registro es exitoso, muestra un mensaje de confirmación y regresa al login.
    // Si ocurre un error, informa al usuario que no fue posible crear la cuenta.
    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "No se pudo registrar la cuenta",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}