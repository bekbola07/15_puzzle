package com.example.mi

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var gridLayout: GridLayout
    private val size = 4
    private val tiles = Array(size) { Array(size) { 0 } }
    private val buttons = Array(size) { arrayOfNulls<Button>(size) }
    private var emptyRow = 0
    private var emptyCol = 0
    private var moveCount = 0
    private var timerStarted = false
    private var startTime = 0L
    private var elapsedTime = 0L
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var moveTextView: TextView
    private lateinit var timerTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gridLayout = findViewById(R.id.gridLayout)
        moveTextView = findViewById(R.id.moveCounter)
        timerTextView = findViewById(R.id.timerText)

        val restartButton: Button = findViewById(R.id.restartButton)
        restartButton.setOnClickListener {
            initializeGame()
        }
        initializeGame()
    }

    private fun initializeGame() {
        moveCount = 0
        timerStarted = false
        elapsedTime = 0
        handler.removeCallbacks(updateTimer)
        moveTextView.text = "Moves: 0"
        timerTextView.text = "Time: 0s"

        gridLayout.removeAllViews()

        val numbers = generateSolvablePuzzle()

        for (i in 0 until size) {
            for (j in 0 until size) {
                val number = numbers[i * size + j]
                tiles[i][j] = number
                val button = Button(this).apply {
                    text = if (number != 0) number.toString() else ""
                    textSize = 24f
                    gravity = Gravity.CENTER
                    setOnClickListener { moveTile(i, j) }
                }
                buttons[i][j] = button
                val params = GridLayout.LayoutParams().apply {
                    rowSpec = GridLayout.spec(i, 1f)
                    columnSpec = GridLayout.spec(j, 1f)
                    width = 0
                    height = 0
                }
                gridLayout.addView(button, params)

                if (number == 0) {
                    emptyRow = i
                    emptyCol = j
                }
            }
        }
    }

    private fun moveTile(row: Int, col: Int) {
        if ((abs(emptyRow - row) == 1 && emptyCol == col) ||
            (abs(emptyCol - col) == 1 && emptyRow == row)
        ) {
            if (!timerStarted) {
                startTime = System.currentTimeMillis()
                timerStarted = true
                handler.post(updateTimer)
            }

            tiles[emptyRow][emptyCol] = tiles[row][col]
            tiles[row][col] = 0
            buttons[emptyRow][emptyCol]?.text = buttons[row][col]?.text
            buttons[row][col]?.text = ""
            emptyRow = row
            emptyCol = col

            moveCount++
            moveTextView.text = "Moves: $moveCount"

            if (isSolved()) {
                handler.removeCallbacks(updateTimer)
                Toast.makeText(
                    this,
                    "Solved in $moveCount moves and $elapsedTime seconds!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private val updateTimer = object : Runnable {
        override fun run() {
            elapsedTime = (System.currentTimeMillis() - startTime) / 1000
            timerTextView.text = "Time: ${elapsedTime}s"
            handler.postDelayed(this, 1000)
        }
    }

    private fun isSolved(): Boolean {
        var count = 1
        for (i in 0 until size) {
            for (j in 0 until size) {
                if (i == size - 1 && j == size - 1) {
                    if (tiles[i][j] != 0) return false
                } else {
                    if (tiles[i][j] != count) return false
                    count++
                }
            }
        }
        return true
    }

    fun isSolvable(puzzle: List<Int>): Boolean {
        val gridSize = 4
        val inversions = countInversions(puzzle)
        val blankRowFromBottom = gridSize - (puzzle.indexOf(0) / gridSize)

        return if (gridSize % 2 == 0) {
            (blankRowFromBottom % 2 == 0) == (inversions % 2 != 0)
        } else {
            inversions % 2 == 0
        }
    }

    fun countInversions(puzzle: List<Int>): Int {
        val tiles = puzzle.filter { it != 0 }
        var inversions = 0
        for (i in tiles.indices) {
            for (j in i + 1 until tiles.size) {
                if (tiles[i] > tiles[j]) inversions++
            }
        }
        return inversions
    }

    fun generateSolvablePuzzle(): List<Int> {
        val puzzle = (0 until 16).toMutableList()
        do {
            puzzle.shuffle()
        } while (!isSolvable(puzzle))
        return puzzle
    }
}
