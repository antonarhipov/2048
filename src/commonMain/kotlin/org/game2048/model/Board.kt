package org.game2048.model

/**
 * Represents the game board for 2048.
 * This immutable data class holds the current state of the game board.
 *
 * @property size The size of the board (number of cells in each row/column)
 * @property cells The 2D array representing the board's cells
 * @property score The current game score
 */
data class Board(
    val size: Int = DEFAULT_SIZE,
    val cells: Array<Array<Int>> = createCells(size),
    val score: Int = 0
) {
    init {
        require(cells.size == size && cells.all { it.size == size }) {
            "Cells array dimensions must match the board size"
        }
    }

    companion object {
        const val DEFAULT_SIZE = 4
        const val WINNING_VALUE = 2048

        private fun createCells(size: Int): Array<Array<Int>> {
            require(size > 0) { "Board size must be positive" }
            return Array(size) { Array(size) { 0 } }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Board) return false

        if (size != other.size) return false
        if (!cells.contentDeepEquals(other.cells)) return false
        if (score != other.score) return false

        return true
    }

    override fun hashCode(): Int {
        var result = size
        result = 31 * result + cells.contentDeepHashCode()
        result = 31 * result + score
        return result
    }
}
