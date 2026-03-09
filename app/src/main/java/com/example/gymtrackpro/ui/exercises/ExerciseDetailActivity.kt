package com.example.gymtrackpro.ui.exercises

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.example.gymtrackpro.data.local.entities.ExerciseEntity
import com.example.gymtrackpro.data.remote.api.ApiClient
import com.example.gymtrackpro.databinding.ActivityExerciseDetailBinding
import com.example.gymtrackpro.ui.routines.RoutinePickerActivity
import com.example.gymtrackpro.utils.AppProvider
import com.example.gymtrackpro.utils.ViewModelFactory
import com.google.android.material.snackbar.Snackbar

class ExerciseDetailActivity : AppCompatActivity() {

    private lateinit var b: ActivityExerciseDetailBinding
    private val vm: ExerciseDetailViewModel by viewModels {
        ViewModelFactory(AppProvider.provideRepository(this))
    }
    private var currentExerciseId: String = ""
    private val routinePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) return@registerForActivityResult
        val routineId = result.data?.getIntExtra(RoutinePickerActivity.EXTRA_SELECTED_ROUTINE_ID, -1) ?: -1
        if (routineId > 0 && currentExerciseId.isNotBlank()) {
            vm.addExerciseToRoutine(routineId, currentExerciseId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityExerciseDetailBinding.inflate(layoutInflater)
        setContentView(b.root)

        val exerciseId = intent.getStringExtra(EXTRA_ID).orEmpty()
        currentExerciseId = exerciseId
        val targetRoutineId = intent.getIntExtra(EXTRA_TARGET_ROUTINE_ID, -1)
        val name = intent.getStringExtra(EXTRA_NAME).orEmpty()
        val muscleGroup = intent.getStringExtra(EXTRA_MUSCLE_GROUP).orEmpty()
        val bodyPart = intent.getStringExtra(EXTRA_BODY_PART).orEmpty()
        val equipment = intent.getStringExtra(EXTRA_EQUIPMENT).orEmpty()
        val target = intent.getStringExtra(EXTRA_TARGET).orEmpty()
        val gifUrl = normalizeMediaUrl(intent.getStringExtra(EXTRA_GIF_URL).orEmpty())
        val secondaryMuscles = intent.getStringExtra(EXTRA_SECONDARY_MUSCLES).orEmpty()
        val instructions = intent.getStringExtra(EXTRA_INSTRUCTIONS).orEmpty()

        render(
            ExerciseEntity(
                id = exerciseId.ifBlank { name },
                name = name,
                muscleGroup = muscleGroup,
                gifUrl = gifUrl,
                bodyPart = bodyPart,
                equipment = equipment,
                target = target,
                secondaryMuscles = secondaryMuscles,
                instructions = instructions
            )
        )

        vm.detail.observe(this) { detail ->
            if (detail != null) render(detail)
        }

        if (exerciseId.isNotBlank() && (gifUrl.isBlank() || instructions.isBlank())) {
            vm.loadDetail(exerciseId)
        }

        if (targetRoutineId > 0) {
            b.btnAddToRoutine.text = "Anadir a la rutina"
        } else {
            b.btnAddToRoutine.text = "Anadir a una rutina"
        }

        b.btnAddToRoutine.setOnClickListener {
            if (exerciseId.isBlank()) {
                Toast.makeText(this, "Ejercicio invalido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (targetRoutineId > 0) {
                vm.addExerciseToRoutine(targetRoutineId, exerciseId)
            } else {
                openRoutinePicker()
            }
        }

        vm.message.observe(this) { msg ->
            if (!msg.isNullOrBlank()) {
                if (msg.startsWith("Ejercicio anadido")) {
                    showAddedFeedback(msg)
                    b.btnAddToRoutine.postDelayed({
                        finish()
                        overridePendingTransition(0, 0)
                    }, 550)
                } else {
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }
                vm.clearMessage()
            }
        }

    }

    private fun valueOrFallback(label: String, value: String): String {
        return if (value.isBlank()) "$label: N/D" else "$label: $value"
    }

    private fun render(exercise: ExerciseEntity) {
        b.tvTitle.text = exercise.name
        b.tvMuscleGroup.text = valueOrFallback("Grupo muscular", exercise.muscleGroup)
        b.tvBodyPart.text = valueOrFallback("Parte del cuerpo", exercise.bodyPart.orEmpty())
        b.tvEquipment.text = valueOrFallback("Equipo", exercise.equipment.orEmpty())
        b.tvTarget.text = valueOrFallback("Objetivo", exercise.target.orEmpty())
        b.tvSecondaryMuscles.text = valueOrFallback("Musculos secundarios", exercise.secondaryMuscles.orEmpty())
        b.tvInstructions.text = valueOrFallback("Instrucciones", exercise.instructions.orEmpty())

        val proxyUrl = resolveProxyMediaUrl(exercise)
        val directUrl = normalizeMediaUrl(exercise.gifUrl.orEmpty())
        val primaryUrl = if (proxyUrl.isNotBlank()) proxyUrl else directUrl

        if (primaryUrl.isBlank()) {
            b.ivExercise.visibility = View.GONE
        } else {
            b.ivExercise.visibility = View.VISIBLE
            var req: RequestBuilder<android.graphics.drawable.Drawable> = Glide.with(this)
                .load(primaryUrl)
                .thumbnail(0.25f)
                .placeholder(android.R.drawable.ic_menu_gallery)

            val fallbackUrl = if (primaryUrl == proxyUrl) directUrl else proxyUrl
            if (fallbackUrl.isNotBlank() && fallbackUrl != primaryUrl) {
                req = req.error(
                    Glide.with(this)
                        .load(fallbackUrl)
                        .thumbnail(0.25f)
                )
            }

            req.into(b.ivExercise)
        }
    }

    private fun normalizeMediaUrl(url: String): String {
        if (url.isBlank()) return ""
        return if (url.startsWith("http://")) {
            "https://${url.removePrefix("http://")}"
        } else {
            url
        }
    }

    private fun resolveProxyMediaUrl(exercise: ExerciseEntity): String {
        val id = exercise.id.trim()
        if (id.isNotBlank()) {
            return "${ApiClient.BASE_URL}exercises/$id/media"
        }
        return ""
    }

    private fun openRoutinePicker() {
        routinePickerLauncher.launch(Intent(this, RoutinePickerActivity::class.java))
    }

    private fun showAddedFeedback(message: String) {
        b.btnAddToRoutine.isEnabled = false
        b.btnAddToRoutine.text = "Anadido"
        b.btnAddToRoutine.setIconResource(android.R.drawable.checkbox_on_background)
        Snackbar.make(b.root, message, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_TARGET_ROUTINE_ID = "extra_target_routine_id"
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_MUSCLE_GROUP = "extra_muscle_group"
        const val EXTRA_BODY_PART = "extra_body_part"
        const val EXTRA_EQUIPMENT = "extra_equipment"
        const val EXTRA_TARGET = "extra_target"
        const val EXTRA_GIF_URL = "extra_gif_url"
        const val EXTRA_SECONDARY_MUSCLES = "extra_secondary_muscles"
        const val EXTRA_INSTRUCTIONS = "extra_instructions"
    }
}
