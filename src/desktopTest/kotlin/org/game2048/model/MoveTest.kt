package org.game2048.model

import kotlin.test.*

class MoveTest {
    @Test
    fun `test row delta calculations`() {
        assertEquals(-1, Move.UP.rowDelta())
        assertEquals(1, Move.DOWN.rowDelta())
        assertEquals(0, Move.LEFT.rowDelta())
        assertEquals(0, Move.RIGHT.rowDelta())
    }

    @Test
    fun `test column delta calculations`() {
        assertEquals(0, Move.UP.columnDelta())
        assertEquals(0, Move.DOWN.columnDelta())
        assertEquals(-1, Move.LEFT.columnDelta())
        assertEquals(1, Move.RIGHT.columnDelta())
    }

    @Test
    fun `test vertical move detection`() {
        assertTrue(Move.UP.isVertical())
        assertTrue(Move.DOWN.isVertical())
        assertFalse(Move.LEFT.isVertical())
        assertFalse(Move.RIGHT.isVertical())
    }

    @Test
    fun `test horizontal move detection`() {
        assertFalse(Move.UP.isHorizontal())
        assertFalse(Move.DOWN.isHorizontal())
        assertTrue(Move.LEFT.isHorizontal())
        assertTrue(Move.RIGHT.isHorizontal())
    }

    @Test
    fun `test all moves list`() {
        val allMoves = Move.allMoves()
        assertEquals(4, allMoves.size)
        assertTrue(Move.UP in allMoves)
        assertTrue(Move.DOWN in allMoves)
        assertTrue(Move.LEFT in allMoves)
        assertTrue(Move.RIGHT in allMoves)
    }

    @Test
    fun `test opposite directions have opposite deltas`() {
        assertEquals(-(Move.UP.rowDelta()), Move.DOWN.rowDelta())
        assertEquals(-(Move.LEFT.columnDelta()), Move.RIGHT.columnDelta())
    }
}