package peczedavid.nhf.model

import android.app.Activity
import android.content.Context
import android.util.Log
import peczedavid.nhf.animation.MovementInfo

class GameBoard() {
    var gameBoardBefore: MutableList<Int> = mutableListOf()
    private var gameBoard: MutableList<Int> = mutableListOf()

    private var summedHelper: MutableList<Boolean> = mutableListOf()

    private var animations: MutableList<MovementInfo> = mutableListOf()

    private var shouldSpawnNewTile = false

    companion object {
        var gameEndHelper = 1
    }

    var pointsBefore = 0
    var points = 0

    var gameEnded = false

    init {
        newGame()
    }

    private fun clone() : GameBoard {
        val cloneBoard = GameBoard()

        for(i in 0..15)
            cloneBoard.gameBoard[i] = gameBoard[i]

        return cloneBoard
    }

    private fun isMovement(movementInfo: MovementInfo) : Boolean {
        val startX =  movementInfo.start.x
        val startY =  movementInfo.start.y
        val endX =  movementInfo.end.x
        val endY =  movementInfo.end.y

        if(startX == endX && startY == endY)
            return false

        return true
    }

    private fun checkForGameEnd() {
        var moved = false

        gameEndHelper = 0

        val gameBoardUp = clone()
        val movementsUp = gameBoardUp.move(Direction.UP)
        for(movement: MovementInfo in movementsUp) {
            if(isMovement(movement))
                moved = true
        }

        val gameBoardDown = clone()
        val movementsDown = gameBoardDown.move(Direction.DOWN)
        for(movement: MovementInfo in movementsDown) {
            if(isMovement(movement))
                moved = true
        }

        val gameBoardRight = clone()
        val movementsRight = gameBoardRight.move(Direction.RIGHT)
        for(movement: MovementInfo in movementsRight) {
            if(isMovement(movement))
                moved = true
        }

        val gameBoardLeft = clone()
        val movementsLeft = gameBoardLeft.move(Direction.LEFT)
        for(movement: MovementInfo in movementsLeft) {
            if(isMovement(movement))
                moved = true
        }

        gameEndHelper = 1
        gameEnded = !moved
        Log.d("Game ended", gameEnded.toString())
    }

    fun newGame() {
        initBoard()
        spawnRandom()
        resetSummedHelper()
        clearAnimations()
        gameBoardBefore.clear()
        pointsBefore = 0
        points = 0
        shouldSpawnNewTile = false
        gameEnded = false
    }

    private fun initBoard() {
        gameBoard.clear()
        gameBoard.addAll(mutableListOf(0, 0, 0, 0))
        gameBoard.addAll(mutableListOf(0, 0, 0, 0))
        gameBoard.addAll(mutableListOf(0, 0, 0, 0))
        gameBoard.addAll(mutableListOf(0, 0, 0, 0))
    }

    private fun resetSummedHelper() {
        summedHelper.clear()
        summedHelper.addAll(mutableListOf(false, false, false, false))
        summedHelper.addAll(mutableListOf(false, false, false, false))
        summedHelper.addAll(mutableListOf(false, false, false, false))
        summedHelper.addAll(mutableListOf(false, false, false, false))
    }

    private fun clearAnimations() {
        animations.clear()
        for(i in 0..15)
            animations.add(MovementInfo(Point(), Point(), 0, 0))
    }

    private fun insertAnimation(idx: Int, movementInfo: MovementInfo) {
        animations.add(idx, movementInfo)
        animations.removeAt(idx + 1)
    }

    private fun moveTile(start: Point, end: Point) : MovementInfo {
        val startValue = gameBoard[Utils.getIndex(start)]
        val endValue = gameBoard[Utils.getIndex(end)]

        if(startValue == 0)
            return MovementInfo(start, start, 0, 0)

        val summed = summedHelper[Utils.getIndex(end)]

        // Can add together
        if(startValue == endValue && !summed) {
            gameBoard[Utils.getIndex(start)] = 0
            gameBoard[Utils.getIndex(end)] = endValue * 2
            summedHelper[Utils.getIndex(end)] = true
            shouldSpawnNewTile = true
            points += endValue * 2
            return MovementInfo(start, end, startValue, startValue*2, true)
        }

        // Can move
        if(endValue == 0) {
            gameBoard[Utils.getIndex(start)] = 0
            gameBoard[Utils.getIndex(end)] = startValue
            shouldSpawnNewTile = true
            return MovementInfo(start, end, startValue, startValue)
        }

        return MovementInfo(start, start, startValue, startValue)
    }

    private fun spawnRandom() : MovementInfo {
        var spawned = false

        var value = 2

        if((1..10).random() > 8) {
            value = 4
        }

        val countEmpty = gameBoard.count { v -> v == 0 }

        var index = -1

        if(countEmpty > 0) {
            while(!spawned) {
                index = (0..15).random()
                if(gameBoard[index] == 0) {
                    gameBoard[index] = value
                    spawned = true
                }
            }
        }

        val point = Utils.getPoint(index)

        if(index == -1) {
            return MovementInfo(Point(), Point(), 0, 0)
        }

        return MovementInfo(Point(), point, 0, value)
    }

    private fun pushTileDown(i: Int, j: Int) : MovementInfo {
        var movementInfo = MovementInfo(Point(i.toFloat(), j.toFloat()), Point(i.toFloat(), j.toFloat()),
            gameBoard[Utils.getIndex(i, j)], gameBoard[Utils.getIndex(i, j)])

        val startValue = gameBoard[Utils.getIndex(i, j)]

        for(_i in i until 3) {
            movementInfo = moveTile(Point(_i.toFloat(), j.toFloat()), Point((_i+1).toFloat(), j.toFloat()))

            if(movementInfo.start == movementInfo.end || movementInfo.sum)
                break
        }

        return MovementInfo(Point(i.toFloat(), j.toFloat()), movementInfo.end, startValue, movementInfo.endValue, movementInfo.sum)
    }

    private fun pushTileUp(i: Int, j: Int) : MovementInfo {
        var movementInfo = MovementInfo(Point(i.toFloat(), j.toFloat()), Point(i.toFloat(), j.toFloat()),
            gameBoard[Utils.getIndex(i, j)], gameBoard[Utils.getIndex(i, j)])

        val startValue = gameBoard[Utils.getIndex(i, j)]

        for(_i in i downTo 1) {
            movementInfo = moveTile(Point(_i.toFloat(), j.toFloat()), Point((_i-1).toFloat(), j.toFloat()))

            if(movementInfo.start == movementInfo.end || movementInfo.sum)
                break
        }

        return MovementInfo(Point(i.toFloat(), j.toFloat()), movementInfo.end, startValue, movementInfo.endValue, movementInfo.sum)
    }

    private fun pushTileLeft(i: Int, j: Int): MovementInfo {
        var movementInfo = MovementInfo(Point(i.toFloat(), j.toFloat()), Point(i.toFloat(), j.toFloat()),
            gameBoard[Utils.getIndex(i, j)], gameBoard[Utils.getIndex(i, j)])

        val startValue = gameBoard[Utils.getIndex(i, j)]

        for(_j in j downTo 1) {
            movementInfo = moveTile(Point(i.toFloat(), _j.toFloat()), Point(i.toFloat(), (_j-1).toFloat()))

            if(movementInfo.start == movementInfo.end || movementInfo.sum)
                break
        }

        return MovementInfo(Point(i.toFloat(), j.toFloat()), movementInfo.end, startValue, movementInfo.endValue, movementInfo.sum)
    }

    private fun pushTileRight(i: Int, j: Int): MovementInfo {
        var movementInfo = MovementInfo(Point(i.toFloat(), j.toFloat()), Point(i.toFloat(), j.toFloat()),
            gameBoard[Utils.getIndex(i, j)], gameBoard[Utils.getIndex(i, j)])

        val startValue = gameBoard[Utils.getIndex(i, j)]

        for(_j in j until 3) {
            movementInfo = moveTile(Point(i.toFloat(), _j.toFloat()), Point(i.toFloat(), (_j+1).toFloat()))

            if(movementInfo.start == movementInfo.end || movementInfo.sum)
                break
        }

        return MovementInfo(Point(i.toFloat(), j.toFloat()), movementInfo.end, startValue, movementInfo.endValue, movementInfo.sum)
    }

    private fun saveGameBoard() {
        gameBoardBefore.clear()

        for(i in 0..15) {
            gameBoardBefore.add(gameBoard[i])
        }

        pointsBefore = points
    }

    fun stepBack() {
        if(gameBoardBefore.size != 0) {
            for(i in 0..15) {
                gameBoard[i] = gameBoardBefore[i]
            }

            points = pointsBefore
            gameEnded = false
        }
    }

    fun move(direction: Direction): MutableList<MovementInfo> {
        clearAnimations()
        resetSummedHelper()

        saveGameBoard()

        shouldSpawnNewTile = false

        when(direction) {
            Direction.RIGHT -> {
                for(i in 0..3) {
                    for(j in 3 downTo 0) {
                        val movementInfo = pushTileRight(i, j)
                        insertAnimation(Utils.getIndex(i, j), movementInfo)
                    }
                }
            }

            Direction.LEFT -> {
                for(i in 0..3) {
                    for(j in 0..3) {
                        val movementInfo = pushTileLeft(i, j)
                        insertAnimation(Utils.getIndex(i, j), movementInfo)
                    }
                }
            }

            Direction.UP -> {
                for(j in 0..3) {
                    for(i in 0..3) {
                        val movementInfo = pushTileUp(i, j)
                        insertAnimation(Utils.getIndex(i, j), movementInfo)
                    }
                }
            }

            Direction.DOWN -> {
                for(j in 0..3) {
                    for(i in 3 downTo 0) {
                        val movementInfo = pushTileDown(i, j)
                        insertAnimation(Utils.getIndex(i, j), movementInfo)
                    }
                }
            }
        }

        if(shouldSpawnNewTile)
            spawnRandom()

        if(gameEndHelper == 1)
            checkForGameEnd()

        return animations
    }

    fun loadState(activity: Activity) {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE) ?: return
        gameBoard.clear()

        if(sharedPref.getInt("gameBoard_element_0", -1) == -1) {
            newGame()
            return
        }

        for(i in 0..15) {
            val value =  sharedPref.getInt("gameBoard_element_$i", 0)
            gameBoard.add(value)
        }

        points = sharedPref.getInt("gameBoard_points", 0)
        gameEnded = sharedPref.getBoolean("gameBoard_game_ended", false)
    }

    fun saveState(activity: Activity) {
        val sharedPref = activity.getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            for(i in 0..15) {
                putInt("gameBoard_element_$i", gameBoard[i])
                apply()
            }

            putInt("gameBoard_points", points)
            apply()

            putBoolean("gameBoard_game_ended", gameEnded)
            apply()
        }
    }

    fun getValue(i: Int, j: Int) : Int {
        return gameBoard[Utils.getIndex(i, j)]
    }
}