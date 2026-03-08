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

    private lateinit var b: ActivityRoutinePickerBinding

    private val vm: RoutinePickerViewModel by viewModels {
        ViewModelFactory(AppProvider.provideRepository(this))
    }

    private val adapter = RoutineAdapter(
        onClick = { routine ->
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

        b.rvRoutines.layoutManager = LinearLayoutManager(this)
        b.rvRoutines.setHasFixedSize(true)
        b.rvRoutines.adapter = adapter

        vm.routines.observe(this) { routines ->
            adapter.submit(routines)
            b.tvEmpty.visibility = if (routines.isEmpty()) View.VISIBLE else View.GONE
        }
        vm.error.observe(this) { b.tvError.text = it ?: "" }

        vm.loadRoutines()
    }

    companion object {
        const val EXTRA_SELECTED_ROUTINE_ID = "extra_selected_routine_id"
        const val EXTRA_SELECTED_ROUTINE_NAME = "extra_selected_routine_name"
    }
}
