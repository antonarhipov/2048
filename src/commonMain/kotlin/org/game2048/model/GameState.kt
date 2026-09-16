package org.game2048.model

/**
 * Represents the current state of the game.
 * This sealed class hierarchy defines all possible game states.
 *
 * @property board The current game board
 */
sealed class GameState(open val board: Board) {
    /**
     * Represents the state when the game is in progress.
     *
     * @property board The current game board
     * @property canUndo Whether an undo operation is available
     */
    data class Playing(
        override val board: Board,
        val canUndo: Boolean = false
    ) : GameState(board)

    /**
     * Represents the state when the player has won by reaching 2048.
     *
     * @property board The final game board
     * @property continueGame Whether the player chose to continue playing after winning
     */
    data class Won(
        override val board: Board,
        val continueGame: Boolean = false
    ) : GameState(board)

    /**
     * Represents the state when no more moves are possible.
     *
     * @property board The final game board
     */
    data class Lost(
        override val board: Board
    ) : GameState(board)
}