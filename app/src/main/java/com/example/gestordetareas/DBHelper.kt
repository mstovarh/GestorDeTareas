package com.example.gestordetareas

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val securityHelper = SecurityHelper()

    companion object {
        private const val DATABASE_NAME = "tasks.db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_TASKS = "tasks"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USER_EMAIL = "user_email"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_DESCRIPTION = "description"
    }

    // Crea la estructura inicial de la base de datos SQLite.
    // Esta función se ejecuta automáticamente la primera vez que se crea la base de datos.
    // La tabla almacena el correo del usuario, el nombre de la tarea y su descripción.
    // Los campos name y description guardarán información cifrada antes de insertarse.
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_TASKS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_EMAIL TEXT NOT NULL,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT NOT NULL
            )
        """.trimIndent()

        db.execSQL(createTable)
    }

    // Actualiza la estructura de la base de datos cuando cambia la versión.
    // En este caso, elimina la tabla anterior y la vuelve a crear.
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        onCreate(db)
    }

    // Inserta una tarea en SQLite.
    // Antes de guardar, cifra el nombre y la descripción para evitar almacenamiento en texto plano.
    fun insertTask(userEmail: String, name: String, description: String): Boolean {
        val db = writableDatabase

        val encryptedName = securityHelper.encryptText(name)
        val encryptedDescription = securityHelper.encryptText(description)

        val values = ContentValues().apply {
            put(COLUMN_USER_EMAIL, userEmail)
            put(COLUMN_NAME, encryptedName)
            put(COLUMN_DESCRIPTION, encryptedDescription)
        }

        val result = db.insert(TABLE_TASKS, null, values)
        db.close()

        return result != -1L
    }

    // Consulta únicamente las tareas asociadas al usuario autenticado.
    // Se usa el correo del usuario como filtro para evitar que un usuario vea tareas de otro.
    // La consulta utiliza parámetro seguro (?) para reducir el riesgo de SQL Injection.
    // Los datos recuperados desde SQLite están cifrados, por eso se descifran antes de mostrarlos.
    fun getTasksByUser(userEmail: String): List<Task> {
        val tasks = ArrayList<Task>()
        val db = readableDatabase

        val cursor = db.rawQuery(
            """
                SELECT $COLUMN_NAME, $COLUMN_DESCRIPTION 
                FROM $TABLE_TASKS 
                WHERE $COLUMN_USER_EMAIL = ?
                ORDER BY $COLUMN_ID DESC
            """.trimIndent(),
            arrayOf(userEmail)
        )

        if (cursor.moveToFirst()) {
            do {
                val encryptedName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val encryptedDescription = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))

                val name = try {
                    securityHelper.decryptText(encryptedName)
                } catch (e: Exception) {
                    "Dato protegido"
                }

                val description = try {
                    securityHelper.decryptText(encryptedDescription)
                } catch (e: Exception) {
                    "No se pudo descifrar la descripción"
                }

                tasks.add(Task(name, description))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return tasks
    }
}