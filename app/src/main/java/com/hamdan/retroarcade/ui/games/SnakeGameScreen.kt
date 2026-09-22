package com.hamdan.retroarcade.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hamdan.retroarcade.viewmodel.GameDataViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val GRID_SIZE     = 20
private const val INITIAL_SPEED = 200L

private enum class SnakeDir { UP, DOWN, LEFT, RIGHT }
private data class SnakeCell(val x: Int, val y: Int)

private fun randomFood(snake: List<SnakeCell>): SnakeCell {
    var cell: SnakeCell
    do { cell = SnakeCell((0 until GRID_SIZE).random(), (0 until GRID_SIZE).random()) }
    while (snake.contains(cell))
    return cell
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnakeGameScreen(
    onBack: () -> Unit,
    gameDataViewModel: GameDataViewModel? = null
) {
    // ── Persisted high score from ViewModel ────────────────────────────────────
    val persistedHigh by (gameDataViewModel?.snakeHigh ?: kotlinx.coroutines.flow.MutableStateFlow(0))
        .collectAsState()

    // ── Local game state ───────────────────────────────────────────────────────
    var snake     by remember { mutableStateOf(listOf(SnakeCell(10,10), SnakeCell(9,10), SnakeCell(8,10))) }
    var direction by remember { mutableStateOf(SnakeDir.RIGHT) }
    var nextDir   by remember { mutableStateOf(SnakeDir.RIGHT) }
    var food      by remember { mutableStateOf(randomFood(listOf(SnakeCell(10,10)))) }
    var score     by remember { mutableIntStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }
    var gameOver  by remember { mutableStateOf(false) }
    var gameJob   by remember { mutableStateOf<Job?>(null) }

    val displayBest = maxOf(persistedHigh, score)

    val primaryColor  = MaterialTheme.colorScheme.primary
    val surfaceColor  = MaterialTheme.colorScheme.surfaceVariant
    val bgColor       = MaterialTheme.colorScheme.background

    // ── Game loop ──────────────────────────────────────────────────────────────
    fun startLoop() {
        gameJob?.cancel()
        gameJob = CoroutineScope(Dispatchers.Main).launch {
            while (isRunning && !gameOver) {
                delay(INITIAL_SPEED - (score * 2L).coerceAtMost(150L))
                direction = nextDir
                val head    = snake.first()
                val newHead = when (direction) {
                    SnakeDir.UP    -> SnakeCell(head.x, (head.y - 1 + GRID_SIZE) % GRID_SIZE)
                    SnakeDir.DOWN  -> SnakeCell(head.x, (head.y + 1) % GRID_SIZE)
                    SnakeDir.LEFT  -> SnakeCell((head.x - 1 + GRID_SIZE) % GRID_SIZE, head.y)
                    SnakeDir.RIGHT -> SnakeCell((head.x + 1) % GRID_SIZE, head.y)
                }
                if (snake.contains(newHead)) {
                    gameOver  = true
                    isRunning = false
                    gameDataViewModel?.submitSnakeScore(score)
                    gameDataViewModel?.vibrateGameOver()
                    return@launch
                }
                val ate = newHead == food
                val newSnake = if (ate) listOf(newHead) + snake
                               else     (listOf(newHead) + snake).dropLast(1)
                snake = newSnake
                if (ate) {
                    score++
                    food = randomFood(newSnake)
                    gameDataViewModel?.vibrateLight()
                }
            }
        }
    }

    fun resetGame() {
        gameJob?.cancel()
        snake     = listOf(SnakeCell(10,10), SnakeCell(9,10), SnakeCell(8,10))
        direction = SnakeDir.RIGHT
        nextDir   = SnakeDir.RIGHT
        food      = randomFood(listOf(SnakeCell(10,10)))
        score     = 0
        gameOver  = false
        isRunning = true
        startLoop()
    }

    DisposableEffect(Unit) { onDispose { gameJob?.cancel() } }

    // ── UI ─────────────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "SNAKE",
                            style = MaterialTheme.typography.titleLarge.copy(color = primaryColor)
                        )
                        Text(
                            "SCORE: $score",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { gameJob?.cancel(); onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = primaryColor)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (gameOver) resetGame()
                        else {
                            isRunning = !isRunning
                            if (isRunning) startLoop() else gameJob?.cancel()
                        }
                    }) {
                        Icon(
                            imageVector = when {
                                gameOver  -> Icons.Default.Refresh
                                isRunning -> Icons.Default.Pause
                                else      -> Icons.Default.PlayArrow
                            },
                            contentDescription = "Play/Pause",
                            tint = primaryColor
                        )
                    }
                    IconButton(onClick = { resetGame() }) {
                        Icon(Icons.Default.Refresh, "Reset", tint = primaryColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Best score row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "SCORE: $score",
                    style = MaterialTheme.typography.labelMedium.copy(color = primaryColor)
                )
                Text(
                    "BEST: $displayBest",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Board ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .border(2.dp, primaryColor, RoundedCornerShape(8.dp))
                    .background(bgColor, RoundedCornerShape(8.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cellW = size.width  / GRID_SIZE
                    val cellH = size.height / GRID_SIZE

                    // Subtle grid dots
                    for (r in 1 until GRID_SIZE) for (c in 1 until GRID_SIZE) {
                        drawCircle(
                            color  = surfaceColor.copy(alpha = 0.15f),
                            radius = 1f,
                            center = Offset(c * cellW, r * cellH)
                        )
                    }

                    // Food with glow
                    val fx = food.x * cellW; val fy = food.y * cellH
                    drawCircle(
                        Color(0xFFFF4444).copy(alpha = 0.3f),
                        (cellW / 2f) * 1.6f,
                        Offset(fx + cellW / 2, fy + cellH / 2)
                    )
                    drawRoundRect(
                        color        = Color(0xFFFF4444),
                        topLeft      = Offset(fx + 2, fy + 2),
                        size         = Size(cellW - 4, cellH - 4),
                        cornerRadius = CornerRadius(4f)
                    )

                    // Snake with gradient body
                    snake.forEachIndexed { idx, cell ->
                        val alpha = if (idx == 0) 1f
                                    else (0.85f - idx.toFloat() / snake.size * 0.6f).coerceAtLeast(0.2f)
                        drawRoundRect(
                            color        = primaryColor.copy(alpha = alpha),
                            topLeft      = Offset(cell.x * cellW + 1, cell.y * cellH + 1),
                            size         = Size(cellW - 2, cellH - 2),
                            cornerRadius = CornerRadius(3f)
                        )
                        // Head glow
                        if (idx == 0) drawRoundRect(
                            color        = primaryColor.copy(alpha = 0.25f),
                            topLeft      = Offset(cell.x * cellW - 1, cell.y * cellH - 1),
                            size         = Size(cellW + 2, cellH + 2),
                            cornerRadius = CornerRadius(5f)
                        )
                    }
                }

                // ── Overlay ────────────────────────────────────────────────
                if (gameOver || (!isRunning && score == 0)) {
                    Box(
                        modifier         = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (gameOver) "GAME OVER" else "SNAKE",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = primaryColor, fontWeight = FontWeight.Bold
                                )
                            )
                            if (gameOver) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Score: $score",
                                    style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                                )
                                if (score > 0 && score >= persistedHigh) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "🏆 New Best!",
                                        style = MaterialTheme.typography.labelMedium.copy(color = primaryColor)
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (gameOver) "Tap ↺ to restart" else "Tap ▶ to start",
                                style     = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── D-Pad ──────────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SnakeDPadBtn(Icons.Default.KeyboardArrowUp, "Up") {
                    if (direction != SnakeDir.DOWN) nextDir = SnakeDir.UP
                    if (!isRunning && !gameOver) { isRunning = true; startLoop() }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    SnakeDPadBtn(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Left") {
                        if (direction != SnakeDir.RIGHT) nextDir = SnakeDir.LEFT
                        if (!isRunning && !gameOver) { isRunning = true; startLoop() }
                    }
                    Spacer(modifier = Modifier.size(52.dp))
                    SnakeDPadBtn(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Right") {
                        if (direction != SnakeDir.LEFT) nextDir = SnakeDir.RIGHT
                        if (!isRunning && !gameOver) { isRunning = true; startLoop() }
                    }
                }
                SnakeDPadBtn(Icons.Default.KeyboardArrowDown, "Down") {
                    if (direction != SnakeDir.UP) nextDir = SnakeDir.DOWN
                    if (!isRunning && !gameOver) { isRunning = true; startLoop() }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SnakeDPadBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    onClick: () -> Unit
) {
    Box(
        modifier         = Modifier
            .size(52.dp)
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, modifier = Modifier.fillMaxSize()) {
            Icon(icon, desc, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        }
    }
}
