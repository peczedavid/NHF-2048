package peczedavid.nhf.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import peczedavid.nhf.data.GameRun
import peczedavid.nhf.view.GameRunAdapter
import peczedavid.nhf.data.LeaderboardDatabase
import peczedavid.nhf.databinding.ActivityLeaderboardBinding
import kotlin.concurrent.thread

class LeaderboardActivity : AppCompatActivity(), GameRunAdapter.GameRunClickListener {
    private lateinit var binding: ActivityLeaderboardBinding

    private lateinit var database: LeaderboardDatabase
    private lateinit var adapter: GameRunAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLeaderboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = LeaderboardDatabase.getDatabase(applicationContext)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        adapter = GameRunAdapter(this)
        binding.leaderboardRv.layoutManager = LinearLayoutManager(this)
        binding.leaderboardRv.adapter = adapter
        loadItemsInBackground()
    }

    private fun loadItemsInBackground() {
        thread {
            val gameRuns = database.gameRunDao().getAll()
            runOnUiThread {
                adapter.update(gameRuns)
            }
        }
    }

    override fun onItemChanged(gameRun: GameRun) {
        thread {
            database.gameRunDao().update(gameRun)
        }
    }
}