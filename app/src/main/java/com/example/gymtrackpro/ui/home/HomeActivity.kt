package com.example.gymtrackpro.ui.home

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.gymtrackpro.databinding.ActivityHomeBinding
import com.example.gymtrackpro.ui.adapters.ExerciseAdapter
import com.example.gymtrackpro.utils.AppProvider
import com.example.gymtrackpro.utils.ViewModelFactory

class HomeActivity : AppCompatActivity() {

    private lateinit var b: ActivityHomeBinding

    private val vm: HomeViewModel by viewModels {
        ViewModelFactory(AppProvider.provideRepository(this))
    }

    private val adapter = ExerciseAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.rvExercises.adapter = adapter

        b.btnRefresh.setOnClickListener { vm.refreshExercises() }
        b.btnSync.setOnClickListener { vm.syncProgress() }

        vm.exercises.observe(this) { adapter.submit(it) }

        vm.loading.observe(this) { b.progress.visibility = if (it) View.VISIBLE else View.GONE }
        vm.error.observe(this) { b.tvError.text = it ?: "" }

        // al abrir intenta traer de API; si falla, te quedas con Room (offline)
        vm.refreshExercises()
    }
}
