package org.game2048.engine

import org.game2048.model.Board
import org.game2048.model.GameState
import org.game2048.model.Move
import kotlin.test.*

class GameEngineImplTest {
    private lateinit var engine: GameEngine

    @BeforeTest
    fun setup() {
        engine = GameEngineImpl()
    }

    private fun initializeBoard(cells: Array<Array<Int>>) {
        engine = GameEngineImpl()
        engine::class.java.getDeclaredField("_currentState").apply {
            isAccessible = true
            set(engine, GameState.Playing(Board(size = cells.size, cells = cells)))
        }
    }

    @Test
    fun `test new game initialization`() {
        val state = engine.newGame()
        assertTrue(state is GameState.Playing)
        assertEquals(Board.DEFAULT_SIZE, state.board.size)

        // Check that exactly two tiles are placed
        val nonEmptyTiles = state.board.cells.flatten().count { it != 0 }
        assertEquals(GameEngine.INITIAL_TILES, nonEmptyTiles)

        // Check that placed tiles are either 2 or 4
        assertTrue(state.board.cells.flatten().all { it == 0 || it == 2 || it == 4 })
    }

    @Test
    @Ignore
    fun `test horizontal merge`() {
        val cells = arrayOf(
            arrayOf(2, 2, 0, 0),
            arrayOf(2, 0, 2, 0),
            arrayOf(4, 0, 4, 0),
            arrayOf(2, 2, 2, 2)
        )
        initializeBoard(cells)

        val state = engine.makeMove(Move.LEFT)
        val newCells = state.board.cells

        // First row: 2,2,0,0 -> 4,0,0,x
        assertEquals(4, newCells[0][0])
        assertEquals(0, newCells[0][1])

        // Second row: 2,0,2,0 -> 4,0,0,x
        assertEquals(4, newCells[1][0])
        assertEquals(0, newCells[1][1])

        // Third row: 4,0,4,0 -> 8,0,0,x
        assertEquals(8, newCells[2][0])
        assertEquals(0, newCells[2][1])

        // Fourth row: 2,2,2,2 -> 4,4,0,x
        assertEquals(4, newCells[3][0])
        assertEquals(4, newCells[3][1])
        assertEquals(0, newCells[3][2])
    }

    @Test
    @Ignore
    fun `test vertical merge`() {
        val cells = arrayOf(
            arrayOf(2, 0, 4, 2),
            arrayOf(2, 0, 0, 2),
            arrayOf(0, 0, 4, 2),
            arrayOf(0, 0, 0, 2)
        )
        initializeBoard(cells)

        val state = engine.makeMove(Move.UP)
        val newCells = state.board.cells

        // First column: 2,2,0,0 -> 4,0,0,x
        assertEquals(4, newCells[0][0])
        assertEquals(0, newCells[1][0])

        // Fourth column: 2,2,2,2 -> 4,4,0,x
        assertEquals(4, newCells[0][3])
        assertEquals(4, newCells[1][3])
        assertEquals(0, newCells[2][3])
    }

    @Test
    fun `test score calculation`() {
        val cells = arrayOf(
            arrayOf(2, 2, 2, 2),
            arrayOf(0, 0, 0, 0),
            arrayOf(0, 0, 0, 0),
            arrayOf(0, 0, 0, 0)
        )
        initializeBoard(cells)

        val state = engine.makeMove(Move.LEFT)
        assertEquals(8, state.board.score) // Two merges of 2+2=4, total score increase = 8
    }

    @Test
    fun `test win condition`() {
        val cells = arrayOf(
            arrayOf(1024, 1024, 0, 0),
            arrayOf(0, 0, 0, 0),
            arrayOf(0, 0, 0, 0),
            arrayOf(0, 0, 0, 0)
        )
        initializeBoard(cells)

        val state = engine.makeMove(Move.LEFT)
        assertTrue(state is GameState.Won)
        assertEquals(2048, state.board.cells[0][0])
    }

    @Test
    fun `test continue after win`() {
        val cells = arrayOf(
            arrayOf(1024, 1024, 0, 0),
            arrayOf(0, 0, 0, 0),
            arrayOf(0, 0, 0, 0),
            arrayOf(0, 0, 0, 0)
        )
        initializeBoard(cells)

        var state = engine.makeMove(Move.LEFT)
        assertTrue(state is GameState.Won)

        state = engine.continueGame()
        assertTrue(state is GameState.Won)
        assertTrue((state as GameState.Won).continueGame)
    }

    @Test
    fun `test game over condition`() {
        val cells = arrayOf(
            arrayOf(4, 2, 4, 2),
            arrayOf(2, 4, 2, 4),
            arrayOf(4, 2, 4, 2),
            arrayOf(2, 4, 2, 4)
        )
        initializeBoard(cells)

        // Verify no moves are possible in any direction
        assertFalse(engine.isValidMove(Move.LEFT))
        assertFalse(engine.isValidMove(Move.RIGHT))
        assertFalse(engine.isValidMove(Move.UP))
        assertFalse(engine.isValidMove(Move.DOWN))

        // Try to make a move
        val state = engine.makeMove(Move.LEFT)

        // Verify game is lost
        assertTrue(state is GameState.Lost)
        assertEquals(cells, state.board.cells)  // Board should remain unchanged
    }

    @Test
    fun `test undo functionality`() {
        val state = engine.newGame()
        val initialBoard = state.board

        engine.makeMove(Move.LEFT)
        val undoState = engine.undo()

        assertNotNull(undoState)
        assertEquals(initialBoard, undoState.board)

        val secondUndo = engine.undo()
        assertNull(secondUndo) // Can't undo more than once
    }
}
