package peczedavid.nhf.view

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import peczedavid.nhf.data.GameRun
import peczedavid.nhf.databinding.GamerunLeaderboardBinding
import peczedavid.nhf.model.Utils

class GameRunAdapter(private val listener: GameRunClickListener) :
    RecyclerView.Adapter<GameRunAdapter.GameRunViewHolder>() {

    private var gameRuns = mutableListOf<GameRun>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = GameRunViewHolder (
        GamerunLeaderboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: GameRunViewHolder, position: Int) {
        val gameRun = gameRuns[position]

        holder.binding.placingTv.text = "#${position + 1}"
        holder.binding.pointsTv.text = "Points: ${gameRun.points}"
        holder.binding.timeTv.text = "Time: ${Utils.formatTime(gameRun.seconds.toDouble())}"
    }

    @SuppressLint("NotifyDataSetChanged")
    fun update(gameRuns: List<GameRun>) {
        this.gameRuns.clear()
        this.gameRuns.addAll(gameRuns)
        this.gameRuns.sortByDescending { it.points }
        if(this.gameRuns.size > 20)
            this.gameRuns = this.gameRuns.subList(0, 20)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = gameRuns.size

    interface GameRunClickListener {
        fun onItemChanged(gameRun: GameRun)
    }

    inner class GameRunViewHolder(val binding: GamerunLeaderboardBinding) : RecyclerView.ViewHolder(binding.root)
}