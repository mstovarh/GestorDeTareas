package com.example.gestordetareas

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecurityHelper() {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "GestorDeTareasSecureKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
        private const val IV_SIZE = 12
    }

    // Se ejecuta al crear una instancia de SecurityHelper.
    // Garantiza que exista una llave segura en Android Keystore
    // antes de realizar operaciones de cifrado o descifrado.
    init {
        createSecretKeyIfNeeded()
    }

    // Crea la llave secreta de cifrado dentro de Android Keystore si aún no existe.
    // Esta llave se usa para cifrar y descifrar los datos sensibles de la aplicación
    // sin almacenarla directamente en el código fuente.
    private fun createSecretKeyIfNeeded() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    // Obtiene la llave secreta almacenada en Android Keystore.
    // Esta llave es necesaria para ejecutar los procesos de cifrado y descifrado
    // de las tareas protegidas en la base de datos local.
    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        val secretKeyEntry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
        return secretKeyEntry.secretKey
    }

    // Cifra un texto usando AES/GCM y lo convierte a Base64.
    // Este método protege los datos sensibles antes de guardarlos en SQLite,
    // evitando que se almacenen como texto plano.
    fun encryptText(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        val combinedData = iv + encryptedBytes

        return Base64.encodeToString(combinedData, Base64.NO_WRAP)
    }

    // Descifra un texto previamente cifrado con AES/GCM.
    // Este método permite recuperar la información original para mostrarla
    // únicamente dentro de la aplicación al usuario autenticado.
    fun decryptText(encryptedText: String): String {
        val combinedData = Base64.decode(encryptedText, Base64.NO_WRAP)

        val iv = combinedData.copyOfRange(0, IV_SIZE)
        val encryptedBytes = combinedData.copyOfRange(IV_SIZE, combinedData.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)

        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

        val decryptedBytes = cipher.doFinal(encryptedBytes)

        return String(decryptedBytes, Charsets.UTF_8)
    }
}