package org.game2048.model

import kotlin.test.*

class GameStateTest {
    private val emptyBoard = Board()
    private val boardWithScore = Board(score = 100)

    @Test
    fun `test Playing state creation`() {
        val state = GameState.Playing(emptyBoard)
        assertEquals(emptyBoard, state.board)
        assertFalse(state.canUndo)
    }

    @Test
    fun `test Playing state with undo available`() {
        val state = GameState.Playing(emptyBoard, canUndo = true)
        assertTrue(state.canUndo)
    }

    @Test
    fun `test Won state creation`() {
        val state = GameState.Won(boardWithScore)
        assertEquals(boardWithScore, state.board)
        assertFalse(state.continueGame)
    }

    @Test
    fun `test Won state with continue game`() {
        val state = GameState.Won(boardWithScore, continueGame = true)
        assertTrue(state.continueGame)
    }

    @Test
    fun `test Lost state creation`() {
        val state = GameState.Lost(boardWithScore)
        assertEquals(boardWithScore, state.board)
    }

    @Test
    fun `test state inheritance`() {
        val playingState = GameState.Playing(emptyBoard)
        val wonState = GameState.Won(emptyBoard)
        val lostState = GameState.Lost(emptyBoard)

        assertTrue(playingState is GameState)
        assertTrue(wonState is GameState)
        assertTrue(lostState is GameState)
    }

    @Test
    fun `test state equality`() {
        val state1 = GameState.Playing(emptyBoard)
        val state2 = GameState.Playing(emptyBoard)
        val state3 = GameState.Playing(boardWithScore)
        val wonState: GameState = GameState.Won(emptyBoard)

        assertEquals(state1, state2)
        assertNotEquals(state1, state3)
        assertNotEquals<GameState>(state1, wonState)
    }

    @Test
    fun `test state properties preservation`() {
        val playingState = GameState.Playing(boardWithScore, canUndo = true)
        assertEquals(100, playingState.board.score)
        assertTrue(playingState.canUndo)

        val wonState = GameState.Won(boardWithScore, continueGame = true)
        assertEquals(100, wonState.board.score)
        assertTrue(wonState.continueGame)

        val lostState = GameState.Lost(boardWithScore)
        assertEquals(100, lostState.board.score)
    }
}
