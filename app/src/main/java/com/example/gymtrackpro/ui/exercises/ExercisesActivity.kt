package com.example.gymtrackpro.ui.exercises

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymtrackpro.R
import com.example.gymtrackpro.databinding.ActivityExercisesBinding
import com.example.gymtrackpro.ui.adapters.ExerciseAdapter
import com.example.gymtrackpro.ui.navigation.MainBottomNav
import com.example.gymtrackpro.utils.AppProvider
import com.example.gymtrackpro.utils.ViewModelFactory

class ExercisesActivity : AppCompatActivity() {

    private lateinit var b: ActivityExercisesBinding

    private val vm: ExercisesViewModel by viewModels {
        ViewModelFactory(AppProvider.provideRepository(this))
    }

    private val targetRoutineId by lazy { intent.getIntExtra(EXTRA_TARGET_ROUTINE_ID, -1) }
    private val isRoutineSelectionMode by lazy { targetRoutineId > 0 }

    private val adapter = ExerciseAdapter { exercise ->
        startActivity(Intent(this, ExerciseDetailActivity::class.java).apply {
            putExtra(ExerciseDetailActivity.EXTRA_ID, exercise.id)
            putExtra(ExerciseDetailActivity.EXTRA_TARGET_ROUTINE_ID, targetRoutineId)
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
    private var currentItemsCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityExercisesBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.rvExercises.layoutManager = LinearLayoutManager(this)
        b.rvExercises.setHasFixedSize(true)
        b.rvExercises.adapter = adapter
        if (isRoutineSelectionMode) {
            b.bottomNav.visibility = View.GONE
            b.tvSelectModeHint.visibility = View.VISIBLE
        } else {
            MainBottomNav.bind(this, b.bottomNav, R.id.nav_exercises)
            b.tvSelectModeHint.visibility = View.GONE
        }
        val lm = b.rvExercises.layoutManager as LinearLayoutManager
        b.rvExercises.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    vm.loadMoreIfNeeded(lm.findLastVisibleItemPosition())
                }
            }
        })

        b.etSearch.addTextChangedListener { vm.onQueryChanged(it?.toString().orEmpty()) }

        vm.exercises.observe(this) { items ->
            currentItemsCount = items.size
            adapter.submit(items)
            b.tvEmptyExercises.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            b.rvExercises.post {
                if (items.isNotEmpty() && !b.rvExercises.canScrollVertically(1)) {
                    vm.loadMoreIfNeeded(items.lastIndex)
                }
            }
        }
        vm.loading.observe(this) { isLoading ->
            b.progressInitial.visibility = if (isLoading && currentItemsCount == 0) View.VISIBLE else View.GONE
            b.progressPaging.visibility = if (isLoading && currentItemsCount > 0) View.VISIBLE else View.GONE
        }
        vm.error.observe(this) { b.tvError.text = it ?: "" }

        vm.loadExercises()
    }

    override fun onResume() {
        super.onResume()
        if (!isRoutineSelectionMode) {
            MainBottomNav.syncSelection(b.bottomNav, R.id.nav_exercises)
        }
    }

    companion object {
        const val EXTRA_TARGET_ROUTINE_ID = "extra_target_routine_id"
    }
}
