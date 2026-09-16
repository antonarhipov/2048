package org.game2048.model

/**
 * Represents a move direction in the game.
 * This enum defines all possible move directions and provides helper methods for movement calculations.
 */
enum class Move {
    UP, DOWN, LEFT, RIGHT;

    /**
     * Returns the row delta for this move direction.
     * @return -1 for UP, 1 for DOWN, 0 for LEFT/RIGHT
     */
    fun rowDelta(): Int = when (this) {
        UP -> -1
        DOWN -> 1
        else -> 0
    }

    /**
     * Returns the column delta for this move direction.
     * @return -1 for LEFT, 1 for RIGHT, 0 for UP/DOWN
     */
    fun columnDelta(): Int = when (this) {
        LEFT -> -1
        RIGHT -> 1
        else -> 0
    }

    /**
     * Returns whether this move is vertical (UP or DOWN).
     */
    fun isVertical(): Boolean = this == UP || this == DOWN

    /**
     * Returns whether this move is horizontal (LEFT or RIGHT).
     */
    fun isHorizontal(): Boolean = this == LEFT || this == RIGHT

    companion object {
        /**
         * Returns all possible move directions.
         */
            fun allMoves(): List<Move> = entries.toList()
    }
}