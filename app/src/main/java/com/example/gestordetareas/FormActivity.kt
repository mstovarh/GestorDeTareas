package com.example.gestordetareas

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class FormActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        dbHelper = DBHelper(this)
        auth = FirebaseAuth.getInstance()

        val etTaskName = findViewById<EditText>(R.id.etTaskName)
        val etTaskDescription = findViewById<EditText>(R.id.etTaskDescription)
        val btnSaveTask = findViewById<Button>(R.id.btnSaveTask)
        val btnBack = findViewById<Button>(R.id.btnBack)

        btnSaveTask.setOnClickListener {
            val name = etTaskName.text.toString().trim()
            val description = etTaskDescription.text.toString().trim()
            val currentUserEmail = auth.currentUser?.email

            if (currentUserEmail == null) {
                Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
                finish()
                return@setOnClickListener
            }

            if (name.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Completa nombre y descripción", Toast.LENGTH_SHORT).show()
            } else {
                val saved = dbHelper.insertTask(currentUserEmail, name, description)

                if (saved) {
                    Toast.makeText(this, "Tarea guardada de forma segura", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "No se pudo guardar la tarea", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnBack.setOnClickListener {
            Toast.makeText(this, "Registro cancelado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}