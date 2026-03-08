package com.example.gymtrackpro.ui.placeholder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gymtrackpro.R
import com.example.gymtrackpro.databinding.ActivityThirdHubBinding
import com.example.gymtrackpro.ui.navigation.MainBottomNav

class ThirdHubActivity : AppCompatActivity() {

    private lateinit var b: ActivityThirdHubBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityThirdHubBinding.inflate(layoutInflater)
        setContentView(b.root)

        MainBottomNav.bind(
            activity = this,
            bottomNav = b.bottomNav,
            selectedItemId = R.id.nav_third
        )
    }

    override fun onResume() {
        super.onResume()
        MainBottomNav.syncSelection(b.bottomNav, R.id.nav_third)
    }
}
