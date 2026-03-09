/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/ui/routines/RoutinePickerActivity.kt
 * Proposito: Detalle/seleccion de rutinas y acciones sobre ejercicios internos.
 */
package com.example.gymtrackpro.ui.routines

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gymtrackpro.databinding.ActivityRoutinePickerBinding
import com.example.gymtrackpro.ui.adapters.RoutineAdapter
import com.example.gymtrackpro.utils.AppProvider
import com.example.gymtrackpro.utils.ViewModelFactory

class RoutinePickerActivity : AppCompatActivity() {

    // [Req C/UI] Pantalla simple de seleccion con RecyclerView + resultado a Activity llamadora.
    private lateinit var b: ActivityRoutinePickerBinding

    private val vm: RoutinePickerViewModel by viewModels {
        ViewModelFactory(AppProvider.provideRepository(this))
    }

    private val adapter = RoutineAdapter(
        onClick = { routine ->
            // Devuelve rutina elegida para "Anadir a rutina" desde detalle de ejercicio.
            setResult(
                Activity.RESULT_OK,
                Intent().apply {
                    putExtra(EXTRA_SELECTED_ROUTINE_ID, routine.id)
                    putExtra(EXTRA_SELECTED_ROUTINE_NAME, routine.name)
                }
            )
            finish()
        },
        onDelete = { _ ->
            // En esta pantalla solo se selecciona una rutina.
        },
        onShare = { _ ->
            // En esta pantalla solo se selecciona una rutina.
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRoutinePickerBinding.inflate(layoutInflater)
        setContentView(b.root)

        // [Req C/UI] Lista optimizada para seleccion rapida.
        b.rvRoutines.layoutManager = LinearLayoutManager(this)
        b.rvRoutines.setHasFixedSize(true)
        b.rvRoutines.adapter = adapter

        vm.routines.observe(this) { routines ->
            // Estado vacio cuando no existen rutinas propias.
            adapter.submit(routines)
            b.tvEmpty.visibility = if (routines.isEmpty()) View.VISIBLE else View.GONE
        }
        // [Req B] Mensaje de error si falla la lectura de datos.
        vm.error.observe(this) { b.tvError.text = it ?: "" }

        vm.loadRoutines()
    }

    companion object {
        const val EXTRA_SELECTED_ROUTINE_ID = "extra_selected_routine_id"
        const val EXTRA_SELECTED_ROUTINE_NAME = "extra_selected_routine_name"
    }
}

