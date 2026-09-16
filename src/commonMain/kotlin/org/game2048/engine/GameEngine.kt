package org.game2048.engine

import org.game2048.model.Board
import org.game2048.model.GameState
import org.game2048.model.Move

/**
 * Interface defining the core game operations for 2048.
 * This interface handles game state management, move processing, and game rules.
 */
interface GameEngine {
    /**
     * The current state of the game.
     */
    val currentState: GameState

    /**
     * Makes a move in the specified direction.
     * @param move The direction to move tiles
     * @return The new game state after the move
     */
    fun makeMove(move: Move): GameState

    /**
     * Checks if the specified move is valid in the current game state.
     * @param move The move to validate
     * @return true if the move is valid, false otherwise
     */
    fun isValidMove(move: Move): Boolean

    /**
     * Creates a new game with the specified board size.
     * @param size The size of the game board (default is 4)
     * @return The initial game state
     */
    fun newGame(size: Int = Board.DEFAULT_SIZE): GameState

    /**
     * Attempts to undo the last move if possible.
     * @return The previous game state if undo is available, null otherwise
     */
    fun undo(): GameState?

    /**
     * Continues the game after winning if desired.
     * @return The new game state with continueGame flag set
     */
    fun continueGame(): GameState

    companion object {
        /**
         * Number of initial tiles when starting a new game
         */
        const val INITIAL_TILES = 2

        /**
         * Probability of generating a tile with value 4 (vs 2)
         */
        const val PROBABILITY_OF_FOUR = 0.1
    }
}