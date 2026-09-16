package org.game2048

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    val windowState = WindowState(size = DpSize(550.dp, 700.dp))

    Window(
        onCloseRequest = ::exitApplication,
        title = "2048 Game",
        state = windowState,
        resizable = true
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .sizeIn(minWidth = 400.dp, minHeight = 500.dp)
        ) {
            Game()
        }
    }
}
