/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/ui/auth/LoginActivity.kt
 * Proposito: Pantallas y ViewModel de autenticacion (login/registro).
 */
package com.example.gymtrackpro.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.gymtrackpro.databinding.ActivityLoginBinding
import com.example.gymtrackpro.ui.home.HomeActivity
import com.example.gymtrackpro.utils.AppProvider
import com.example.gymtrackpro.utils.ViewModelFactory

class LoginActivity : AppCompatActivity() {

    // [Req C/UI] ViewBinding para acceso seguro a componentes de login.
    private lateinit var b: ActivityLoginBinding

    private val vm: AuthViewModel by viewModels {
        ViewModelFactory(AppProvider.provideRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        // [Req C - MVVM] Si existe sesion local, evita login redundante.
        vm.checkSession()

        // Botón para entrar sin registro (Modo Invitado)
        b.btnGuest.setOnClickListener {
            // [Req C] Acceso en modo invitado (flujo sin sesion).
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        b.btnLogin.setOnClickListener {
            // [Req B] Envia credenciales al backend.
            vm.login(
                email = b.etEmail.text.toString().trim(),
                password = b.etPassword.text.toString()
            )
        }

        b.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        vm.loading.observe(this) { isLoading ->
            // [Req B] Indicador de carga durante llamada de red.
            b.progress.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        vm.error.observe(this) { msg ->
            // [Req B] Mensaje de error de autenticacion.
            b.tvError.text = msg ?: ""
        }

        vm.loggedIn.observe(this) { ok ->
            if (ok) {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
        }
    }
}

