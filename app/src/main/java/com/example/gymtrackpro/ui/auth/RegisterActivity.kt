package com.example.gymtrackpro.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.gymtrackpro.databinding.ActivityRegisterBinding
import com.example.gymtrackpro.ui.home.HomeActivity
import com.example.gymtrackpro.utils.AppProvider
import com.example.gymtrackpro.utils.ViewModelFactory

class RegisterActivity : AppCompatActivity() {

    private lateinit var b: ActivityRegisterBinding

    private val vm: AuthViewModel by viewModels {
        ViewModelFactory(AppProvider.provideRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnCreateAccount.setOnClickListener {
            vm.register(
                name = b.etName.text.toString().trim(),
                email = b.etEmail.text.toString().trim(),
                password = b.etPassword.text.toString()
            )
        }

        b.btnBackToLogin.setOnClickListener {
            finish()
        }

        vm.loading.observe(this) { isLoading ->
            b.progress.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        vm.error.observe(this) { msg ->
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
