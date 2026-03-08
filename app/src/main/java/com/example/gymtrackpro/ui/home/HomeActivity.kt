package com.example.gymtrackpro.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.gymtrackpro.R
import com.example.gymtrackpro.data.local.entities.RoutineEntity
import com.example.gymtrackpro.databinding.ActivityHomeBinding
import com.example.gymtrackpro.ui.adapters.RoutineAdapter
import com.example.gymtrackpro.ui.navigation.MainBottomNav
import com.example.gymtrackpro.ui.routines.RoutineDetailActivity
import com.example.gymtrackpro.utils.AppProvider
import com.example.gymtrackpro.utils.ViewModelFactory

class HomeActivity : AppCompatActivity() {

    private lateinit var b: ActivityHomeBinding

    private val vm: HomeViewModel by viewModels {
        ViewModelFactory(AppProvider.provideRepository(this))
    }

    private val adapter = RoutineAdapter(
        onClick = { routine ->
            startActivity(Intent(this, RoutineDetailActivity::class.java).apply {
                putExtra(RoutineDetailActivity.EXTRA_ROUTINE_ID, routine.id)
                putExtra(RoutineDetailActivity.EXTRA_ROUTINE_NAME, routine.name)
                putExtra(RoutineDetailActivity.EXTRA_ROUTINE_TYPE, routine.routineType)
            })
        },
        onDelete = { routine ->
            confirmDeleteRoutine(routine)
        },
        onShare = { routine ->
            vm.shareRoutine(routine.id)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.rvExercises.adapter = adapter
        (b.rvExercises.itemAnimator as? DefaultItemAnimator)?.supportsChangeAnimations = false
        attachRoutineSwipeActions()
        MainBottomNav.bind(this, b.bottomNav, R.id.nav_home)
        b.toggleRoutineCategory.check(R.id.btnCategoryOwn)
        b.btnCategoryOwn.setOnClickListener { vm.setCategory(HomeViewModel.RoutineCategory.OWN) }
        b.btnCategoryFavorite.setOnClickListener { vm.setCategory(HomeViewModel.RoutineCategory.FAVORITES) }

        b.btnAddRoutine.setOnClickListener {
            openCreateRoutineDialog()
        }

        vm.routines.observe(this) { routines ->
            adapter.submit(routines)
            b.tvEmptyRoutines.visibility = if (routines.isEmpty()) View.VISIBLE else View.GONE
        }
        vm.category.observe(this) { category ->
            b.btnAddRoutine.visibility = if (category == HomeViewModel.RoutineCategory.OWN) View.VISIBLE else View.GONE
            b.tvEmptyRoutines.text = if (category == HomeViewModel.RoutineCategory.OWN) {
                "No hay rutinas propias"
            } else {
                "No hay favoritas de la comunidad"
            }
            adapter.closeActions()
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

    private fun attachRoutineSwipeActions() {
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return
                val routine = adapter.getItemAt(pos) ?: return
                if (routine.routineType != "OWN") {
                    adapter.closeActions()
                    adapter.resetSwipe(pos)
                    return
                }
                if (direction == ItemTouchHelper.LEFT) {
                    adapter.toggleActions(pos)
                } else {
                    adapter.closeActions()
                }
            }

            override fun onChildDraw(
                c: android.graphics.Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE || viewHolder !is RoutineAdapter.VH) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    return
                }

                val position = viewHolder.bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return
                val routine = adapter.getItemAt(position)
                if (routine?.routineType != "OWN") {
                    viewHolder.b.foregroundContainer.translationX = 0f
                    return
                }

                val maxReveal = adapter.revealWidthPx
                val openOffset = if (adapter.isActionsOpen(position)) -maxReveal else 0f
                val translated = (openOffset + dX).coerceIn(-maxReveal, 0f)
                viewHolder.b.foregroundContainer.translationX = translated
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                if (viewHolder !is RoutineAdapter.VH) return
                val position = viewHolder.bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return
                val target = if (adapter.isActionsOpen(position)) -adapter.revealWidthPx else 0f
                viewHolder.b.foregroundContainer.animate().translationX(target).setDuration(120).start()
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.25f
        }

        ItemTouchHelper(callback).attachToRecyclerView(b.rvExercises)
    }

    private fun confirmDeleteRoutine(routine: RoutineEntity) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar rutina")
            .setMessage("Se eliminara \"${routine.name}\".")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                vm.deleteRoutine(routine.id)
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        MainBottomNav.syncSelection(b.bottomNav, R.id.nav_home)
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
