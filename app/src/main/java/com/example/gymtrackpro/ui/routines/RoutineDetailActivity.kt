package com.example.gymtrackpro.ui.routines

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gymtrackpro.databinding.ActivityRoutineDetailBinding
import com.example.gymtrackpro.ui.adapters.ExerciseAdapter
import com.example.gymtrackpro.ui.exercises.ExerciseDetailActivity
import com.example.gymtrackpro.utils.AppProvider
import com.example.gymtrackpro.utils.ViewModelFactory

class RoutineDetailActivity : AppCompatActivity() {

    private lateinit var b: ActivityRoutineDetailBinding

    private val vm: RoutineDetailViewModel by viewModels {
        ViewModelFactory(AppProvider.provideRepository(this))
    }
    private var routineId: Int = -1

    private val adapter = ExerciseAdapter { exercise ->
        startActivity(Intent(this, ExerciseDetailActivity::class.java).apply {
            putExtra(ExerciseDetailActivity.EXTRA_ID, exercise.id)
            putExtra(ExerciseDetailActivity.EXTRA_NAME, exercise.name)
            putExtra(ExerciseDetailActivity.EXTRA_MUSCLE_GROUP, exercise.muscleGroup)
            putExtra(ExerciseDetailActivity.EXTRA_BODY_PART, exercise.bodyPart)
            putExtra(ExerciseDetailActivity.EXTRA_EQUIPMENT, exercise.equipment)
            putExtra(ExerciseDetailActivity.EXTRA_TARGET, exercise.target)
            putExtra(ExerciseDetailActivity.EXTRA_GIF_URL, exercise.gifUrl)
            putExtra(ExerciseDetailActivity.EXTRA_SECONDARY_MUSCLES, exercise.secondaryMuscles)
            putExtra(ExerciseDetailActivity.EXTRA_INSTRUCTIONS, exercise.instructions)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRoutineDetailBinding.inflate(layoutInflater)
        setContentView(b.root)

        routineId = intent.getIntExtra(EXTRA_ROUTINE_ID, -1)
        val routineName = intent.getStringExtra(EXTRA_ROUTINE_NAME).orEmpty()
        val routineType = intent.getStringExtra(EXTRA_ROUTINE_TYPE).orEmpty()

        b.tvTitle.text = if (routineName.isBlank()) "Rutina" else routineName
        if (routineType != "OWN") {
            b.btnAddExerciseToRoutine.visibility = View.GONE
        } else {
            b.btnAddExerciseToRoutine.visibility = View.VISIBLE
            b.btnAddExerciseToRoutine.setOnClickListener {
                if (routineId > 0) {
                    startActivity(
                        Intent(this, com.example.gymtrackpro.ui.exercises.ExercisesActivity::class.java).apply {
                            putExtra(com.example.gymtrackpro.ui.exercises.ExercisesActivity.EXTRA_TARGET_ROUTINE_ID, routineId)
                        }
                    )
                }
            }
        }

        b.rvRoutineExercises.layoutManager = LinearLayoutManager(this)
        b.rvRoutineExercises.setHasFixedSize(true)
        b.rvRoutineExercises.adapter = adapter

        vm.exercises.observe(this) { items ->
            adapter.submit(items)
            b.tvEmptyRoutineExercises.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }
        vm.loading.observe(this) { b.progress.visibility = if (it) View.VISIBLE else View.GONE }
        vm.error.observe(this) { b.tvError.text = it ?: "" }

        if (routineId <= 0) {
            b.tvError.text = "Rutina invalida"
            return
        }
        vm.loadRoutineExercises(routineId)
    }

    override fun onResume() {
        super.onResume()
        if (routineId > 0) {
            vm.loadRoutineExercises(routineId)
        }
    }

    companion object {
        const val EXTRA_ROUTINE_ID = "extra_routine_id"
        const val EXTRA_ROUTINE_NAME = "extra_routine_name"
        const val EXTRA_ROUTINE_TYPE = "extra_routine_type"
    }
}
