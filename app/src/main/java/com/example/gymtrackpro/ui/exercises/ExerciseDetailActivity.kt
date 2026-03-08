package com.example.gymtrackpro.ui.exercises

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.gymtrackpro.databinding.ActivityExerciseDetailBinding

class ExerciseDetailActivity : AppCompatActivity() {

    private lateinit var b: ActivityExerciseDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityExerciseDetailBinding.inflate(layoutInflater)
        setContentView(b.root)

        val name = intent.getStringExtra(EXTRA_NAME).orEmpty()
        val muscleGroup = intent.getStringExtra(EXTRA_MUSCLE_GROUP).orEmpty()
        val bodyPart = intent.getStringExtra(EXTRA_BODY_PART).orEmpty()
        val equipment = intent.getStringExtra(EXTRA_EQUIPMENT).orEmpty()
        val target = intent.getStringExtra(EXTRA_TARGET).orEmpty()
        val gifUrl = intent.getStringExtra(EXTRA_GIF_URL).orEmpty()
        val secondaryMuscles = intent.getStringExtra(EXTRA_SECONDARY_MUSCLES).orEmpty()
        val instructions = intent.getStringExtra(EXTRA_INSTRUCTIONS).orEmpty()

        b.tvTitle.text = name
        b.tvMuscleGroup.text = valueOrFallback("Grupo muscular", muscleGroup)
        b.tvBodyPart.text = valueOrFallback("Parte del cuerpo", bodyPart)
        b.tvEquipment.text = valueOrFallback("Equipo", equipment)
        b.tvTarget.text = valueOrFallback("Objetivo", target)
        b.tvSecondaryMuscles.text = valueOrFallback("Musculos secundarios", secondaryMuscles)
        b.tvInstructions.text = valueOrFallback("Instrucciones", instructions)

        if (gifUrl.isBlank()) {
            b.ivExercise.visibility = View.GONE
        } else {
            b.ivExercise.visibility = View.VISIBLE
            Glide.with(this)
                .asGif()
                .load(gifUrl)
                .error(
                    Glide.with(this)
                        .load(gifUrl)
                )
                .into(b.ivExercise)
        }

        b.btnBack.setOnClickListener { finish() }
    }

    private fun valueOrFallback(label: String, value: String): String {
        return if (value.isBlank()) "$label: N/D" else "$label: $value"
    }

    companion object {
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
