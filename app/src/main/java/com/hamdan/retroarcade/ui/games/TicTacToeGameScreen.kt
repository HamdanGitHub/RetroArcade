package com.hamdan.retroarcade.ui.games

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hamdan.retroarcade.viewmodel.GameDataViewModel
import kotlinx.coroutines.delay

// ── Enums ──────────────────────────────────────────────────────────────────────
private enum class TTTPlayer { X, O }
private enum class GameMode  { VS_FRIEND, VS_AI }
private enum class Difficulty(val label: String) { EASY("Easy"), MEDIUM("Medium"), HARD("Hard") }

// ── Pure game logic ────────────────────────────────────────────────────────────
private fun checkWinner(board: List<TTTPlayer?>, size: Int): TTTPlayer? {
    val winLen = if (size == 5) 4 else size
    for (r in 0 until size) for (c in 0..size - winLen) {
        val p = board[r * size + c] ?: continue
        if ((1 until winLen).all { board[r * size + c + it] == p }) return p
    }
    for (c in 0 until size) for (r in 0..size - winLen) {
        val p = board[r * size + c] ?: continue
        if ((1 until winLen).all { board[(r + it) * size + c] == p }) return p
    }
    for (r in 0..size - winLen) for (c in 0..size - winLen) {
        val p = board[r * size + c] ?: continue
        if ((1 until winLen).all { board[(r + it) * size + (c + it)] == p }) return p
    }
    for (r in 0..size - winLen) for (c in winLen - 1 until size) {
        val p = board[r * size + c] ?: continue
        if ((1 until winLen).all { board[(r + it) * size + (c - it)] == p }) return p
    }
    return null
}

private fun isFull(board: List<TTTPlayer?>): Boolean = board.none { it == null }

private fun isWinCell(board: List<TTTPlayer?>, size: Int, idx: Int, w: TTTPlayer): Boolean {
    val winLen = if (size == 5) 4 else size
    val r = idx / size; val c = idx % size
    fun ok(cells: List<Int>) = cells.all { board[it] == w }
    for (s in 0..size - winLen) {
        if (c in s until s + winLen && ok((s until s + winLen).map { r * size + it })) return true
        if (r in s until s + winLen && ok((s until s + winLen).map { it * size + c })) return true
    }
    for (rr in 0..size - winLen) for (cc in 0..size - winLen) {
        val cells = (0 until winLen).map { (rr + it) * size + (cc + it) }
        if (idx in cells && ok(cells)) return true
    }
    for (rr in 0..size - winLen) for (cc in winLen - 1 until size) {
        val cells = (0 until winLen).map { (rr + it) * size + (cc - it) }
        if (idx in cells && ok(cells)) return true
    }
    return false
}

// ── Minimax AI ─────────────────────────────────────────────────────────────────
private fun minimax(
    board: MutableList<TTTPlayer?>,
    size: Int,
    depth: Int,
    maxDepth: Int,
    isMaximising: Boolean,
    alpha: Int,
    beta: Int
): Int {
    val winner = checkWinner(board, size)
    if (winner == TTTPlayer.O) return 100 - depth
    if (winner == TTTPlayer.X) return depth - 100
    if (isFull(board) || depth >= maxDepth) return 0

    var a = alpha; var b = beta
    var best = if (isMaximising) Int.MIN_VALUE else Int.MAX_VALUE
    for (i in board.indices) {
        if (board[i] != null) continue
        board[i] = if (isMaximising) TTTPlayer.O else TTTPlayer.X
        val score = minimax(board, size, depth + 1, maxDepth, !isMaximising, a, b)
        board[i] = null
        if (isMaximising) {
            best = maxOf(best, score); a = maxOf(a, score)
        } else {
            best = minOf(best, score); b = minOf(b, score)
        }
        if (b <= a) break
    }
    return best
}

private fun aiMove(board: List<TTTPlayer?>, size: Int, difficulty: Difficulty): Int {
    val empty = board.indices.filter { board[it] == null }
    return when (difficulty) {
        Difficulty.EASY -> empty.random()
        Difficulty.MEDIUM -> {
            // 60% minimax, 40% random
            if ((0..9).random() < 6) bestMinimaxMove(board, size, maxDepth = 2)
            else empty.random()
        }
        Difficulty.HARD -> bestMinimaxMove(board, size, maxDepth = if (size == 3) 9 else 4)
    }
}

private fun bestMinimaxMove(board: List<TTTPlayer?>, size: Int, maxDepth: Int): Int {
    var bestScore = Int.MIN_VALUE
    var bestMove  = board.indices.first { board[it] == null }
    val mutable   = board.toMutableList()
    for (i in board.indices) {
        if (board[i] != null) continue
        mutable[i] = TTTPlayer.O
        val score = minimax(mutable, size, 0, maxDepth, false, Int.MIN_VALUE, Int.MAX_VALUE)
        mutable[i] = null
        if (score > bestScore) { bestScore = score; bestMove = i }
    }
    return bestMove
}

// ── Composable ─────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicTacToeGameScreen(
    onBack: () -> Unit,
    gameDataViewModel: GameDataViewModel? = null
) {
    var gridSize   by remember { mutableIntStateOf(3) }
    var board      by remember { mutableStateOf(List<TTTPlayer?>(9) { null }) }
    var current    by remember { mutableStateOf(TTTPlayer.X) }
    var winner     by remember { mutableStateOf<TTTPlayer?>(null) }
    var isDraw     by remember { mutableStateOf(false) }
    var xWins      by remember { mutableIntStateOf(0) }
    var oWins      by remember { mutableIntStateOf(0) }
    var draws      by remember { mutableIntStateOf(0) }
    var gameMode   by remember { mutableStateOf(GameMode.VS_AI) }
    var difficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
    var aiThinking by remember { mutableStateOf(false) }

    val primary = MaterialTheme.colorScheme.primary
    val xColor  = primary
    val oColor  = MaterialTheme.colorScheme.onSurface

    fun resetBoard(size: Int = gridSize) {
        gridSize   = size
        board      = List(size * size) { null }
        current    = TTTPlayer.X
        winner     = null
        isDraw     = false
        aiThinking = false
    }

    fun applyMove(index: Int, player: TTTPlayer): Boolean {
        if (board[index] != null || winner != null || isDraw) return false
        val nb = board.toMutableList().also { it[index] = player }
        board = nb
        val w = checkWinner(nb, gridSize)
        when {
            w != null        -> {
                winner = w
                if (w == TTTPlayer.X) xWins++ else oWins++
                gameDataViewModel?.vibrateSuccess()
                if (w == TTTPlayer.X && gameMode == GameMode.VS_AI)
                    gameDataViewModel?.submitTttWin(vsAi = true, difficulty = difficulty.name)
                else if (gameMode == GameMode.VS_FRIEND)
                    gameDataViewModel?.submitTttWin(vsAi = false, difficulty = "")
            }
            isFull(nb)       -> { isDraw = true; draws++; gameDataViewModel?.vibrateMedium() }
            else             -> current = if (player == TTTPlayer.X) TTTPlayer.O else TTTPlayer.X
        }
        return true
    }

    fun onCellClick(index: Int) {
        if (aiThinking || current != TTTPlayer.X && gameMode == GameMode.VS_AI) return
        if (!applyMove(index, current)) return
        gameDataViewModel?.vibrateLight()
    }

    // AI turn trigger
    LaunchedEffect(current, gameMode, board) {
        if (gameMode == GameMode.VS_AI && current == TTTPlayer.O && winner == null && !isDraw) {
            aiThinking = true
            delay(450L) // humanising pause
            val move = aiMove(board, gridSize, difficulty)
            applyMove(move, TTTPlayer.O)
            aiThinking = false
        }
    }

    val statusText = when {
        aiThinking     -> "AI is thinking..."
        winner != null -> if (gameMode == GameMode.VS_AI && winner == TTTPlayer.O) "AI Wins! 🤖"
                          else "${winner!!.name} Wins! 🎉"
        isDraw         -> "It's a Draw! 🤝"
        gameMode == GameMode.VS_AI && current == TTTPlayer.X -> "Your Turn (X)"
        gameMode == GameMode.VS_AI -> "AI's Turn (O)..."
        else           -> "${current.name}'s Turn"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("TIC TAC TOE", style = MaterialTheme.typography.titleLarge.copy(color = primary))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = primary)
                    }
                },
                actions = {
                    IconButton(onClick = { resetBoard() }) {
                        Icon(Icons.Default.Refresh, "Reset", tint = primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // ── Mode selector ──────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GameMode.entries.forEach { mode ->
                    val sel = gameMode == mode
                    Box(
                        modifier         = Modifier
                            .weight(1f).height(40.dp)
                            .background(
                                if (sel) primary else MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(10.dp)
                            )
                            .border(1.dp, primary.copy(alpha = if (sel) 0f else 0.3f), RoundedCornerShape(10.dp))
                            .clickable { gameMode = mode; resetBoard() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector        = if (mode == GameMode.VS_AI) Icons.Default.SmartToy else Icons.Default.Person,
                                contentDescription = mode.name,
                                tint               = if (sel) MaterialTheme.colorScheme.onPrimary else primary,
                                modifier           = Modifier.size(14.dp)
                            )
                            Text(
                                if (mode == GameMode.VS_AI) "vs AI" else "vs Friend",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color      = if (sel) MaterialTheme.colorScheme.onPrimary else primary,
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }

            // ── Difficulty (only in VS_AI mode) ───────────────────────────
            AnimatedVisibility(visible = gameMode == GameMode.VS_AI) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Difficulty.entries.forEach { diff ->
                        val sel = difficulty == diff
                        val diffColor = when (diff) {
                            Difficulty.EASY   -> Color(0xFF39FF14)
                            Difficulty.MEDIUM -> Color(0xFFFFAB00)
                            Difficulty.HARD   -> Color(0xFFFF2D78)
                        }
                        Box(
                            modifier         = Modifier
                                .weight(1f).height(34.dp)
                                .background(
                                    if (sel) diffColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(8.dp)
                                )
                                .border(1.dp, diffColor.copy(alpha = if (sel) 0.8f else 0.25f), RoundedCornerShape(8.dp))
                                .clickable { difficulty = diff; resetBoard() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                diff.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color      = if (sel) diffColor else diffColor.copy(alpha = 0.6f),
                                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }

            // ── Grid size selector ─────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(3 to "3×3", 4 to "4×4", 5 to "5×5").forEach { (size, label) ->
                    val sel = gridSize == size
                    Box(
                        modifier         = Modifier
                            .weight(1f).height(38.dp)
                            .background(
                                if (sel) primary else MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(10.dp)
                            )
                            .border(1.dp, primary.copy(alpha = if (sel) 0f else 0.3f), RoundedCornerShape(10.dp))
                            .clickable { resetBoard(size) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelLarge.copy(
                                color      = if (sel) MaterialTheme.colorScheme.onPrimary else primary,
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }

            // ── Score card ─────────────────────────────────────────────────
            Card(
                shape    = RoundedCornerShape(12.dp),
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    TTTScoreItem(label = "X",    value = xWins, color = xColor)
                    TTTScoreItem(label = "DRAW", value = draws, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TTTScoreItem(label = if (gameMode == GameMode.VS_AI) "AI" else "O", value = oWins, color = oColor)
                }
            }

            // ── Status ─────────────────────────────────────────────────────
            AnimatedContent(
                targetState   = statusText,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                label         = "status"
            ) { text ->
                Text(
                    text      = text,
                    style     = MaterialTheme.typography.titleMedium.copy(
                        color      = when {
                            winner == TTTPlayer.X  -> xColor
                            winner == TTTPlayer.O  -> if (gameMode == GameMode.VS_AI) Color(0xFFFF2D78) else oColor
                            isDraw                  -> MaterialTheme.colorScheme.onSurfaceVariant
                            aiThinking              -> MaterialTheme.colorScheme.onSurfaceVariant
                            current == TTTPlayer.X  -> xColor
                            else                    -> oColor
                        },
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
            }

            // ── Board ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(4.dp)
            ) {
                Column(
                    modifier            = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (row in 0 until gridSize) {
                        Row(
                            modifier              = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (col in 0 until gridSize) {
                                val idx      = row * gridSize + col
                                val cell     = board[idx]
                                val isWin    = winner != null && isWinCell(board, gridSize, idx, winner!!)
                                val canClick = cell == null && winner == null && !isDraw &&
                                        !(gameMode == GameMode.VS_AI && current == TTTPlayer.O)

                                val cellScale by animateFloatAsState(
                                    targetValue   = if (cell != null) 1f else if (canClick) 0.97f else 1f,
                                    animationSpec = tween(100),
                                    label         = "cell_$idx"
                                )

                                Box(
                                    modifier         = Modifier
                                        .weight(1f).fillMaxSize()
                                        .scale(cellScale)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                isWin  -> primary.copy(alpha = 0.18f)
                                                else   -> MaterialTheme.colorScheme.surface
                                            }
                                        )
                                        .border(
                                            width = if (isWin) 2.dp else 1.dp,
                                            color = if (isWin) primary else primary.copy(alpha = 0.18f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable(enabled = canClick) { onCellClick(idx) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (cell) {
                                        TTTPlayer.X -> Icon(
                                            Icons.Default.Close, "X",
                                            tint     = xColor,
                                            modifier = Modifier.fillMaxSize(0.6f)
                                        )
                                        TTTPlayer.O -> Icon(
                                            Icons.Default.RadioButtonUnchecked, "O",
                                            tint     = if (gameMode == GameMode.VS_AI) Color(0xFFFF2D78) else oColor,
                                            modifier = Modifier.fillMaxSize(0.6f)
                                        )
                                        null -> {}
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── New game button ────────────────────────────────────────────
            AnimatedVisibility(visible = winner != null || isDraw) {
                androidx.compose.material3.Button(
                    onClick  = { resetBoard() },
                    shape    = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("NEW GAME", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TTTScoreItem(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", style = MaterialTheme.typography.headlineSmall.copy(color = color, fontWeight = FontWeight.Bold))
        Text(label,   style = MaterialTheme.typography.labelSmall.copy(color = color.copy(alpha = 0.7f)))
    }
}
