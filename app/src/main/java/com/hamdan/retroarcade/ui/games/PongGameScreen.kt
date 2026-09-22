package com.hamdan.retroarcade.ui.games

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hamdan.retroarcade.viewmodel.GameDataViewModel
import kotlinx.coroutines.delay
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerInputChange
import kotlin.math.abs

// ── Constants ──────────────────────────────────────────────────────────────────
private const val PW       = 0.025f   // paddle width  (normalised)
private const val PH       = 0.18f    // paddle height (normalised)
private const val BR       = 0.018f   // ball radius   (normalised)
private const val WIN_SCORE = 7

private enum class PongMode(val label: String) { VS_AI("vs AI"), VS_FRIEND("vs Friend") }
private enum class PongDiff(val label: String, val speed: Float, val predict: Boolean) {
    EASY  ("Easy",   0.004f, false),
    MEDIUM("Medium", 0.007f, false),
    HARD  ("Hard",   0.011f, true )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PongGameScreen(
    onBack: () -> Unit,
    gameDataViewModel: GameDataViewModel? = null
) {
    // ── Game settings state ────────────────────────────────────────────────────
    var mode       by remember { mutableStateOf(PongMode.VS_AI) }
    var difficulty by remember { mutableStateOf(PongDiff.MEDIUM) }

    // ── Game physics state ─────────────────────────────────────────────────────
    var leftY    by remember { mutableFloatStateOf(0.5f) }   // left paddle centre
    var rightY   by remember { mutableFloatStateOf(0.5f) }   // right paddle centre
    var ballX    by remember { mutableFloatStateOf(0.5f) }
    var ballY    by remember { mutableFloatStateOf(0.5f) }
    var ballVX   by remember { mutableFloatStateOf(0.013f) }
    var ballVY   by remember { mutableFloatStateOf(0.009f) }
    var leftScore  by remember { mutableIntStateOf(0) }
    var rightScore by remember { mutableIntStateOf(0) }
    var isRunning  by remember { mutableStateOf(false) }
    var gameOver   by remember { mutableStateOf(false) }
    var winner     by remember { mutableStateOf("") }

    // ── Touch tracking for 2-player split ─────────────────────────────────────
    // We store separate finger Ys for left and right halves
    var leftDragY  by remember { mutableFloatStateOf(-1f) }   // -1 = no touch
    var rightDragY by remember { mutableFloatStateOf(-1f) }

    val primary = MaterialTheme.colorScheme.primary

    fun resetBall(dir: Float = 1f) {
        ballX = 0.5f; ballY = 0.5f
        ballVX = 0.013f * dir
        ballVY = listOf(-0.009f, 0.009f).random()
    }

    fun resetAll() {
        leftY = 0.5f; rightY = 0.5f
        leftScore = 0; rightScore = 0
        gameOver = false; winner = ""
        resetBall(); isRunning = true
    }

    // ── Game loop ──────────────────────────────────────────────────────────────
    LaunchedEffect(isRunning, mode, difficulty) {
        while (isRunning && !gameOver) {
            delay(16L)

            // Move ball
            ballX += ballVX
            ballY += ballVY

            // Wall bounce
            if (ballY - BR <= 0f) { ballY = BR; ballVY = -ballVY; gameDataViewModel?.vibrateLight() }
            if (ballY + BR >= 1f) { ballY = 1f - BR; ballVY = -ballVY; gameDataViewModel?.vibrateLight() }

            // ── Left paddle movement ───────────────────────────────────────
            // Always player-controlled (left half drag)
            if (leftDragY >= 0f) {
                leftY = leftDragY.coerceIn(PH / 2, 1f - PH / 2)
            }

            // ── Right paddle ───────────────────────────────────────────────
            when (mode) {
                PongMode.VS_AI -> {
                    // AI logic
                    val target = if (difficulty.predict) {
                        // Predict where ball will be when it reaches right side
                        predictBallY(ballX, ballY, ballVX, ballVY)
                    } else ballY

                    val diff = target - rightY
                    val move = difficulty.speed * if (difficulty == PongDiff.EASY) {
                        // Add jitter on Easy
                        if ((0..5).random() == 0) (0..1).random().toFloat() * 0.5f else 1f
                    } else 1f

                    rightY = if (diff > 0) (rightY + move).coerceAtMost(1f - PH / 2)
                             else          (rightY - move).coerceAtLeast(PH / 2)
                }
                PongMode.VS_FRIEND -> {
                    // Right half drag
                    if (rightDragY >= 0f) {
                        rightY = rightDragY.coerceIn(PH / 2, 1f - PH / 2)
                    }
                }
            }

            // ── Paddle collisions ──────────────────────────────────────────
            // Left paddle (x ≈ 0.05)
            if (ballX - BR <= 0.055f && ballVX < 0 && ballY in (leftY - PH/2)..(leftY + PH/2)) {
                ballVX = -ballVX * 1.04f
                val offset = (ballY - leftY) / (PH / 2)
                ballVY = offset * 0.016f
                gameDataViewModel?.vibrateLight()
            }
            // Right paddle (x ≈ 0.945)
            if (ballX + BR >= 0.945f && ballVX > 0 && ballY in (rightY - PH/2)..(rightY + PH/2)) {
                ballVX = -ballVX * 1.04f
                val offset = (ballY - rightY) / (PH / 2)
                ballVY = offset * 0.016f
                gameDataViewModel?.vibrateLight()
            }

            // Clamp speed
            ballVX = ballVX.coerceIn(-0.032f, 0.032f)
            ballVY = ballVY.coerceIn(-0.026f, 0.026f)

            // ── Scoring ────────────────────────────────────────────────────
            if (ballX < 0f) {
                rightScore++
                gameDataViewModel?.vibrateMedium()
                if (rightScore >= WIN_SCORE) {
                    val w = if (mode == PongMode.VS_AI) "AI WINS 🤖" else "Player 2 Wins! 🎉"
                    winner = w; gameOver = true; isRunning = false
                    gameDataViewModel?.vibrateGameOver()
                } else resetBall(1f)
            }
            if (ballX > 1f) {
                leftScore++
                gameDataViewModel?.vibrateMedium()
                if (leftScore >= WIN_SCORE) {
                    val w = if (mode == PongMode.VS_AI) "YOU WIN! 🎉" else "Player 1 Wins! 🎉"
                    winner = w; gameOver = true; isRunning = false
                    gameDataViewModel?.vibrateSuccess()
                    if (mode == PongMode.VS_AI) {
                        val shutout = rightScore == 0
                        gameDataViewModel?.submitPongWin(difficulty.name, shutout)
                    }
                } else resetBall(-1f)
            }
        }
    }

    DisposableEffect(Unit) { onDispose { isRunning = false } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("PONG", style = MaterialTheme.typography.titleLarge.copy(color = primary))
                        Text("First to $WIN_SCORE", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { isRunning = false; onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = primary)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (gameOver) resetAll() else isRunning = !isRunning
                    }) {
                        Icon(
                            imageVector = when { gameOver -> Icons.Default.Refresh; isRunning -> Icons.Default.Pause; else -> Icons.Default.PlayArrow },
                            contentDescription = "Toggle", tint = primary
                        )
                    }
                    IconButton(onClick = { resetAll() }) {
                        Icon(Icons.Default.Refresh, "Reset", tint = primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier            = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // ── Mode selector ──────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PongMode.entries.forEach { m ->
                    val sel = mode == m
                    Box(
                        modifier         = Modifier
                            .weight(1f).height(38.dp)
                            .background(if (sel) primary else MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                            .border(1.dp, primary.copy(alpha = if (sel) 0f else 0.3f), RoundedCornerShape(10.dp))
                            .clickable { mode = m; resetAll() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector        = if (m == PongMode.VS_AI) Icons.Default.SmartToy else Icons.Default.Person,
                                contentDescription = m.label,
                                tint               = if (sel) MaterialTheme.colorScheme.onPrimary else primary,
                                modifier           = Modifier.size(14.dp)
                            )
                            Text(
                                m.label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color      = if (sel) MaterialTheme.colorScheme.onPrimary else primary,
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }

            // ── Difficulty (VS AI only) ────────────────────────────────────
            AnimatedVisibility(visible = mode == PongMode.VS_AI) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PongDiff.entries.forEach { diff ->
                        val sel = difficulty == diff
                        val dc = when (diff) {
                            PongDiff.EASY   -> Color(0xFF39FF14)
                            PongDiff.MEDIUM -> Color(0xFFFFAB00)
                            PongDiff.HARD   -> Color(0xFFFF2D78)
                        }
                        Box(
                            modifier         = Modifier
                                .weight(1f).height(32.dp)
                                .background(if (sel) dc.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .border(1.dp, dc.copy(alpha = if (sel) 0.8f else 0.25f), RoundedCornerShape(8.dp))
                                .clickable { difficulty = diff; resetAll() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                diff.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color      = if (sel) dc else dc.copy(alpha = 0.6f),
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }

            // ── Score display ──────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (mode == PongMode.VS_FRIEND) "P1" else "YOU",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Text("$leftScore", style = MaterialTheme.typography.headlineMedium.copy(color = primary, fontWeight = FontWeight.Bold))
                }
                Text(":", style = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (mode == PongMode.VS_FRIEND) "P2" else "AI",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Text("$rightScore", style = MaterialTheme.typography.headlineMedium.copy(
                        color = if (mode == PongMode.VS_AI) Color(0xFFFF2D78) else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    ))
                }
            }

            // ── Game canvas ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF050510), RoundedCornerShape(12.dp))
                    .border(1.dp, primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .pointerInput(mode) {
                        awaitEachGesture {
                            // Track all active pointers and route left/right half
                            val trackedPointers = mutableMapOf<Long, PointerInputChange>()
                            val firstDown = awaitFirstDown()
                            trackedPointers[firstDown.id.value] = firstDown
                            routeTouch(firstDown, size.width, size.height, mode == PongMode.VS_FRIEND,
                                setLeft  = { leftDragY  = it },
                                setRight = { rightDragY = it }
                            )
                            if (!isRunning && !gameOver) isRunning = true

                            do {
                                val event = awaitPointerEvent()
                                event.changes.forEach { change ->
                                    change.consume()
                                    trackedPointers[change.id.value] = change
                                    routeTouch(change, size.width, size.height, mode == PongMode.VS_FRIEND,
                                        setLeft  = { leftDragY  = it },
                                        setRight = { rightDragY = it }
                                    )
                                }
                                // Clear released pointers
                                val released = event.changes.filter { !it.pressed }
                                released.forEach { r ->
                                    trackedPointers.remove(r.id.value)
                                    // If was left touch, clear left drag
                                    if (r.position.x < size.width / 2) leftDragY  = -1f
                                    else                                 rightDragY = -1f
                                }
                            } while (event.changes.any { it.pressed })
                            leftDragY  = -1f
                            rightDragY = -1f
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width; val h = size.height
                    val pw = PW * w; val ph = PH * h; val br = BR * w

                    // Centre dashed line
                    for (i in 0..14) {
                        if (i % 2 == 0) drawLine(
                            Color.White.copy(alpha = 0.12f),
                            Offset(w / 2, i * h / 14f),
                            Offset(w / 2, (i + 0.7f) * h / 14f),
                            2f
                        )
                    }

                    // Left paddle
                    drawRoundRect(primary, Offset(w * 0.03f, leftY * h - ph / 2), Size(pw, ph), CornerRadius(4f))
                    // Left paddle glow
                    drawRoundRect(primary.copy(alpha = 0.15f), Offset(w * 0.02f, leftY * h - ph / 2 - 4), Size(pw + 8, ph + 8), CornerRadius(6f))

                    // Right paddle
                    val rightColor = if (mode == PongMode.VS_FRIEND) Color(0xFFFFAB00) else Color.White.copy(alpha = 0.8f)
                    drawRoundRect(rightColor, Offset(w * 0.97f - pw, rightY * h - ph / 2), Size(pw, ph), CornerRadius(4f))
                    drawRoundRect(rightColor.copy(alpha = 0.15f), Offset(w * 0.97f - pw - 4, rightY * h - ph / 2 - 4), Size(pw + 8, ph + 8), CornerRadius(6f))

                    // Ball
                    drawCircle(primary, br, Offset(ballX * w, ballY * h))
                    // Ball trail glow
                    drawCircle(primary.copy(alpha = 0.3f), br * 1.8f, Offset(ballX * w, ballY * h))
                    drawCircle(primary.copy(alpha = 0.1f), br * 3f,   Offset(ballX * w, ballY * h))

                    // Touch zone hint for 2-player
                    if (mode == PongMode.VS_FRIEND) {
                        drawLine(Color.White.copy(alpha = 0.05f), Offset(w / 2, 0f), Offset(w / 2, h), 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)))
                    }
                }

                // Touch hint labels (2-player)
                if (mode == PongMode.VS_FRIEND && !isRunning && leftScore == 0 && rightScore == 0) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(Modifier.weight(1f).fillMaxHeight(), Alignment.Center) {
                            Text("P1\nDrag here", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.3f)), textAlign = TextAlign.Center)
                        }
                        Box(Modifier.weight(1f).fillMaxHeight(), Alignment.Center) {
                            Text("P2\nDrag here", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.3f)), textAlign = TextAlign.Center)
                        }
                    }
                }

                // Start / Game Over overlay
                this@Column.AnimatedVisibility(
                    visible = gameOver || (!isRunning && leftScore == 0 && rightScore == 0),
                    enter   = fadeIn(), exit = fadeOut()
                ) {
                    Box(
                        modifier         = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                if (gameOver) winner else "PONG",
                                style = MaterialTheme.typography.headlineLarge.copy(color = primary, fontWeight = FontWeight.Bold)
                            )
                            if (!gameOver && mode == PongMode.VS_FRIEND) {
                                Text("Touch left half = P1  •  Touch right half = P2",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f)),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                            Text(
                                if (gameOver) "Tap ↺ to play again" else "Touch to start",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // ── Controls hint ──────────────────────────────────────────────
            Text(
                text  = if (mode == PongMode.VS_FRIEND)
                    "P1: drag left half  •  P2: drag right half"
                else
                    "Drag anywhere on screen to move your paddle",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))
        }
    }
}

// ── Helper: route touch coordinates to left or right paddle ───────────────────
private fun routeTouch(
    change: PointerInputChange,
    width: Int,
    height: Int,
    isTwoPlayer: Boolean,
    setLeft:  (Float) -> Unit,
    setRight: (Float) -> Unit
) {
    val normY = change.position.y / height
    if (isTwoPlayer) {
        if (change.position.x < width / 2) setLeft(normY)
        else                                setRight(normY)
    } else {
        // Single player: drag anywhere moves left paddle
        setLeft(normY)
    }
}

// ── AI: predict ball Y at right paddle X ──────────────────────────────────────
private fun predictBallY(bx: Float, by: Float, vx: Float, vy: Float): Float {
    if (vx <= 0f) return by   // ball going away — hold position
    var x = bx; var y = by; var dyv = vy
    var steps = 0
    while (x < 0.94f && steps < 120) {
        x += vx; y += dyv
        if (y < 0f) { y = -y; dyv = -dyv }
        if (y > 1f) { y = 2f - y; dyv = -dyv }
        steps++
    }
    return y
}
