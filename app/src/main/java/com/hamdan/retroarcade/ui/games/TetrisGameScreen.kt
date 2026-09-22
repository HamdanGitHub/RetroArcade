package com.hamdan.retroarcade.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Board dims ─────────────────────────────────────────────────────────────────
private const val COLS = 10
private const val ROWS = 20

// ── Tetromino definitions [shape][rotation][cells] ────────────────────────────
private val PIECES: List<List<List<Pair<Int,Int>>>> = listOf(
    // I
    listOf(listOf(0 to 1, 1 to 1, 2 to 1, 3 to 1), listOf(1 to 0, 1 to 1, 1 to 2, 1 to 3)),
    // O
    listOf(listOf(0 to 0, 1 to 0, 0 to 1, 1 to 1)),
    // T
    listOf(
        listOf(1 to 0, 0 to 1, 1 to 1, 2 to 1),
        listOf(1 to 0, 1 to 1, 2 to 1, 1 to 2),
        listOf(0 to 1, 1 to 1, 2 to 1, 1 to 2),
        listOf(1 to 0, 0 to 1, 1 to 1, 1 to 2)
    ),
    // S
    listOf(listOf(1 to 0, 2 to 0, 0 to 1, 1 to 1), listOf(0 to 0, 0 to 1, 1 to 1, 1 to 2)),
    // Z
    listOf(listOf(0 to 0, 1 to 0, 1 to 1, 2 to 1), listOf(1 to 0, 0 to 1, 1 to 1, 0 to 2)),
    // J
    listOf(
        listOf(0 to 0, 0 to 1, 1 to 1, 2 to 1),
        listOf(1 to 0, 2 to 0, 1 to 1, 1 to 2),
        listOf(0 to 1, 1 to 1, 2 to 1, 2 to 2),
        listOf(1 to 0, 1 to 1, 0 to 2, 1 to 2)
    ),
    // L
    listOf(
        listOf(2 to 0, 0 to 1, 1 to 1, 2 to 1),
        listOf(1 to 0, 1 to 1, 1 to 2, 2 to 2),
        listOf(0 to 1, 1 to 1, 2 to 1, 0 to 2),
        listOf(0 to 0, 1 to 0, 1 to 1, 1 to 2)
    )
)

private val PIECE_COLORS = listOf(
    Color(0xFF00BFFF), // I - cyan
    Color(0xFFFFD700), // O - yellow
    Color(0xFFBB86FC), // T - purple
    Color(0xFF39FF14), // S - green
    Color(0xFFFF4444), // Z - red
    Color(0xFF0000FF), // J - blue
    Color(0xFFFF8C00)  // L - orange
)

private data class Piece(val type: Int, val rot: Int, val x: Int, val y: Int) {
    val cells get() = PIECES[type][rot % PIECES[type].size].map { (dx, dy) -> (x + dx) to (y + dy) }
}

private fun emptyBoard() = Array(ROWS) { IntArray(COLS) { -1 } }

private fun randomPiece() = Piece(
    type = (PIECES.indices).random(),
    rot  = 0,
    x    = 3,
    y    = 0
)

private fun isValid(piece: Piece, board: Array<IntArray>): Boolean =
    piece.cells.all { (cx, cy) ->
        cx in 0 until COLS && cy in 0 until ROWS && board[cy][cx] == -1
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TetrisGameScreen(
    onBack: () -> Unit,
    gameDataViewModel: com.hamdan.retroarcade.viewmodel.GameDataViewModel? = null
) {

    var board      by remember { mutableStateOf(emptyBoard()) }
    var current    by remember { mutableStateOf(randomPiece()) }
    var next       by remember { mutableStateOf(randomPiece()) }
    var score      by remember { mutableIntStateOf(0) }
    var lines      by remember { mutableIntStateOf(0) }
    var level      by remember { mutableIntStateOf(1) }
    var highScore  by remember { mutableIntStateOf(0) }
    var maxCombo   by remember { mutableIntStateOf(0) }
    var isRunning  by remember { mutableStateOf(false) }
    var gameOver   by remember { mutableStateOf(false) }
    var gameJob    by remember { mutableStateOf<Job?>(null) }

    val primary = MaterialTheme.colorScheme.primary

    fun lockPiece(piece: Piece, b: Array<IntArray>): Array<IntArray> {
        val nb = b.map { it.clone() }.toTypedArray()
        piece.cells.forEach { (cx, cy) -> if (cy in 0 until ROWS && cx in 0 until COLS) nb[cy][cx] = piece.type }
        return nb
    }

    fun clearLines(b: Array<IntArray>): Pair<Array<IntArray>, Int> {
        val kept   = b.filter { row -> row.any { it == -1 } }
        val cleared = ROWS - kept.size
        val nb     = Array(cleared) { IntArray(COLS) { -1 } } + kept.map { it.clone() }.toTypedArray()
        return nb to cleared
    }

    fun scoreFor(c: Int) = when (c) { 1 -> 100; 2 -> 300; 3 -> 500; 4 -> 800; else -> 0 } * level

    fun startLoop() {
        gameJob?.cancel()
        gameJob = CoroutineScope(Dispatchers.Main).launch {
            while (isRunning && !gameOver) {
                val speed = (800L - (level - 1) * 60L).coerceAtLeast(80L)
                delay(speed)
                val dropped = current.copy(y = current.y + 1)
                if (isValid(dropped, board)) {
                    current = dropped
                } else {
                    val nb = lockPiece(current, board)
                    val (clearedBoard, count) = clearLines(nb)
                    board   = clearedBoard
                    score  += scoreFor(count)
                    lines  += count
                    level   = lines / 10 + 1
                    if (count > maxCombo) maxCombo = count
                    if (score > highScore) highScore = score
                    if (count > 0) gameDataViewModel?.vibrateLight()
                    val np = next
                    if (!isValid(np, board)) {
                        gameOver  = true
                        isRunning = false
                        gameDataViewModel?.submitTetrisScore(score, lines, maxCombo, level)
                        gameDataViewModel?.vibrateGameOver()
                        return@launch
                    }
                    current = np
                    next    = randomPiece()
                }
            }
        }
    }

    fun resetGame() {
        gameJob?.cancel()
        board     = emptyBoard()
        current   = randomPiece()
        next      = randomPiece()
        score     = 0
        lines     = 0
        level     = 1
        maxCombo  = 0
        gameOver  = false
        isRunning = true
        startLoop()
    }

    fun move(dx: Int) {
        if (!isRunning) return
        val moved = current.copy(x = current.x + dx)
        if (isValid(moved, board)) current = moved
    }

    fun rotate() {
        if (!isRunning) return
        val rot = current.copy(rot = (current.rot + 1) % PIECES[current.type].size)
        if (isValid(rot, board)) current = rot
    }

    fun softDrop() {
        if (!isRunning) return
        val dropped = current.copy(y = current.y + 1)
        if (isValid(dropped, board)) current = dropped
    }

    fun hardDrop() {
        if (!isRunning) return
        var p = current
        while (isValid(p.copy(y = p.y + 1), board)) p = p.copy(y = p.y + 1)
        val nb = lockPiece(p, board)
        val (clearedBoard, count) = clearLines(nb)
        board   = clearedBoard
        score  += scoreFor(count)
        lines  += count
        level   = lines / 10 + 1
        if (score > highScore) highScore = score
        val np = next
        if (!isValid(np, board)) { gameOver = true; isRunning = false; gameJob?.cancel()
            gameDataViewModel?.submitTetrisScore(score, lines, maxCombo, level)
            gameDataViewModel?.vibrateGameOver()
            return
        }
        current = np
        next    = randomPiece()
    }

    DisposableEffect(Unit) { onDispose { gameJob?.cancel() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "TETRIS",
                        style = MaterialTheme.typography.titleLarge.copy(color = primary)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { gameJob?.cancel(); onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = primary)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (gameOver) resetGame()
                        else { isRunning = !isRunning; if (isRunning) startLoop() else gameJob?.cancel() }
                    }) {
                        Icon(
                            imageVector = when { gameOver -> Icons.Default.Refresh; isRunning -> Icons.Default.Pause; else -> Icons.Default.PlayArrow },
                            contentDescription = "Play/Pause", tint = primary
                        )
                    }
                    IconButton(onClick = { resetGame() }) {
                        Icon(Icons.Default.Refresh, "Reset", tint = primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Board ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(2.dp, primary, RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cw = size.width  / COLS
                    val ch = size.height / ROWS

                    // locked cells
                    for (r in 0 until ROWS) for (c in 0 until COLS) {
                        val t = board[r][c]
                        if (t != -1) {
                            drawRect(
                                color   = PIECE_COLORS[t],
                                topLeft = Offset(c * cw + 1, r * ch + 1),
                                size    = Size(cw - 2, ch - 2)
                            )
                        }
                    }
                    // ghost piece
                    var ghost = current
                    while (isValid(ghost.copy(y = ghost.y + 1), board)) ghost = ghost.copy(y = ghost.y + 1)
                    ghost.cells.forEach { (cx, cy) ->
                        if (cy in 0 until ROWS) drawRect(
                            color   = PIECE_COLORS[current.type].copy(alpha = 0.25f),
                            topLeft = Offset(cx * cw + 1, cy * ch + 1),
                            size    = Size(cw - 2, ch - 2)
                        )
                    }
                    // active piece
                    current.cells.forEach { (cx, cy) ->
                        if (cy in 0 until ROWS) drawRect(
                            color   = PIECE_COLORS[current.type],
                            topLeft = Offset(cx * cw + 1, cy * ch + 1),
                            size    = Size(cw - 2, ch - 2)
                        )
                    }
                }

                if (gameOver || (!isRunning && score == 0)) {
                    Box(
                        modifier         = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (gameOver) "GAME OVER" else "TETRIS",
                                style = MaterialTheme.typography.headlineMedium.copy(color = primary, fontWeight = FontWeight.Bold)
                            )
                            if (gameOver) {
                                Spacer(Modifier.height(4.dp))
                                Text("Score: $score", style = MaterialTheme.typography.titleMedium.copy(color = Color.White))
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (gameOver) "Tap ↺ to restart" else "Tap ▶ to start",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // ── Side panel ─────────────────────────────────────────────────
            Column(
                modifier            = Modifier
                    .width(90.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SideInfoCard(label = "SCORE",  value = "$score")
                SideInfoCard(label = "BEST",   value = "$highScore")
                SideInfoCard(label = "LINES",  value = "$lines")
                SideInfoCard(label = "LEVEL",  value = "$level")

                Spacer(Modifier.height(8.dp))
                Text("NEXT", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                Box(
                    modifier         = Modifier
                        .size(80.dp)
                        .border(1.dp, primary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(72.dp)) {
                        val cw = size.width  / 4
                        val ch = size.height / 4
                        next.cells.forEach { (cx, cy) ->
                            if (cx in 0..3 && cy in 0..3) {
                                drawRect(
                                    color   = PIECE_COLORS[next.type],
                                    topLeft = Offset(cx * cw + 1, cy * ch + 1),
                                    size    = Size(cw - 2, ch - 2)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                // Controls
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SmallGameBtn(icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,  desc = "Left")   { move(-1) }
                        SmallGameBtn(icon = Icons.AutoMirrored.Filled.KeyboardArrowRight, desc = "Right")  { move(1)  }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SmallGameBtn(icon = Icons.Default.KeyboardArrowDown, desc = "Down")       { softDrop() }
                        SmallGameBtn(icon = Icons.Default.RotateRight,       desc = "Rotate")     { rotate()   }
                    }
                    androidx.compose.material3.Button(
                        onClick  = { hardDrop() },
                        shape    = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("DROP", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun SideInfoCard(label: String, value: String) {
    Card(
        shape  = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier            = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            Text(value, style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
private fun SmallGameBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    onClick: () -> Unit
) {
    Box(
        modifier         = Modifier
            .size(36.dp)
            .background(MaterialTheme.colorScheme.surface, CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, modifier = Modifier.fillMaxSize()) {
            Icon(icon, desc, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
    }
}
