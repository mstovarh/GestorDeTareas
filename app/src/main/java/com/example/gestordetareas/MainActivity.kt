package com.example.gestordetareas

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbHelper: DBHelper
    private lateinit var listTasks: ListView
    private lateinit var tvEmptyTasks: TextView
    private val tasksList = ArrayList<String>()

    // Inicializa la pantalla principal del gestor de tareas.
    // Valida que exista un usuario autenticado antes de permitir el acceso,
    // configura los botones de navegación y prepara la lista donde se muestran las tareas.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Control de acceso: si no hay usuario autenticado,
        // la app redirige al login y bloquea el acceso al gestor.
        if (auth.currentUser == null) {
            goToLogin()
            return
        }

        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(this)

        val btnAddTask = findViewById<Button>(R.id.btnAddTask)
        val btnMap = findViewById<Button>(R.id.btnMap)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        listTasks = findViewById(R.id.listTasks)
        tvEmptyTasks = findViewById(R.id.tvEmptyTasks)

        btnAddTask.setOnClickListener {
            val intent = Intent(this, FormActivity::class.java)
            startActivity(intent)
        }

        btnMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
            goToLogin()
        }
    }

    // Actualiza la información de la pantalla cada vez que el usuario vuelve al gestor.
    // Esto permite recargar la lista de tareas después de crear una nueva tarea
    // o después de regresar desde otra pantalla de la aplicación.
    override fun onResume() {
        super.onResume()

        if (::dbHelper.isInitialized) {
            loadTasks()
        }
    }

    // Carga desde SQLite únicamente las tareas asociadas al usuario autenticado.
    // Si no hay sesión activa, bloquea el acceso y redirige al login.
    // Cuando no existen tareas, muestra un mensaje de estado vacío para evitar una pantalla en blanco.
    private fun loadTasks() {
        tasksList.clear()

        val currentUserEmail = auth.currentUser?.email

        if (currentUserEmail == null) {
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show()
            goToLogin()
            return
        }

        val tasks = dbHelper.getTasksByUser(currentUserEmail)

        for (task in tasks) {
            tasksList.add("${task.name}\n${task.description}")
        }

        val adapter = ArrayAdapter(
            this,
            R.layout.task_item,
            android.R.id.text1,
            tasksList
        )

        listTasks.adapter = adapter

        if (tasksList.isEmpty()) {
            tvEmptyTasks.visibility = View.VISIBLE
            listTasks.visibility = View.GONE
        } else {
            tvEmptyTasks.visibility = View.GONE
            listTasks.visibility = View.VISIBLE
        }
    }

    // Redirige al usuario a la pantalla de inicio de sesión.
    // Se utiliza cuando no hay sesión activa o cuando el usuario cierra sesión.
    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}