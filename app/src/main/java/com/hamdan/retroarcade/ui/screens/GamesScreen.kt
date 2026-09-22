package com.hamdan.retroarcade.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamdan.retroarcade.navigation.NavRoutes
import com.hamdan.retroarcade.ui.components.StarfieldBackground
import com.hamdan.retroarcade.viewmodel.GameDataViewModel
import kotlinx.coroutines.delay

// ── Game metadata ──────────────────────────────────────────────────────────────
data class GameInfo(
    val title: String,
    val subtitle: String,
    val description: String,
    val route: String,
    val tag: String,
    val accentColor: Color,
    val drawPreview: DrawScope.(Float, Color) -> Unit  // (animTick, primary) -> Unit
)

private val GAMES = listOf(
    GameInfo(
        title       = "SNAKE",
        subtitle    = "Classic Nokia",
        description = "Eat, grow, survive. How long can you last?",
        route       = NavRoutes.SNAKE,
        tag         = "CLASSIC",
        accentColor = Color(0xFF39FF14),
        drawPreview = { tick, primary ->
            // Animated snake body
            val cellW = size.width / 8f
            val cellH = size.height / 8f
            val snakeCells = listOf(
                Offset(4f, 4f), Offset(3f, 4f), Offset(2f, 4f),
                Offset(2f, 3f), Offset(2f, 2f), Offset(3f, 2f)
            )
            snakeCells.forEachIndexed { i, (cx, cy) ->
                drawRoundRect(
                    color        = primary.copy(alpha = 1f - i * 0.12f),
                    topLeft      = Offset(cx * cellW + 1, cy * cellH + 1),
                    size         = Size(cellW - 2, cellH - 2),
                    cornerRadius = CornerRadius(3f)
                )
            }
            // Animated food (pulse with tick)
            val foodPulse = 0.8f + 0.2f * kotlin.math.sin(tick * 4f)
            val fx = 6f * cellW; val fy = 4f * cellH
            drawCircle(
                color  = Color(0xFFFF4444).copy(alpha = foodPulse),
                radius = (cellW / 2f) * foodPulse,
                center = Offset(fx + cellW / 2, fy + cellH / 2)
            )
            // Grid dots
            for (r in 1..7) for (c in 1..7) {
                drawCircle(Color.White.copy(alpha = 0.06f), 1f, Offset(c * cellW, r * cellH))
            }
        }
    ),
    GameInfo(
        title       = "TETRIS",
        subtitle    = "Block Stacker",
        description = "Stack blocks, clear lines. Can you reach level 10?",
        route       = NavRoutes.TETRIS,
        tag         = "PUZZLE",
        accentColor = Color(0xFF00BFFF),
        drawPreview = { tick, primary ->
            val cellW = size.width / 6f
            val cellH = size.height / 10f
            val blockColors = listOf(
                Color(0xFF00BFFF), Color(0xFFFFD700), Color(0xFFBB86FC),
                Color(0xFF39FF14), Color(0xFFFF4444), Color(0xFFFF8C00)
            )
            // Stacked blocks at bottom
            val stack = listOf(
                listOf(0,1,1,1,0,1), listOf(1,1,0,1,1,1),
                listOf(1,0,1,1,0,1), listOf(0,1,1,0,1,1)
            )
            stack.forEachIndexed { row, cols ->
                cols.forEachIndexed { col, filled ->
                    if (filled == 1) {
                        drawRoundRect(
                            color        = blockColors[(row * 6 + col) % blockColors.size].copy(alpha = 0.85f),
                            topLeft      = Offset(col * cellW + 1f, (6 + row) * cellH + 1f),
                            size         = Size(cellW - 2f, cellH - 2f),
                            cornerRadius = CornerRadius(2f)
                        )
                    }
                }
            }
            // Falling piece (animated with tick)
            val fallY = (tick * 1.5f % 4f)
            val tPiece = listOf(Offset(2f, 0f), Offset(1f, 1f), Offset(2f, 1f), Offset(3f, 1f))
            tPiece.forEach { (px, py) ->
                drawRoundRect(
                    color        = primary.copy(alpha = 0.9f),
                    topLeft      = Offset(px * cellW + 1f, (fallY + py) * cellH + 1f),
                    size         = Size(cellW - 2f, cellH - 2f),
                    cornerRadius = CornerRadius(2f)
                )
            }
        }
    ),
    GameInfo(
        title       = "PONG",
        subtitle    = "Arcade Classic",
        description = "VS AI or a friend. First to 7 wins!",
        route       = NavRoutes.PONG,
        tag         = "ARCADE",
        accentColor = Color(0xFFFFAB00),
        drawPreview = { tick, primary ->
            val w = size.width; val h = size.height
            val ph = h * 0.35f; val pw = w * 0.06f
            // Centre dashed line
            val dashStep = h / 10f
            for (i in 0..9) {
                if (i % 2 == 0) drawLine(
                    Color.White.copy(alpha = 0.2f),
                    Offset(w / 2, i * dashStep),
                    Offset(w / 2, (i + 0.6f) * dashStep),
                    strokeWidth = 2f
                )
            }
            // Ball (animated)
            val bx = w * (0.2f + 0.6f * (0.5f + 0.5f * kotlin.math.sin(tick * 2f)))
            val by = h * (0.3f + 0.4f * (0.5f + 0.5f * kotlin.math.cos(tick * 1.7f)))
            drawCircle(primary, w * 0.04f, Offset(bx, by))
            // Ball glow
            drawCircle(primary.copy(alpha = 0.25f), w * 0.08f, Offset(bx, by))
            // Left paddle
            drawRoundRect(primary, Offset(w * 0.04f, h * 0.3f), Size(pw, ph), CornerRadius(4f))
            // Right paddle
            drawRoundRect(Color.White.copy(alpha = 0.7f), Offset(w * 0.9f, h * 0.4f), Size(pw, ph), CornerRadius(4f))
        }
    ),
    GameInfo(
        title       = "MEMORY",
        subtitle    = "Card Pairs",
        description = "Flip and match. Test your memory!",
        route       = NavRoutes.MEMORY,
        tag         = "MEMORY",
        accentColor = Color(0xFFBB86FC),
        drawPreview = { tick, primary ->
            val cols = 4; val rows = 3
            val cellW = size.width / (cols + 0.5f)
            val cellH = size.height / (rows + 0.5f)
            val flipCard = (tick * 0.8f % (cols * rows)).toInt()
            for (r in 0 until rows) for (c in 0 until cols) {
                val idx = r * cols + c
                val isFlipped = idx == flipCard || idx == (flipCard + 3) % (cols * rows)
                val isMatched = idx < 4
                drawRoundRect(
                    color        = when {
                        isMatched -> primary.copy(alpha = 0.3f)
                        isFlipped -> primary.copy(alpha = 0.85f)
                        else      -> Color.White.copy(alpha = 0.08f)
                    },
                    topLeft      = Offset(c * cellW + cellW * 0.25f + 2f, r * cellH + cellH * 0.25f + 2f),
                    size         = Size(cellW * 0.8f, cellH * 0.8f),
                    cornerRadius = CornerRadius(4f)
                )
                if (!isFlipped && !isMatched) {
                    // "?" mark
                    drawCircle(
                        Color.White.copy(alpha = 0.2f), 3f,
                        Offset(c * cellW + cellW * 0.65f, r * cellH + cellH * 0.65f)
                    )
                }
            }
        }
    ),
    GameInfo(
        title       = "TIC TAC TOE",
        subtitle    = "Strategy Grid",
        description = "3×3, 4×4, 5×5. Play solo vs AI or with a friend!",
        route       = NavRoutes.TIC_TAC_TOE,
        tag         = "STRATEGY",
        accentColor = Color(0xFFFF2D78),
        drawPreview = { tick, primary ->
            val cellW = size.width / 3f; val cellH = size.height / 3f
            val sw = 2f
            // Grid lines
            drawLine(Color.White.copy(alpha = 0.3f), Offset(cellW, 0f), Offset(cellW, size.height), sw)
            drawLine(Color.White.copy(alpha = 0.3f), Offset(cellW * 2, 0f), Offset(cellW * 2, size.height), sw)
            drawLine(Color.White.copy(alpha = 0.3f), Offset(0f, cellH), Offset(size.width, cellH), sw)
            drawLine(Color.White.copy(alpha = 0.3f), Offset(0f, cellH * 2), Offset(size.width, cellH * 2), sw)
            // X marks
            listOf(Pair(0, 0), Pair(1, 2), Pair(2, 1)).forEach { (r, c) ->
                val cx = c * cellW + cellW / 2; val cy = r * cellH + cellH / 2; val s = cellW * 0.28f
                drawLine(primary, Offset(cx - s, cy - s), Offset(cx + s, cy + s), sw + 1f)
                drawLine(primary, Offset(cx + s, cy - s), Offset(cx - s, cy + s), sw + 1f)
            }
            // O marks
            listOf(Pair(0, 1), Pair(1, 0)).forEach { (r, c) ->
                val cx = c * cellW + cellW / 2; val cy = r * cellH + cellH / 2
                drawCircle(Color.White.copy(alpha = 0.75f), cellW * 0.28f, Offset(cx, cy), style = Stroke(sw + 0.5f))
            }
            // Winning line (animated opacity)
            val winAlpha = 0.5f + 0.5f * kotlin.math.sin(tick * 3f)
            // Diagonal win
            drawLine(
                primary.copy(alpha = winAlpha),
                Offset(cellW * 0.15f, cellH * 0.15f),
                Offset(cellW * 2.85f, cellH * 0.85f),
                strokeWidth = 3f
            )
        }
    )
)

// ── Screen ─────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    onGameSelected: (String) -> Unit,
    onBack: () -> Unit,
    gameDataViewModel: GameDataViewModel? = null
) {
    val primary = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SELECT GAME",
                        style = MaterialTheme.typography.titleLarge.copy(color = primary)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->

        StarfieldBackground(
            modifier     = Modifier.fillMaxSize(),
            primaryColor = primary
        ) {
            // Dark overlay so cards are readable over stars
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.92f)
                            )
                        )
                    )
            )

            LazyVerticalGrid(
                columns               = GridCells.Fixed(2),
                contentPadding        = PaddingValues(
                    start  = 14.dp,
                    end    = 14.dp,
                    top    = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = 28.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement   = Arrangement.spacedBy(12.dp),
                modifier              = Modifier.fillMaxSize()
            ) {
                itemsIndexed(GAMES) { idx, game ->
                    EnhancedGameCard(
                        game     = game,
                        index    = idx,
                        onClick  = { onGameSelected(game.route) }
                    )
                }
            }
        }
    }
}

// ── Animated game card ─────────────────────────────────────────────────────────
@Composable
private fun EnhancedGameCard(game: GameInfo, index: Int, onClick: () -> Unit) {
    val primary = MaterialTheme.colorScheme.primary

    // Staggered entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 80L)
        visible = true
    }
    val scale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0.7f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label         = "cardScale_$index"
    )
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label         = "cardAlpha_$index"
    )

    // Infinite animation tick for preview
    val infTransition = rememberInfiniteTransition(label = "preview_$index")
    val tick by infTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation  = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "tick_$index"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .scale(scale)
            .graphicsLayer { this.alpha = alpha }
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        game.accentColor.copy(alpha = 0.6f),
                        game.accentColor.copy(alpha = 0.1f),
                        primary.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
    ) {

        // ── Mini game preview Canvas ───────────────────────────────────────
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF050510),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                )
        ) {
            game.drawPreview(this, tick, game.accentColor)
        }

        // ── Gradient fade from preview to info ─────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .align(Alignment.TopCenter)
                .padding(top = 90.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface)
                    )
                )
        )

        // ── Tag chip (top-right) ───────────────────────────────────────────
        Text(
            text     = game.tag,
            style    = MaterialTheme.typography.labelSmall.copy(
                color      = game.accentColor,
                fontWeight = FontWeight.Bold,
                fontSize   = 9.sp
            ),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(game.accentColor.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )

        // ── Text info panel ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
        ) {
            Text(
                text  = game.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    color      = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Text(
                text  = game.subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color    = game.accentColor,
                    fontSize = 10.sp
                )
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text     = game.description,
                style    = MaterialTheme.typography.bodySmall.copy(
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                ),
                maxLines = 2
            )
        }

        // ── Bottom accent bar ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            game.accentColor,
                            game.accentColor.copy(alpha = 0.3f)
                        )
                    ),
                    RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp)
                )
        )
    }
}
