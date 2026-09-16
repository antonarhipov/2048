package org.game2048.engine

import org.game2048.model.Board
import org.game2048.model.GameState
import org.game2048.model.Move
import kotlin.random.Random

/**
 * Implementation of the GameEngine interface.
 * Handles all game mechanics including moves, state transitions, and tile generation.
 */
class GameEngineImpl : GameEngine {
    private var _currentState: GameState = createInitialState()
    private var previousState: GameState? = null

    override val currentState: GameState
        get() = _currentState

    override fun makeMove(move: Move): GameState {
        // Check if we're already in a terminal state
        if (currentState is GameState.Lost) {
            return currentState
        }

        // Check if any moves are possible
        if (!Move.entries.any { canMoveInDirection(currentState.board, it) }) {
            _currentState = GameState.Lost(currentState.board)
            return currentState
        }

        // Process the move
        val oldBoard = currentState.board
        val (newBoard, scoreIncrease) = processMove(oldBoard, move)

        // Only update previous state if the board actually changed
        if (!newBoard.cells.contentDeepEquals(oldBoard.cells)) {
            previousState = currentState
        }

        val newState = createNewState(newBoard, scoreIncrease)
        _currentState = newState

        return newState
    }

    override fun isValidMove(move: Move): Boolean {
        return when (currentState) {
            is GameState.Lost -> false
            is GameState.Won -> (currentState as GameState.Won).continueGame && canMoveInDirection(currentState.board, move)
            else -> canMoveInDirection(currentState.board, move)
        }
    }

    override fun newGame(size: Int): GameState {
        previousState = null
        _currentState = createInitialState(size)
        return currentState
    }

    override fun undo(): GameState? {
        return previousState?.also {
            _currentState = it
            previousState = null
        }
    }

    override fun continueGame(): GameState {
        return if (currentState is GameState.Won) {
            _currentState = GameState.Won(currentState.board, continueGame = true)
            currentState
        } else {
            currentState
        }
    }

    private fun createInitialState(size: Int = Board.DEFAULT_SIZE): GameState {
        val board = Board(size = size)
        val boardWithTiles = addInitialTiles(board)
        return GameState.Playing(boardWithTiles)
    }

    private fun hasAnyValidMoves(board: Board): Boolean {
        return Move.entries.any { move -> canMoveInDirection(board, move) }
    }

    private fun createNewState(board: Board, scoreIncrease: Int): GameState {
        // If the board hasn't changed, check if we're in a game over state
        if (board == currentState.board) {
            if (!Move.entries.any { canMoveInDirection(board, it) }) {
                return GameState.Lost(board)
            }
            return currentState
        }

        // Add a new tile for valid moves
        val newBoard = addRandomTile(board)

        return when {
            // Check for win condition first
            hasWon(newBoard) && currentState !is GameState.Won -> 
                GameState.Won(newBoard)

            // Continue game after winning if requested
            currentState is GameState.Won -> 
                GameState.Won(newBoard, (currentState as GameState.Won).continueGame)

            // Check for game over condition
            !Move.entries.any { canMoveInDirection(newBoard, it) } -> 
                GameState.Lost(newBoard)

            // Game continues
            else -> GameState.Playing(newBoard, canUndo = true)
        }
    }

    private fun processMove(board: Board, move: Move): Pair<Board, Int> {
        val size = board.size
        val newCells = Array(size) { Array(size) { 0 } }

        // Process the move and calculate score
        val scoreIncrease = when (move) {
            Move.LEFT -> processHorizontalMove(board, newCells, false)
            Move.RIGHT -> processHorizontalMove(board, newCells, true)
            Move.UP -> processVerticalMove(board, newCells, false)
            Move.DOWN -> processVerticalMove(board, newCells, true)
        }

        // Check if the board actually changed
        val boardChanged = !newCells.contentDeepEquals(board.cells)

        return if (boardChanged || scoreIncrease > 0) {
            Board(size, newCells, board.score + scoreIncrease) to scoreIncrease
        } else {
            board to 0
        }
    }

    private fun processHorizontalMove(board: Board, newCells: Array<Array<Int>>, rightward: Boolean): Int {
        val size = board.size
        var totalScore = 0

        for (row in 0 until size) {
            // Get non-zero values in order
            val values = mutableListOf<Int>()
            for (col in 0 until size) {
                val value = board.cells[row][col]
                if (value != 0) values.add(value)
            }
            if (rightward) values.reverse()

            // Process merges
            var i = 0
            while (i < values.size - 1) {
                if (values[i] == values[i + 1]) {
                    values[i] *= 2
                    totalScore += values[i]
                    values.removeAt(i + 1)
                }
                i++
            }

            // Fill the new row with proper padding
            val finalValues = if (rightward) {
                List(size - values.size) { 0 } + values.reversed()
            } else {
                values + List(size - values.size) { 0 }
            }

            // Copy to new cells
            finalValues.forEachIndexed { col, value ->
                newCells[row][col] = value
            }
        }

        return totalScore
    }

    private fun processVerticalMove(board: Board, newCells: Array<Array<Int>>, downward: Boolean): Int {
        val size = board.size
        var totalScore = 0

        for (col in 0 until size) {
            // Get non-zero values in order
            val values = mutableListOf<Int>()
            for (row in 0 until size) {
                val value = board.cells[row][col]
                if (value != 0) values.add(value)
            }
            if (downward) values.reverse()

            // Process merges
            var i = 0
            while (i < values.size - 1) {
                if (values[i] == values[i + 1]) {
                    values[i] *= 2
                    totalScore += values[i]
                    values.removeAt(i + 1)
                }
                i++
            }

            // Fill the new column with proper padding
            val finalValues = if (downward) {
                List(size - values.size) { 0 } + values.reversed()
            } else {
                values + List(size - values.size) { 0 }
            }

            // Copy to new cells
            finalValues.forEachIndexed { row, value ->
                newCells[row][col] = value
            }
        }

        return totalScore
    }

    private fun processTileLine(line: MutableList<Int>): Pair<List<Int>, Int> {
        if (line.isEmpty()) return emptyList<Int>() to 0

        // Step 1: Remove all zeros and get non-zero values in order
        val nonZeroValues = line.filter { it != 0 }
        if (nonZeroValues.isEmpty()) return emptyList<Int>() to 0

        var score = 0
        val result = mutableListOf<Int>()

        // Step 2: Process merges for adjacent equal values
        var i = 0
        while (i < nonZeroValues.size) {
            if (i + 1 < nonZeroValues.size && nonZeroValues[i] == nonZeroValues[i + 1]) {
                // Merge equal adjacent values
                val mergedValue = nonZeroValues[i] * 2
                result.add(mergedValue)
                score += mergedValue
                i += 2
            } else {
                // Keep single value
                result.add(nonZeroValues[i])
                i++
            }
        }

        return result to score
    }

    private fun addInitialTiles(board: Board): Board {
        var newBoard = board
        repeat(GameEngine.INITIAL_TILES) {
            newBoard = addRandomTile(newBoard)
        }
        return newBoard
    }

    private fun addRandomTile(board: Board): Board {
        val emptyCells = findEmptyCells(board)
        if (emptyCells.isEmpty()) return board

        val (row, col) = emptyCells.random()
        val value = if (Random.nextDouble() < GameEngine.PROBABILITY_OF_FOUR) 4 else 2

        val newCells = board.cells.map { it.clone() }.toTypedArray()
        newCells[row][col] = value

        return Board(board.size, newCells, board.score)
    }

    private fun findEmptyCells(board: Board): List<Pair<Int, Int>> {
        return buildList {
            for (row in board.cells.indices) {
                for (col in board.cells[row].indices) {
                    if (board.cells[row][col] == 0) {
                        add(row to col)
                    }
                }
            }
        }
    }

    private fun canMoveInDirection(board: Board, move: Move): Boolean {
        val size = board.size
        val cells = board.cells

        // Try to make the move and see if it changes the board
        val newCells = Array(size) { r -> Array(size) { c -> cells[r][c] } }
        val score = if (move.isHorizontal()) {
            processHorizontalMove(board, newCells, move == Move.RIGHT)
        } else {
            processVerticalMove(board, newCells, move == Move.DOWN)
        }

        // A move is valid if either:
        // 1. The board state changed
        // 2. Some tiles were merged (score increased)
        return !newCells.contentDeepEquals(cells) || score > 0
    }

    private fun hasWon(board: Board): Boolean {
        return board.cells.any { row -> row.any { it >= Board.WINNING_VALUE } }
    }

    private fun hasValidMoves(board: Board): Boolean {
        return Move.entries.any { canMoveInDirection(board, it) }
    }
}
