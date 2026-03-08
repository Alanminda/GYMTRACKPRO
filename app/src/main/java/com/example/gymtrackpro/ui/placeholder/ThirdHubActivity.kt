package com.example.gymtrackpro.ui.placeholder

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gymtrackpro.R
import com.example.gymtrackpro.databinding.ActivityThirdHubBinding
import com.example.gymtrackpro.ui.adapters.CommunityRoutineAdapter
import com.example.gymtrackpro.ui.community.CommunityViewModel
import com.example.gymtrackpro.ui.navigation.MainBottomNav
import com.example.gymtrackpro.utils.AppProvider
import com.example.gymtrackpro.utils.ViewModelFactory

class ThirdHubActivity : AppCompatActivity() {

    private lateinit var b: ActivityThirdHubBinding
    private val vm: CommunityViewModel by viewModels {
        ViewModelFactory(AppProvider.provideRepository(this))
    }
    private val adapter = CommunityRoutineAdapter(
        onClick = { _ ->
            Toast.makeText(this, "Vista detalle publica: siguiente paso", Toast.LENGTH_SHORT).show()
        },
        onToggleFavorite = { item ->
            vm.toggleFavorite(item)
        }
    )
    private var currentItemsCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityThirdHubBinding.inflate(layoutInflater)
        setContentView(b.root)

        MainBottomNav.bind(
            activity = this,
            bottomNav = b.bottomNav,
            selectedItemId = R.id.nav_third
        )

        b.rvCommunity.layoutManager = LinearLayoutManager(this)
        b.rvCommunity.setHasFixedSize(true)
        b.rvCommunity.adapter = adapter

        val lm = b.rvCommunity.layoutManager as LinearLayoutManager
        b.rvCommunity.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) vm.loadMoreIfNeeded(lm.findLastVisibleItemPosition())
            }
        })

        vm.items.observe(this) { items ->
            currentItemsCount = items.size
            adapter.submitList(items)
            b.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            b.rvCommunity.post {
                if (items.isNotEmpty() && !b.rvCommunity.canScrollVertically(1)) {
                    vm.loadMoreIfNeeded(items.lastIndex)
                }
            }
        }
        vm.loading.observe(this) { isLoading ->
            b.progressInitial.visibility = if (isLoading && currentItemsCount == 0) View.VISIBLE else View.GONE
            b.progressPaging.visibility = if (isLoading && currentItemsCount > 0) View.VISIBLE else View.GONE
        }
        vm.error.observe(this) { b.tvError.text = it ?: "" }
        vm.message.observe(this) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                vm.clearMessage()
            }
        }

        vm.loadInitial()
    }

    override fun onResume() {
        super.onResume()
        MainBottomNav.syncSelection(b.bottomNav, R.id.nav_third)
    }
}
