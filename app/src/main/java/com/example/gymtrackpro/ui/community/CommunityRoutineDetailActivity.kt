/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/ui/community/CommunityRoutineDetailActivity.kt
 * Proposito: Feed de comunidad, favoritos y detalle de rutinas publicas.
 */
package com.example.gymtrackpro.ui.community

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gymtrackpro.databinding.ActivityCommunityRoutineDetailBinding
import com.example.gymtrackpro.ui.adapters.ExerciseAdapter
import com.example.gymtrackpro.ui.exercises.ExerciseDetailActivity
import com.example.gymtrackpro.utils.AppProvider
import com.example.gymtrackpro.utils.ViewModelFactory

class CommunityRoutineDetailActivity : AppCompatActivity() {

    // [Req C/UI] Binding + adapter para detalle de rutina publica.
    private lateinit var b: ActivityCommunityRoutineDetailBinding
    private val vm: CommunityRoutineDetailViewModel by viewModels {
        ViewModelFactory(AppProvider.provideRepository(this))
    }

    private val adapter = ExerciseAdapter { exercise ->
        // Navega al detalle del ejercicio reutilizando la misma pantalla de ejercicios.
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
        b = ActivityCommunityRoutineDetailBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Contexto recibido desde el feed de comunidad.
        val name = intent.getStringExtra(EXTRA_NAME).orEmpty()
        val ownerName = intent.getStringExtra(EXTRA_OWNER_NAME).orEmpty()
        val ids = intent.getStringArrayListExtra(EXTRA_EXERCISE_IDS).orEmpty()
        val durationMinutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, -1).takeIf { it > 0 }
        val expectedCount = ids.distinct().size

        b.tvTitle.text = if (name.isBlank()) "Rutina de comunidad" else name
        b.tvSubtitle.text = if (ownerName.isBlank()) {
            "$expectedCount ejercicios  |  ${durationMinutes?.let { "$it min" } ?: "Tiempo N/D"}"
        } else {
            "Por $ownerName  |  $expectedCount ejercicios  |  ${durationMinutes?.let { "$it min" } ?: "Tiempo N/D"}"
        }

        // [Req C/UI] RecyclerView optimizada.
        b.rvExercises.layoutManager = LinearLayoutManager(this)
        b.rvExercises.setHasFixedSize(true)
        b.rvExercises.adapter = adapter

        vm.exercises.observe(this) { items ->
            // Render de lista + conteo real cargado.
            adapter.submit(items)
            b.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            val loadedCount = items.size
            b.tvSubtitle.text = if (ownerName.isBlank()) {
                "$loadedCount ejercicios  |  ${durationMinutes?.let { "$it min" } ?: "Tiempo N/D"}"
            } else {
                "Por $ownerName  |  $loadedCount ejercicios  |  ${durationMinutes?.let { "$it min" } ?: "Tiempo N/D"}"
            }
        }
        vm.loading.observe(this) { b.progress.visibility = if (it) View.VISIBLE else View.GONE }
        // [Req B] Error visible si no se pudieron obtener detalles de ejercicios.
        vm.error.observe(this) { b.tvError.text = it ?: "" }

        vm.load(ids)
    }

    companion object {
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_OWNER_NAME = "extra_owner_name"
        const val EXTRA_EXERCISE_IDS = "extra_exercise_ids"
        const val EXTRA_DURATION_MINUTES = "extra_duration_minutes"
    }
}

