package org.game2048

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import kotlinx.coroutines.launch
import org.game2048.engine.GameEngine
import org.game2048.engine.GameEngineImpl
import org.game2048.model.Board
import org.game2048.model.GameState
import org.game2048.model.Move

private val BoardBackground = Color(0xFFBBADA0)
private val TextBrown = Color(0xFF776E65)
private val AppBackground = Color(0xFFFAF8EF)
private val AccentBrown = Color(0xFF8F7A66)
private val WinGold = Color(0xFFF9BE02)

@Composable
fun Game() {
    var gameEngine by remember { mutableStateOf<GameEngine>(GameEngineImpl()) }
    var gameState by remember { mutableStateOf(gameEngine.newGame()) }

    MaterialTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            color = AppBackground
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "2048",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBrown
                    )
                    Text(
                        text = "Join the tiles, get to 2048!",
                        fontSize = 16.sp,
                        color = TextBrown
                    )
                    Text(
                        text = "Swipe, or use arrow keys / WASD",
                        fontSize = 14.sp,
                        color = TextBrown.copy(alpha = 0.7f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SCORE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextBrown
                        )
                        Text(
                            text = "${gameState.board.score}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextBrown
                        )
                        val status = when (val state = gameState) {
                            is GameState.Won -> if (state.continueGame) {
                                StatusText("Keep going!", WinGold)
                            } else {
                                StatusText("You won!", WinGold)
                            }
                            else -> null
                        }
                        status?.let { (text, color) ->
                            Text(
                                text = text,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }
                    }

                    GameControls(
                        onNewGame = { gameState = gameEngine.newGame() },
                        onUndo = { gameEngine.undo()?.let { gameState = it } },
                        onContinue = { gameState = gameEngine.continueGame() },
                        gameState = gameState,
                        canUndo = gameState is GameState.Playing && (gameState as GameState.Playing).canUndo
                    )
                }

                GameBoard(
                    gameState = gameState,
                    onMove = { move -> gameState = gameEngine.makeMove(move) }
                )
            }
        }
    }
}

@Composable
fun GameBoard(
    gameState: GameState,
    onMove: (Move) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var lastKeyPressed by remember { mutableStateOf<String?>(null) }
    var showKeyOverlay by remember { mutableStateOf(false) }
    var showGameOver by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 40.dp.toPx() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(gameState) {
        showGameOver = gameState is GameState.Lost
    }

    var isFocused by remember { mutableStateOf(false) }

    val keyOverlayAlpha by animateFloatAsState(
        targetValue = if (showKeyOverlay) 1f else 0f,
        animationSpec = tween(300),
        finishedListener = { if (!showKeyOverlay) lastKeyPressed = null }
    )

    val gameOverAlpha by animateFloatAsState(
        targetValue = if (showGameOver) 1f else 0f,
        animationSpec = tween(200)
    )

    val borderAlpha by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0f,
        animationSpec = tween(durationMillis = 200, easing = LinearEasing)
    )

    fun submitMove(move: Move) {
        lastKeyPressed = when (move) {
            Move.LEFT -> "←"
            Move.RIGHT -> "→"
            Move.UP -> "↑"
            Move.DOWN -> "↓"
        }
        showKeyOverlay = true
        scope.launch {
            onMove(move)
            kotlinx.coroutines.delay(200)
            showKeyOverlay = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(8.dp)
            .aspectRatio(1f)
            .background(BoardBackground, RoundedCornerShape(6.dp))
            .border(
                width = 2.dp,
                color = AccentBrown.copy(alpha = borderAlpha),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(4.dp)
            .focusRequester(focusRequester)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                val move = when (event.key) {
                    Key.DirectionLeft, Key.A -> Move.LEFT
                    Key.DirectionRight, Key.D -> Move.RIGHT
                    Key.DirectionUp, Key.W -> Move.UP
                    Key.DirectionDown, Key.S -> Move.DOWN
                    else -> null
                }
                if (move != null) {
                    submitMove(move)
                    true
                } else {
                    false
                }
            }
            .pointerInput(Unit) {
                var total = Offset.Zero
                detectDragGestures(
                    onDragStart = { total = Offset.Zero },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        total += dragAmount
                    },
                    onDragEnd = {
                        val move = when {
                            abs(total.x) < swipeThresholdPx && abs(total.y) < swipeThresholdPx -> null
                            abs(total.x) > abs(total.y) -> if (total.x > 0) Move.RIGHT else Move.LEFT
                            else -> if (total.y > 0) Move.DOWN else Move.UP
                        }
                        if (move != null) {
                            submitMove(move)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val board = gameState.board
                for (row in 0 until board.size) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (col in 0 until board.size) {
                            GameTile(
                                value = board.cells[row][col],
                                row = row,
                                col = col,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            if (lastKeyPressed != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TextBrown.copy(alpha = keyOverlayAlpha * 0.3f))
                            .border(
                                width = 2.dp,
                                color = TextBrown.copy(alpha = keyOverlayAlpha * 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = lastKeyPressed!!,
                            fontSize = 40.sp,
                            color = Color.White.copy(alpha = keyOverlayAlpha),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (showGameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF000000).copy(alpha = gameOverAlpha * 0.5f))
                        .clickable { showGameOver = false },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE74C3C).copy(alpha = gameOverAlpha * 0.9f))
                            .border(
                                width = 3.dp,
                                color = Color.White.copy(alpha = gameOverAlpha * 0.8f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Game Over!",
                            fontSize = 32.sp,
                            color = Color.White.copy(alpha = gameOverAlpha),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameTile(
    value: Int,
    row: Int,
    col: Int,
    modifier: Modifier = Modifier
) {
    var isNew by remember { mutableStateOf(true) }
    var isMerged by remember { mutableStateOf(false) }
    var previousRow by remember(row) { mutableStateOf(row) }
    var previousCol by remember(col) { mutableStateOf(col) }
    val scope = rememberCoroutineScope()

    val tileSize = 96.dp
    val moveAnimDuration = 45
    val mergeAnimDuration = 60
    val density = LocalDensity.current
    val tileSizePx = with(density) { tileSize.toPx() }

    var targetOffsetX by remember { mutableStateOf(0f) }
    var targetOffsetY by remember { mutableStateOf(0f) }

    val offsetX by animateFloatAsState(
        targetValue = targetOffsetX,
        animationSpec = tween(durationMillis = moveAnimDuration, easing = LinearEasing)
    )

    val offsetY by animateFloatAsState(
        targetValue = targetOffsetY,
        animationSpec = tween(durationMillis = moveAnimDuration, easing = LinearEasing)
    )

    LaunchedEffect(row, col) {
        if (row != previousRow || col != previousCol) {
            targetOffsetX = (previousCol - col) * tileSizePx
            targetOffsetY = (previousRow - row) * tileSizePx
            kotlinx.coroutines.delay(4)
            targetOffsetX = 0f
            targetOffsetY = 0f
            previousRow = row
            previousCol = col
        }
    }

    LaunchedEffect(value) {
        if (value > 0) {
            isNew = true
            isMerged = true
            scope.launch {
                kotlinx.coroutines.delay((moveAnimDuration + 5).toLong())
                isNew = false
                kotlinx.coroutines.delay(10)
                isMerged = false
            }
        }
    }

    val scale by animateFloatAsState(
        targetValue = when {
            isMerged -> 1.08f
            isNew && value > 0 -> 0.5f
            else -> 1f
        },
        animationSpec = tween(
            durationMillis = when {
                isNew -> moveAnimDuration + 20
                isMerged -> mergeAnimDuration
                else -> moveAnimDuration
            },
            easing = when {
                isNew -> FastOutSlowInEasing
                isMerged -> FastOutLinearInEasing
                else -> LinearEasing
            }
        )
    )

    val alpha by animateFloatAsState(
        targetValue = if (value > 0) 1f else 0f,
        animationSpec = tween(
            durationMillis = moveAnimDuration + 10,
            easing = FastOutLinearInEasing
        )
    )

    Box {
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(4.dp))
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                    translationX = offsetX
                    translationY = offsetY
                }
                .background(getTileColor(value))
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            if (value > 0) {
                val fontSize = when {
                    value >= 1024 -> 20.sp
                    value >= 100 -> 24.sp
                    else -> 28.sp
                }
                Text(
                    text = value.toString(),
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = if (value > 4) Color.White else TextBrown
                )
            }
        }

        if (value == Board.WINNING_VALUE) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0x88F9BE02), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "2048!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun GameControls(
    onNewGame: () -> Unit,
    onUndo: () -> Unit,
    onContinue: () -> Unit,
    gameState: GameState,
    canUndo: Boolean
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = onNewGame,
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBrown,
                contentColor = Color.White
            )
        ) {
            Text(text = "New Game", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        if (gameState is GameState.Won && !gameState.continueGame) {
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(
                    containerColor = WinGold,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Continue", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        Button(
            onClick = onUndo,
            enabled = canUndo,
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBrown,
                contentColor = Color.White
            )
        ) {
            Text(text = "Undo", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private data class StatusText(val text: String, val color: Color)

private fun getTileColor(value: Int): Color = when (value) {
    0 -> Color(0xFFCDC1B4)
    2 -> Color(0xFFEEE4DA)
    4 -> Color(0xFFEDE0C8)
    8 -> Color(0xFFF2B179)
    16 -> Color(0xFFF59563)
    32 -> Color(0xFFF67C5F)
    64 -> Color(0xFFF65E3B)
    128 -> Color(0xFFEDCF72)
    256 -> Color(0xFFEDCC61)
    512 -> Color(0xFFEDC850)
    1024 -> Color(0xFFEDC53F)
    2048 -> Color(0xFFEDC22E)
    else -> Color(0xFF3C3A32)
}
