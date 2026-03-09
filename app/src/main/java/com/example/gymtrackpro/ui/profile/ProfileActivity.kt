/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/ui/profile/ProfileActivity.kt
 * Proposito: Gestion de perfil y sesion del usuario.
 */
package com.example.gymtrackpro.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.gymtrackpro.R
import com.example.gymtrackpro.databinding.ActivityProfileBinding
import com.example.gymtrackpro.ui.auth.LoginActivity
import com.example.gymtrackpro.ui.navigation.MainBottomNav
import com.example.gymtrackpro.utils.AppProvider
import com.example.gymtrackpro.utils.ViewModelFactory

class ProfileActivity : AppCompatActivity() {

    // [Req C/UI] ViewBinding de perfil.
    private lateinit var b: ActivityProfileBinding
    private val vm: ProfileViewModel by viewModels {
        ViewModelFactory(AppProvider.provideRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(b.root)

        MainBottomNav.bind(this, b.bottomNav, R.id.nav_profile)

        b.btnEditProfile.setOnClickListener {
            // Abre formulario de edicion precargando datos actuales.
            val name = vm.profile.value?.name ?: vm.session.value?.name.orEmpty()
            val email = vm.profile.value?.email ?: vm.session.value?.email.orEmpty()
            openEditDialog(name, email)
        }

        b.btnLogout.setOnClickListener {
            vm.logout()
        }

        vm.session.observe(this) { session ->
            // Modo invitado: perfil solo lectura y sin edicion.
            if (session == null) {
                b.tvName.text = "Invitado"
                b.tvEmail.text = "Sin sesion"
                b.tvMemberSince.text = "Miembro desde: N/D"
                b.btnEditProfile.visibility = View.GONE
            } else {
                b.btnEditProfile.visibility = View.VISIBLE
                if (vm.profile.value == null) {
                    b.tvName.text = session.name
                    b.tvEmail.text = session.email
                }
            }
        }

        vm.profile.observe(this) { p ->
            // Actualiza encabezado con datos remotos de perfil.
            if (p != null) {
                b.tvName.text = p.name
                b.tvEmail.text = p.email
                val created = p.createdAt?.take(10) ?: "N/D"
                b.tvMemberSince.text = "Miembro desde: $created"
            }
        }

        vm.loading.observe(this) { b.progress.visibility = if (it) View.VISIBLE else View.GONE }
        // [Req B] Estado de error visible en UI.
        vm.error.observe(this) { b.tvError.text = it ?: "" }
        vm.message.observe(this) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                vm.clearMessage()
            }
        }
        vm.loggedOut.observe(this) { out ->
            // Cierra stack para evitar volver con boton atras a sesiones previas.
            if (out) {
                startActivity(
                    Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )
                finish()
            }
        }

        vm.loadProfile()
    }

    override fun onResume() {
        super.onResume()
        MainBottomNav.syncSelection(b.bottomNav, R.id.nav_profile)
    }

    private fun openEditDialog(currentName: String, currentEmail: String) {
        // Formulario simple de actualizacion de perfil.
        val view = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etName = view.findViewById<EditText>(R.id.etProfileName)
        val etEmail = view.findViewById<EditText>(R.id.etProfileEmail)
        etName.setText(currentName)
        etEmail.setText(currentEmail)

        AlertDialog.Builder(this)
            .setTitle("Editar perfil")
            .setView(view)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar") { _, _ ->
                vm.updateProfile(
                    name = etName.text.toString(),
                    email = etEmail.text.toString()
                )
            }
            .show()
    }
}

