package com.hamdan.retroarcade.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamdan.retroarcade.ui.components.StarfieldBackground
import com.hamdan.retroarcade.viewmodel.Achievement
import com.hamdan.retroarcade.viewmodel.GameDataViewModel
import kotlinx.coroutines.delay

// Groups match achievement id prefixes
private val CATEGORIES = listOf(
    "🐍" to "Snake",
    "🧱" to "Tetris",
    "🏓" to "Pong",
    "🃏" to "Memory",
    "❌" to "Tic Tac Toe",
    "🕹️" to "General"
)

private fun categoryForId(id: String) = when {
    id.startsWith("snake")  -> "Snake"
    id.startsWith("tetris") -> "Tetris"
    id.startsWith("pong")   -> "Pong"
    id.startsWith("memory") -> "Memory"
    id.startsWith("ttt")    -> "Tic Tac Toe"
    else                    -> "General"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    gameDataViewModel: GameDataViewModel,
    onBack: () -> Unit
) {
    val achievements by gameDataViewModel.achievements.collectAsState()
    val snakeHigh    by gameDataViewModel.snakeHigh.collectAsState()
    val tetrisHigh   by gameDataViewModel.tetrisHigh.collectAsState()
    val pongWins     by gameDataViewModel.pongWins.collectAsState()
    val tttWins      by gameDataViewModel.tttWins.collectAsState()
    val memoryBest   by gameDataViewModel.memoryBest.collectAsState()

    val unlocked = achievements.count { it.unlocked }
    val total    = achievements.size
    val progress = if (total == 0) 0f else unlocked.toFloat() / total

    val grouped = achievements.groupBy { categoryForId(it.id) }
    val primary  = MaterialTheme.colorScheme.primary

    // Animate progress bar
    var animProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue   = animProgress,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label         = "achProgress"
    )
    LaunchedEffect(progress) {
        delay(200)
        animProgress = progress
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ACHIEVEMENTS",
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.82f),
                                MaterialTheme.colorScheme.background.copy(alpha = 0.93f)
                            )
                        )
                    )
            )

            LazyColumn(
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Overall progress card ──────────────────────────────────
                item {
                    Spacer(Modifier.height(4.dp))
                    Card(
                        shape    = RoundedCornerShape(16.dp),
                        colors   = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Overall Progress",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color      = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        "$unlocked / $total achievements",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                // Trophy icon with glow if all unlocked
                                Box(
                                    modifier         = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (unlocked == total) primary.copy(alpha = 0.2f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.EmojiEvents,
                                        "Trophy",
                                        tint     = if (unlocked == total) primary
                                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            LinearProgressIndicator(
                                progress     = { animatedProgress },
                                modifier     = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color        = primary,
                                trackColor   = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap    = StrokeCap.Round
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                "${(progress * 100).toInt()}% complete",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = primary
                                )
                            )
                        }
                    }
                }

                // ── High scores card ───────────────────────────────────────
                item {
                    Card(
                        shape    = RoundedCornerShape(16.dp),
                        colors   = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "// HIGH SCORES",
                                style = MaterialTheme.typography.labelMedium.copy(color = primary)
                            )
                            Spacer(Modifier.height(12.dp))
                            val scores = listOf(
                                "🐍 Snake"      to (if (snakeHigh  > 0) "$snakeHigh pts"   else "Not played"),
                                "🧱 Tetris"     to (if (tetrisHigh > 0) "$tetrisHigh pts"  else "Not played"),
                                "🏓 Pong"       to (if (pongWins   > 0) "$pongWins wins"   else "Not played"),
                                "❌ Tic Tac Toe" to (if (tttWins    > 0) "$tttWins wins"    else "Not played"),
                                "🃏 Memory"     to (if (memoryBest < Int.MAX_VALUE) "$memoryBest moves best" else "Not played")
                            )
                            scores.forEach { (game, score) ->
                                Row(
                                    modifier              = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        game,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        score,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color      = if (score == "Not played")
                                                            MaterialTheme.colorScheme.onSurfaceVariant
                                                         else primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Achievement categories ─────────────────────────────────
                CATEGORIES.forEach { (emoji, catName) ->
                    val catAchs = grouped[catName] ?: return@forEach
                    val catUnlocked = catAchs.count { it.unlocked }

                    item(key = catName) {
                        // Category header
                        Row(
                            modifier          = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(emoji, style = MaterialTheme.typography.titleMedium)
                            Text(
                                catName.uppercase(),
                                style = MaterialTheme.typography.labelMedium.copy(color = primary)
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                "$catUnlocked/${catAchs.size}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    items(catAchs, key = { it.id }) { ach ->
                        AchievementRow(achievement = ach, primaryColor = primary)
                    }

                    item(key = "${catName}_spacer") { Spacer(Modifier.height(4.dp)) }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun AchievementRow(achievement: Achievement, primaryColor: Color) {
    val unlocked = achievement.unlocked

    Card(
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(
            containerColor = if (unlocked)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (unlocked) Modifier.border(
                    1.dp,
                    primaryColor.copy(alpha = 0.35f),
                    RoundedCornerShape(12.dp)
                ) else Modifier
            )
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Emoji / lock badge
            Box(
                modifier         = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (unlocked) primaryColor.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (unlocked) {
                    Text(achievement.emoji, fontSize = 22.sp)
                } else {
                    Icon(
                        Icons.Default.Lock, "Locked",
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (unlocked) achievement.title else "???",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color      = if (unlocked) MaterialTheme.colorScheme.onSurface
                                     else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    if (unlocked) achievement.description else "Keep playing to unlock",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color   = if (unlocked) MaterialTheme.colorScheme.onSurfaceVariant
                                  else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        fontSize = 11.sp
                    ),
                    maxLines = 2
                )
            }

            // Unlocked check dot
            if (unlocked) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(primaryColor)
                )
            }
        }
    }
}
