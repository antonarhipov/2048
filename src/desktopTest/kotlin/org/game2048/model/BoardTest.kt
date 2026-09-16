package org.game2048.model

import kotlin.test.*

class BoardTest {
    @Test
    fun `test board creation with default size`() {
        val board = Board()
        assertEquals(Board.DEFAULT_SIZE, board.size)
        assertEquals(0, board.score)
        assertTrue(board.cells.all { row -> row.all { it == 0 } })
    }

    @Test
    fun `test board creation with custom size`() {
        val size = 5
        val board = Board(size = size)
        assertEquals(size, board.size)
        assertEquals(size, board.cells.size)
        assertTrue(board.cells.all { row -> row.size == size })
    }

    @Test
    fun `test board creation with custom cells and score`() {
        val cells = Array(2) { row -> Array(2) { col -> (row * 2 + col + 1) * 2 } }
        val score = 100
        val board = Board(size = 2, cells = cells, score = score)
        
        assertEquals(2, board.size)
        assertEquals(score, board.score)
        assertEquals(2, cells[0][0])
        assertEquals(4, cells[0][1])
        assertEquals(6, cells[1][0])
        assertEquals(8, cells[1][1])
    }

    @Test
    fun `test board creation with invalid size throws exception`() {
        assertFailsWith<IllegalArgumentException> {
            Board(size = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            Board(size = -1)
        }
    }

    @Test
    fun `test board creation with mismatched cells dimensions throws exception`() {
        val invalidCells = Array(3) { Array(4) { 0 } }
        assertFailsWith<IllegalArgumentException> {
            Board(size = 4, cells = invalidCells)
        }
    }

    @Test
    fun `test board equals and hashCode`() {
        val board1 = Board(size = 2, cells = Array(2) { Array(2) { 0 } }, score = 10)
        val board2 = Board(size = 2, cells = Array(2) { Array(2) { 0 } }, score = 10)
        val board3 = Board(size = 2, cells = Array(2) { Array(2) { 1 } }, score = 10)

        assertEquals(board1, board2)
        assertEquals(board1.hashCode(), board2.hashCode())
        assertNotEquals(board1, board3)
        assertNotEquals(board1.hashCode(), board3.hashCode())
    }
}