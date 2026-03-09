package com.example.gymtrackpro.ui.home

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
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
    private lateinit var connectivityManager: ConnectivityManager
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            runOnUiThread { vm.onConnectivityChanged(true) }
        }

        override fun onLost(network: Network) {
            runOnUiThread { vm.onConnectivityChanged(isNetworkAvailable()) }
        }
    }

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
            confirmDeleteRoutine(routine, NO_POSITION)
        },
        onShare = { routine ->
            vm.shareRoutine(routine.id, routine.name)
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(b.root)
        connectivityManager = getSystemService(ConnectivityManager::class.java)

        b.rvExercises.adapter = adapter
        b.rvExercises.itemAnimator = null
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

        vm.loading.observe(this) { isLoading ->
            val hasItems = vm.routines.value?.isNotEmpty() == true
            b.progress.visibility = if (isLoading && !hasItems) View.VISIBLE else View.GONE
        }
        vm.error.observe(this) { b.tvError.text = it ?: "" }
        vm.routineMessage.observe(this) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                vm.clearRoutineMessage()
            }
        }

        startNetworkMonitoring()
        vm.onConnectivityChanged(isNetworkAvailable())
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { connectivityManager.unregisterNetworkCallback(networkCallback) }
    }

    private fun attachRoutineSwipeActions() {
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val pos = viewHolder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return 0
                val routine = adapter.getItemAt(pos) ?: return 0
                return when (routine.routineType) {
                    "OWN" -> ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                    "FAVORITE" -> ItemTouchHelper.RIGHT
                    else -> 0
                }
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val holder = viewHolder as? RoutineAdapter.VH
                resetSwipedHolderVisual(holder)

                if (viewHolder is RoutineAdapter.VH) {
                    ItemTouchHelper.Callback.getDefaultUIUtil().clearView(viewHolder.b.foregroundContainer)
                    viewHolder.b.btnDelete.alpha = 1f
                    viewHolder.b.btnShare.alpha = 1f
                }
                val pos = when {
                    viewHolder.bindingAdapterPosition != RecyclerView.NO_POSITION -> viewHolder.bindingAdapterPosition
                    viewHolder.absoluteAdapterPosition != RecyclerView.NO_POSITION -> viewHolder.absoluteAdapterPosition
                    viewHolder.layoutPosition != RecyclerView.NO_POSITION -> viewHolder.layoutPosition
                    else -> RecyclerView.NO_POSITION
                }
                if (pos == RecyclerView.NO_POSITION) {
                    forceRebindRoutines()
                    return
                }
                val routine = adapter.getItemAt(pos)
                if (routine == null) {
                    forceRebindRoutines()
                    return
                }
                if (routine.routineType != "OWN" && routine.routineType != "FAVORITE") {
                    forceRebindRoutines()
                    return
                }
                if (direction == ItemTouchHelper.LEFT) {
                    if (routine.routineType == "OWN") {
                        vm.shareRoutine(routine.id, routine.name)
                    }
                } else {
                    if (routine.routineType == "OWN") {
                        confirmDeleteRoutine(routine, pos)
                    } else if (routine.routineType == "FAVORITE") {
                        confirmRemoveFavoriteRoutine(routine, pos)
                    }
                }
                forceRebindRoutines()
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                if (viewHolder is RoutineAdapter.VH) {
                    ItemTouchHelper.Callback.getDefaultUIUtil()
                        .onSelected(viewHolder.b.foregroundContainer)
                }
                super.onSelectedChanged(viewHolder, actionState)
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
                if (routine == null) {
                    ItemTouchHelper.Callback.getDefaultUIUtil().onDraw(
                        c,
                        recyclerView,
                        viewHolder.b.foregroundContainer,
                        0f,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                    return
                }

                if (routine.routineType == "FAVORITE" && dX < 0f) {
                    ItemTouchHelper.Callback.getDefaultUIUtil().onDraw(
                        c,
                        recyclerView,
                        viewHolder.b.foregroundContainer,
                        0f,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                    viewHolder.b.btnDelete.alpha = 1f
                    viewHolder.b.btnShare.alpha = 0f
                    return
                }

                if (routine.routineType == "FAVORITE" && dX > 0f) {
                    val maxShift = recyclerView.width.toFloat()
                    val translated = dX.coerceIn(0f, maxShift)
                    ItemTouchHelper.Callback.getDefaultUIUtil().onDraw(
                        c,
                        recyclerView,
                        viewHolder.b.foregroundContainer,
                        translated,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                    viewHolder.b.btnDelete.alpha = 1f
                    viewHolder.b.btnShare.alpha = 0f
                    return
                }

                if (routine.routineType != "OWN" && routine.routineType != "FAVORITE") {
                    ItemTouchHelper.Callback.getDefaultUIUtil().onDraw(
                        c,
                        recyclerView,
                        viewHolder.b.foregroundContainer,
                        0f,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                    return
                }

                val maxShift = recyclerView.width.toFloat()
                val translated = dX.coerceIn(-maxShift, maxShift)
                ItemTouchHelper.Callback.getDefaultUIUtil().onDraw(
                    c,
                    recyclerView,
                    viewHolder.b.foregroundContainer,
                    translated,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                when {
                    translated > 0f -> {
                        viewHolder.b.btnDelete.alpha = 1f
                        viewHolder.b.btnShare.alpha = 0f
                    }
                    translated < 0f -> {
                        viewHolder.b.btnDelete.alpha = 0f
                        viewHolder.b.btnShare.alpha = 1f
                    }
                    else -> {
                        viewHolder.b.btnDelete.alpha = 1f
                        viewHolder.b.btnShare.alpha = 1f
                    }
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                if (viewHolder !is RoutineAdapter.VH) return
                ItemTouchHelper.Callback.getDefaultUIUtil().clearView(viewHolder.b.foregroundContainer)
                viewHolder.b.btnDelete.alpha = 1f
                viewHolder.b.btnShare.alpha = 1f
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.45f

            override fun getAnimationDuration(
                recyclerView: RecyclerView,
                animationType: Int,
                animateDx: Float,
                animateDy: Float
            ): Long = 90L
        }

        ItemTouchHelper(callback).attachToRecyclerView(b.rvExercises)
    }

    private fun confirmDeleteRoutine(routine: RoutineEntity, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar rutina")
            .setMessage("Se eliminara \"${routine.name}\".")
            .setNegativeButton("Cancelar") { _, _ ->
                if (position != NO_POSITION) {
                    forceRebindRoutines()
                }
            }
            .setPositiveButton("Eliminar") { _, _ ->
                vm.deleteRoutine(routine.id)
                forceRebindRoutines()
            }
            .setOnCancelListener {
                if (position != NO_POSITION) {
                    forceRebindRoutines()
                }
            }
            .show()
    }

    private fun confirmRemoveFavoriteRoutine(routine: RoutineEntity, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Quitar de favoritos")
            .setMessage("Se quitara \"${routine.name}\" de favoritos.")
            .setNegativeButton("Cancelar") { _, _ ->
                if (position != NO_POSITION) {
                    forceRebindRoutines()
                }
            }
            .setPositiveButton("Quitar") { _, _ ->
                vm.deleteRoutine(routine.id)
                forceRebindRoutines()
            }
            .setOnCancelListener {
                if (position != NO_POSITION) {
                    forceRebindRoutines()
                }
            }
            .show()
    }

    private fun resetSwipedHolderVisual(holder: RoutineAdapter.VH?) {
        if (holder == null) return
        ItemTouchHelper.Callback.getDefaultUIUtil().clearView(holder.b.foregroundContainer)
        holder.itemView.translationX = 0f
        holder.b.foregroundContainer.translationX = 0f
        holder.b.btnDelete.alpha = 1f
        holder.b.btnShare.alpha = 1f
    }

    private fun forceRebindRoutines() {
        b.rvExercises.post {
            adapter.notifyDataSetChanged()
            val childCount = b.rvExercises.childCount
            for (i in 0 until childCount) {
                val child = b.rvExercises.getChildAt(i)
                val holder = b.rvExercises.getChildViewHolder(child) as? RoutineAdapter.VH
                resetSwipedHolderVisual(holder)
            }
        }
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

    private fun startNetworkMonitoring() {
        runCatching { connectivityManager.registerDefaultNetworkCallback(networkCallback) }
    }

    private fun isNetworkAvailable(): Boolean {
        val active = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(active) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
