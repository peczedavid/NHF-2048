package peczedavid.nhf.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import peczedavid.nhf.animation.MovementInfo
import peczedavid.nhf.data.GameRun
import peczedavid.nhf.data.LeaderboardDatabase
import peczedavid.nhf.databinding.ActivityGameBinding
import peczedavid.nhf.databinding.GridTileBinding
import peczedavid.nhf.model.Direction
import peczedavid.nhf.model.GameBoard
import peczedavid.nhf.model.Point
import peczedavid.nhf.model.Utils
import peczedavid.nhf.services.TimerService
import peczedavid.nhf.view.TileFormatter
import kotlin.concurrent.thread
import kotlin.math.abs

class GameActivity : AppCompatActivity() {
    private lateinit var binding : ActivityGameBinding
    private lateinit var gridItemBinding: GridTileBinding
    private lateinit var detector: GestureDetectorCompat

    private lateinit var tileFormatter: TileFormatter
    private lateinit var database: LeaderboardDatabase
    private lateinit var serviceIntent: Intent

    private var gameBoard: GameBoard = GameBoard()
    private var tileLayouts: MutableList<LinearLayout> = mutableListOf()
    private var tileViews: MutableList<TextView> = mutableListOf()

    private var tileSize = 0
    private var padding = 0F
    private var moveDist = 0F
    private lateinit var offset: Point

    private var animDuration = 200L
    private var time = 0.0

    private fun startTimer() {
        serviceIntent.putExtra(TimerService.TIME_EXTRA, time)
        startService(serviceIntent)
    }

    private fun stopTimer() {
        stopService(serviceIntent)
    }

    private fun resetTimer() {
        stopTimer()
        time = 0.0
        startTimer()
    }

    private val updateTime: BroadcastReceiver = object : BroadcastReceiver()  {
        override fun onReceive(p0: Context?, p1: Intent) {
            time = p1.getDoubleExtra(TimerService.TIME_EXTRA, 0.0)
            binding.timeTv.text = Utils.formatTime(time)
        }
    }

    private fun toPixels(dps: Int) : Int {
        return (dps * binding.parentLayout.context.resources.displayMetrics.density + 0.5F).toInt()
    }

    private fun handleBackButtonState() {
        val uri = if(gameBoard.gameEnded)
            "@color/grey"
        else
            "@color/brown_1"
        val resource = resources.getIdentifier(uri, null, packageName)
        binding.stepBackBtn.isEnabled = !gameBoard.gameEnded
        binding.stepBackBtn.setBackgroundColor(ContextCompat.getColor(this@GameActivity, resource))
    }

    private fun initButtons() {
        binding.newGameBtn.setOnClickListener {
            gameBoard.newGame()
            drawBoard()
            resetTimer()
            handleBackButtonState()
        }

        binding.stepBackBtn.setOnClickListener {
            gameBoard.stepBack()
            drawBoard()
        }
    }

    private fun initSizes() {
        tileSize = toPixels(70)
        padding = toPixels(10).toFloat()
        moveDist = tileSize + padding

        val offsetHelper = GridTileBinding.inflate(layoutInflater).root
        val offsetX = offsetHelper.x + padding
        val offsetY = offsetHelper.y + padding
        offset = Point(offsetX, offsetY)
    }

    private fun clearTiles() {
        for(tileLayout: LinearLayout in tileLayouts) {
            binding.gameBoard.removeView(tileLayout)
        }

        tileViews.clear()
        tileLayouts.clear()
    }

    private fun drawBoard() {
        clearTiles()

        for(i in 0..3) {
            for(j in 0..3) {
                val value = gameBoard.getValue(i, j)
                createTile(i, j, value)
            }
        }

        binding.pointsTv.text = "Points: ${gameBoard.points}"
    }

    private fun createTile(i: Int, j: Int, value: Int) {
        gridItemBinding = GridTileBinding.inflate(layoutInflater)
        val tileLayout = gridItemBinding.root
        tileFormatter.formatTile(gridItemBinding.textView, value)

        tileLayout.x = offset.x + moveDist * j
        tileLayout.y = offset.y + moveDist * i

        binding.gameBoard.addView(tileLayout)
        tileLayouts.add(tileLayout)
        tileViews.add(gridItemBinding.textView)
    }

    private fun animateTiles(movements: MutableList<MovementInfo>) {
        for(i in 0..15) {
            if(movements[i].sum) {
                var value: Int
                var text: String
                if(tileViews[i].text == "") {
                    value = 0
                    text = ""
                }
                else {
                    value = tileViews[i].text.toString().toInt() * 2
                    text = value.toString()
                }
                tileViews[i].text = text
                tileFormatter.formatTile(tileViews[i], value)
                tileLayouts[i].z = 1F
                tileViews[i].z = 1F
            }
            tileLayouts[i].animate().apply {
                duration = animDuration
                val dX = (movements[i].end.x - movements[i].start.x) * moveDist
                val dY = (movements[i].end.y - movements[i].start.y) * moveDist
                translationXBy(dY)
                translationYBy(dX)
            }.start()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            drawBoard()
        }, animDuration)

        saveGameState()
    }

    private fun loadGameState() {
        gameBoard.loadState(this)

        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        val value =  sharedPref.getInt("gameBoard_time", 0)
        time = value.toDouble()

        drawBoard()
    }

    private fun saveGameState() {
        gameBoard.saveState(this)

        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putInt("gameBoard_time", time.toInt())
            apply()
        }
    }

    private fun getNewRun(points: Int, seconds: Int) = GameRun(
        points = points,
        seconds = seconds
    )

    private fun saveRunToDatabase() {
        val newRun = getNewRun(
            gameBoard.points,
            time.toInt())

        thread {
            database.gameRunDao().insert(newRun)
        }

        stopTimer()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        detector = GestureDetectorCompat(this, GameGestureListener())
        tileFormatter = TileFormatter(packageName, this@GameActivity, resources)

        serviceIntent = Intent(applicationContext, TimerService::class.java)
        registerReceiver(updateTime, IntentFilter(TimerService.TIMER_UPDATED))

        database = LeaderboardDatabase.getDatabase(applicationContext)

        initSizes()
        initButtons()

        drawBoard()
    }

    override fun onResume() {
        super.onResume()
        loadGameState()
        if(!gameBoard.gameEnded)
            startTimer()
        else
            binding.timeTv.text = Utils.formatTime(time)
        handleBackButtonState()
    }

    override fun onPause() {
        super.onPause()
        saveGameState()
        stopTimer()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (detector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    inner class GameGestureListener : GestureDetector.SimpleOnGestureListener() {
        private val swipeThreshold = 100

        override fun onFling(downEvent: MotionEvent?, moveEvent: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            if(gameBoard.gameEnded)
                return false

            val diffX = moveEvent?.x?.minus(downEvent!!.x) ?: 0.0F
            val diffY = moveEvent?.y?.minus(downEvent!!.y) ?: 0.0F

            return if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > swipeThreshold) {
                    if (diffX > 0) {
                        Log.d("GameActivity", "Swipe right")
                        animateTiles(gameBoard.move(Direction.RIGHT))
                        if(gameBoard.gameEnded) {
                            saveRunToDatabase()
                            handleBackButtonState()
                        }
                    } else {
                        Log.d("GameActivity", "Swipe left")
                        animateTiles(gameBoard.move(Direction.LEFT))
                        if(gameBoard.gameEnded) {
                            saveRunToDatabase()
                            handleBackButtonState()
                        }
                    }
                    true
                } else {
                    super.onFling(downEvent, moveEvent, velocityX, velocityY)
                }
            } else {
                if (abs(diffY) > swipeThreshold) {
                    if (diffY > 0) {
                        Log.d("GameActivity", "Swipe down")
                        animateTiles(gameBoard.move(Direction.DOWN))
                        if(gameBoard.gameEnded) {
                            saveRunToDatabase()
                            handleBackButtonState()
                        }
                    } else {
                        Log.d("GameActivity", "Swipe up")
                        animateTiles(gameBoard.move(Direction.UP))
                        if(gameBoard.gameEnded) {
                            saveRunToDatabase()
                            handleBackButtonState()
                        }
                    }
                    true
                } else {
                    super.onFling(downEvent, moveEvent, velocityX, velocityY)
                }
            }
        }
    }
}