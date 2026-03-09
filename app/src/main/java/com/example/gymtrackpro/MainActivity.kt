/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/MainActivity.kt
 * Proposito: Punto de entrada de la app y redireccion inicial.
 */
package com.example.gymtrackpro

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gymtrackpro.ui.auth.LoginActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // [Req C] Punto de entrada limpio: delega flujo de auth a LoginActivity.
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}

