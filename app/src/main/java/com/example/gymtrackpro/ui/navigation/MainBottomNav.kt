package com.example.gymtrackpro.ui.navigation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.gymtrackpro.R
import com.example.gymtrackpro.ui.exercises.ExercisesActivity
import com.example.gymtrackpro.ui.home.HomeActivity
import com.example.gymtrackpro.ui.placeholder.ThirdHubActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

object MainBottomNav {

    fun bind(
        activity: AppCompatActivity,
        bottomNav: BottomNavigationView,
        selectedItemId: Int
    ) {
        syncSelection(bottomNav, selectedItemId)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (selectedItemId != R.id.nav_home) {
                        activity.startActivity(
                            Intent(activity, HomeActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            }
                        )
                        activity.overridePendingTransition(0, 0)
                    }
                    true
                }
                R.id.nav_exercises -> {
                    if (selectedItemId != R.id.nav_exercises) {
                        activity.startActivity(
                            Intent(activity, ExercisesActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            }
                        )
                        activity.overridePendingTransition(0, 0)
                    }
                    true
                }
                R.id.nav_third -> {
                    if (selectedItemId != R.id.nav_third) {
                        activity.startActivity(
                            Intent(activity, ThirdHubActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            }
                        )
                        activity.overridePendingTransition(0, 0)
                    }
                    true
                }
                else -> false
            }
        }
    }

    fun syncSelection(bottomNav: BottomNavigationView, selectedItemId: Int) {
        if (bottomNav.selectedItemId != selectedItemId) {
            bottomNav.menu.findItem(selectedItemId)?.isChecked = true
        }
    }
}
