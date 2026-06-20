package com.example.gestordetareas

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbHelper: DBHelper
    private lateinit var listTasks: ListView
    private val tasksList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

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

    override fun onResume() {
        super.onResume()

        if (::dbHelper.isInitialized) {
            loadTasks()
        }
    }

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
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}