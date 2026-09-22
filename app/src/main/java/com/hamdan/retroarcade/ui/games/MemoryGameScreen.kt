package com.hamdan.retroarcade.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

// ── All available icons for cards ─────────────────────────────────────────────
private val ALL_ICONS: List<ImageVector> = listOf(
    Icons.Default.Favorite, Icons.Default.Star, Icons.Default.Rocket,
    Icons.Default.Bolt, Icons.Default.Diamond, Icons.Default.AcUnit,
    Icons.Default.WbSunny, Icons.Default.MusicNote, Icons.Default.Headphones,
    Icons.Default.Pets, Icons.Default.Android, Icons.Default.Cake,
    Icons.Default.LocalPizza, Icons.Default.FlightTakeoff, Icons.Default.Fireplace,
    Icons.Default.Headphones
)

private data class MemoryCard(
    val id: Int,
    val pairId: Int,
    val icon: ImageVector,
    var isFlipped: Boolean  = false,
    var isMatched: Boolean  = false
)

private fun buildDeck(pairs: Int): List<MemoryCard> {
    val icons  = ALL_ICONS.take(pairs)
    val cards  = (icons + icons).mapIndexed { idx, icon ->
        MemoryCard(id = idx, pairId = idx % pairs, icon = icon)
    }
    return cards.shuffled()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryGameScreen(
    onBack: () -> Unit,
    gameDataViewModel: com.hamdan.retroarcade.viewmodel.GameDataViewModel? = null
) {

    // difficulty: 8 pairs (4x4), 12 pairs (4x6), 16 pairs (4x8)
    var pairCount   by remember { mutableIntStateOf(8) }
    var cards       by remember { mutableStateOf(buildDeck(pairCount)) }
    var flipped     by remember { mutableStateOf<List<Int>>(emptyList()) }
    var moves       by remember { mutableIntStateOf(0) }
    var mismatches  by remember { mutableIntStateOf(0) }
    var matched     by remember { mutableIntStateOf(0) }
    var isChecking  by remember { mutableStateOf(false) }
    var showWin     by remember { mutableStateOf(false) }
    var bestMoves   by remember { mutableIntStateOf(Int.MAX_VALUE) }

    val cols = 4

    fun resetGame(pairs: Int = pairCount) {
        pairCount  = pairs
        cards      = buildDeck(pairs)
        flipped    = emptyList()
        moves      = 0
        mismatches = 0
        matched    = 0
        isChecking = false
        showWin    = false
    }

    // Check for match after two cards flipped
    LaunchedEffect(flipped) {
        if (flipped.size == 2 && !isChecking) {
            isChecking = true
            val (a, b) = flipped
            if (cards[a].pairId == cards[b].pairId) {
                // Match
                cards = cards.toMutableList().also {
                    it[a] = it[a].copy(isMatched = true)
                    it[b] = it[b].copy(isMatched = true)
                }
                matched += 2
                if (matched == cards.size) {
                    if (moves + 1 < bestMoves) bestMoves = moves + 1
                    showWin = true
                    gameDataViewModel?.submitMemoryResult(moves + 1, mismatches, pairCount)
                    gameDataViewModel?.vibrateSuccess()
                }
            } else {
                mismatches++
                gameDataViewModel?.vibrateLight()
                delay(900L)
                cards = cards.toMutableList().also {
                    it[a] = it[a].copy(isFlipped = false)
                    it[b] = it[b].copy(isFlipped = false)
                }
            }
            moves++
            flipped    = emptyList()
            isChecking = false
        }
    }

    fun onCardClick(index: Int) {
        if (isChecking) return
        val card = cards[index]
        if (card.isFlipped || card.isMatched) return
        if (flipped.size >= 2) return

        cards  = cards.toMutableList().also { it[index] = it[index].copy(isFlipped = true) }
        flipped = flipped + index
    }

    val primary = MaterialTheme.colorScheme.primary

    if (showWin) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("🎉 You Won!", style = MaterialTheme.typography.headlineSmall.copy(color = primary, fontWeight = FontWeight.Bold))
            },
            text = {
                Column {
                    Text("Moves taken: $moves", style = MaterialTheme.typography.bodyMedium)
                    if (bestMoves != Int.MAX_VALUE && moves <= bestMoves)
                        Text("🏆 New best: $bestMoves!", style = MaterialTheme.typography.bodySmall.copy(color = primary))
                }
            },
            confirmButton = {
                TextButton(onClick = { resetGame() }) {
                    Text("Play Again", color = primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWin = false; onBack() }) {
                    Text("Exit", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("MEMORY", style = MaterialTheme.typography.titleLarge.copy(color = primary))
                        Text("Moves: $moves", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = primary)
                    }
                },
                actions = {
                    IconButton(onClick = { resetGame() }) {
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Difficulty selector
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(8 to "4×4", 12 to "4×6", 16 to "4×8").forEach { (pairs, label) ->
                    val selected = pairCount == pairs
                    Box(
                        modifier         = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(
                                if (selected) primary else MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(8.dp)
                            )
                            .border(1.dp, primary.copy(alpha = if (selected) 0f else 0.4f), RoundedCornerShape(8.dp))
                            .clickable { resetGame(pairs) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color      = if (selected) MaterialTheme.colorScheme.onPrimary else primary,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }
            }

            // Progress bar
            val progress = if (cards.isEmpty()) 0f else matched.toFloat() / cards.size
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .background(primary, RoundedCornerShape(3.dp))
                )
            }

            Text(
                "${matched / 2} / ${cards.size / 2} pairs",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            // Card grid
            LazyVerticalGrid(
                columns               = GridCells.Fixed(cols),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement   = Arrangement.spacedBy(8.dp),
                modifier              = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(cards) { index, card ->
                    FlipCard(
                        card    = card,
                        onClick = { onCardClick(index) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FlipCard(card: MemoryCard, onClick: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue  = if (card.isFlipped || card.isMatched) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label        = "cardFlip_${card.id}"
    )

    val primary  = MaterialTheme.colorScheme.primary
    val matched  = card.isMatched

    Box(
        modifier         = Modifier
            .aspectRatio(0.75f)
            .graphicsLayer {
                rotationY            = rotation
                cameraDistance       = 12 * density
            }
            .background(
                when {
                    matched            -> primary.copy(alpha = 0.2f)
                    rotation > 90f     -> MaterialTheme.colorScheme.surface
                    else               -> MaterialTheme.colorScheme.surfaceVariant
                },
                RoundedCornerShape(10.dp)
            )
            .border(
                width  = if (matched) 2.dp else 1.dp,
                color  = if (matched) primary else primary.copy(alpha = 0.3f),
                shape  = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = !card.isFlipped && !matched) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (rotation > 90f) {
            // Front (icon side) — mirror correction
            Box(
                modifier         = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = card.icon,
                    contentDescription = null,
                    tint               = if (matched) primary else MaterialTheme.colorScheme.onSurface,
                    modifier           = Modifier.size(32.dp)
                )
            }
        } else {
            // Back — question mark pattern
            Text(
                "?",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color      = primary.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
