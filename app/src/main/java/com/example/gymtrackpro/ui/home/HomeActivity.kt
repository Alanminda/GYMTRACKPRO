package com.example.gymtrackpro.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.gymtrackpro.R
import com.example.gymtrackpro.databinding.ActivityHomeBinding
import com.example.gymtrackpro.ui.adapters.RoutineAdapter
import com.example.gymtrackpro.ui.exercises.ExercisesActivity
import com.example.gymtrackpro.utils.AppProvider
import com.example.gymtrackpro.utils.ViewModelFactory

class HomeActivity : AppCompatActivity() {

    private lateinit var b: ActivityHomeBinding

    private val vm: HomeViewModel by viewModels {
        ViewModelFactory(AppProvider.provideRepository(this))
    }

    private val adapter = RoutineAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.rvExercises.adapter = adapter

        b.btnOpenExercises.setOnClickListener {
            startActivity(Intent(this, ExercisesActivity::class.java))
        }

        b.btnAddRoutine.setOnClickListener {
            openCreateRoutineDialog()
        }

        vm.routines.observe(this) { routines ->
            adapter.submit(routines)
            b.tvEmptyRoutines.visibility = if (routines.isEmpty()) View.VISIBLE else View.GONE
        }

        vm.loading.observe(this) { b.progress.visibility = if (it) View.VISIBLE else View.GONE }
        vm.error.observe(this) { b.tvError.text = it ?: "" }
        vm.routineMessage.observe(this) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                vm.clearRoutineMessage()
            }
        }

        vm.syncData()
    }

    private fun openCreateRoutineDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_routine, null)
        val etName = dialogView.findViewById<EditText>(R.id.etRoutineName)
        val etTime = dialogView.findViewById<EditText>(R.id.etRoutineTime)

        AlertDialog.Builder(this)
            .setTitle("Crear rutina")
            .setView(dialogView)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar") { _, _ ->
                vm.createRoutine(
                    name = etName.text.toString(),
                    timeMinutes = etTime.text.toString()
                )
            }
            .show()
    }
}
