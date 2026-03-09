package com.example.gymtrackpro.ui.routines

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private var routineType: String = "OWN"
    private var currentExercisesCount: Int = 0
    private var currentCompletedCount: Int = 0

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
        routineType = intent.getStringExtra(EXTRA_ROUTINE_TYPE).orEmpty()

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
        b.btnClearCompleted.setOnClickListener {
            vm.clearAllCompletedExercises()
        }

        b.rvRoutineExercises.layoutManager = LinearLayoutManager(this)
        b.rvRoutineExercises.setHasFixedSize(true)
        b.rvRoutineExercises.adapter = adapter
        attachExerciseSwipeActions()

        vm.exercises.observe(this) { items ->
            currentExercisesCount = items.size
            adapter.submit(items)
            b.tvEmptyRoutineExercises.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            refreshCompletionUi()
        }
        vm.completedExerciseIds.observe(this) { completed ->
            currentCompletedCount = completed.size
            adapter.submitCompletedIds(completed)
            refreshCompletionUi()
        }
        vm.loading.observe(this) { b.progress.visibility = if (it) View.VISIBLE else View.GONE }
        vm.error.observe(this) { b.tvError.text = it ?: "" }
        vm.message.observe(this) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                vm.clearMessage()
            }
        }

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

    private fun attachExerciseSwipeActions() {
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val pos = viewHolder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return 0
                return if (routineType == "OWN") {
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                } else {
                    ItemTouchHelper.LEFT
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) {
                    adapter.notifyDataSetChanged()
                    return
                }
                val exercise = adapter.getItemAt(pos)
                if (exercise == null) {
                    adapter.notifyItemChanged(pos)
                    return
                }

                if (direction == ItemTouchHelper.LEFT) {
                    vm.toggleExerciseCompleted(exercise.id)
                    adapter.notifyItemChanged(pos)
                } else {
                    if (routineType == "OWN") {
                        confirmDeleteExercise(exercise.id, pos)
                    }
                    adapter.notifyItemChanged(pos)
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    return
                }

                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top

                if (dX < 0f) {
                    val bg = ContextCompat.getColor(this@RoutineDetailActivity, android.R.color.holo_green_dark)
                    val icon = ContextCompat.getDrawable(
                        this@RoutineDetailActivity,
                        com.example.gymtrackpro.R.drawable.ic_check_circle_24
                    )
                    if (icon != null) {
                        val wrapped = DrawableCompat.wrap(icon.mutate())
                        DrawableCompat.setTint(
                            wrapped,
                            ContextCompat.getColor(this@RoutineDetailActivity, android.R.color.white)
                        )
                        drawSwipeBackground(c, itemView, dX, bg, wrapped, itemHeight, alignRight = true)
                    }
                } else if (dX > 0f) {
                    val allowDelete = routineType == "OWN"
                    val bgColor = if (allowDelete) {
                        ContextCompat.getColor(this@RoutineDetailActivity, android.R.color.holo_red_dark)
                    } else {
                        ContextCompat.getColor(this@RoutineDetailActivity, android.R.color.darker_gray)
                    }
                    val iconRes = if (allowDelete) {
                        com.example.gymtrackpro.R.drawable.ic_delete_24
                    } else {
                        android.R.drawable.ic_menu_close_clear_cancel
                    }
                    val icon = ContextCompat.getDrawable(this@RoutineDetailActivity, iconRes)
                    if (icon != null) {
                        val wrapped = DrawableCompat.wrap(icon.mutate())
                        DrawableCompat.setTint(
                            wrapped,
                            if (allowDelete) {
                                ContextCompat.getColor(this@RoutineDetailActivity, android.R.color.white)
                            } else {
                                ContextCompat.getColor(this@RoutineDetailActivity, android.R.color.white)
                            }
                        )
                        drawSwipeBackground(c, itemView, dX, bgColor, wrapped, itemHeight, alignRight = false)
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(callback).attachToRecyclerView(b.rvRoutineExercises)
    }

    private fun confirmDeleteExercise(exerciseId: String, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar ejercicio")
            .setMessage("Estas seguro de eliminar este ejercicio de la rutina?")
            .setNegativeButton("Cancelar") { _, _ ->
                adapter.notifyItemChanged(position)
            }
            .setPositiveButton("Eliminar") { _, _ ->
                vm.removeExercise(exerciseId)
            }
            .setOnCancelListener {
                adapter.notifyItemChanged(position)
            }
            .show()
    }

    private fun drawSwipeBackground(
        canvas: Canvas,
        itemView: View,
        dX: Float,
        bgColor: Int,
        icon: android.graphics.drawable.Drawable,
        itemHeight: Int,
        alignRight: Boolean
    ) {
        val rect = if (alignRight) {
            RectF(
                itemView.right + dX,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat()
            )
        } else {
            RectF(
                itemView.left.toFloat(),
                itemView.top.toFloat(),
                itemView.left + dX,
                itemView.bottom.toFloat()
            )
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bgColor }
        canvas.drawRoundRect(rect, 28f, 28f, paint)

        val iconMargin = (itemHeight - icon.intrinsicHeight) / 2
        val iconTop = itemView.top + iconMargin
        val iconBottom = iconTop + icon.intrinsicHeight
        if (alignRight) {
            val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
            val iconRight = itemView.right - iconMargin
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
        } else {
            val iconLeft = itemView.left + iconMargin
            val iconRight = itemView.left + iconMargin + icon.intrinsicWidth
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
        }
        icon.draw(canvas)
    }

    private fun refreshCompletionUi() {
        val total = currentExercisesCount
        val done = currentCompletedCount.coerceAtMost(total)
        val percent = if (total <= 0) 0 else ((done * 100f) / total).toInt()
        b.progressRoutineCompletion.progress = percent
        b.tvRoutineCompletion.text = "$percent% completado ($done/$total)"
        b.btnClearCompleted.isEnabled = done > 0
        b.btnClearCompleted.alpha = if (done > 0) 1f else 0.5f
    }
}
