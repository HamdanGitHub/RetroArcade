package com.hamdan.retroarcade.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hamdan.retroarcade.ui.components.StarfieldBackground

@Composable
fun HomeScreen(
    onNavigateToGames: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAchievements: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rocket")
    val rotation by infiniteTransition.animateFloat(
        initialValue  = -12f,
        targetValue   = 12f,
        animationSpec = infiniteRepeatable(
            animation  = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rocketRotation"
    )

    StarfieldBackground(
        modifier     = Modifier.fillMaxSize(),
        primaryColor = MaterialTheme.colorScheme.primary
    ) {
        // Dark overlay to keep text readable over stars
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.78f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.88f)
                        )
                    )
                )
        )

        // ── Top bar ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 52.dp, start = 8.dp, end = 8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateToAbout) {
                Icon(
                    Icons.Default.Person, "About",
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Row {
                IconButton(onClick = onNavigateToAchievements) {
                    Icon(
                        Icons.Default.EmojiEvents, "Achievements",
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        Icons.Default.Settings, "Settings",
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // ── Centre content ─────────────────────────────────────────────────
        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated rocket
            Icon(
                Icons.Default.Rocket, "RetroArcade",
                tint     = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(96.dp)
                    .rotate(rotation)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "RETRO",
                style = MaterialTheme.typography.displaySmall.copy(
                    color         = MaterialTheme.colorScheme.primary,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 8.sp
                )
            )
            Text(
                "ARCADE",
                style = MaterialTheme.typography.displaySmall.copy(
                    color         = MaterialTheme.colorScheme.onBackground,
                    fontWeight    = FontWeight.Bold,
                    letterSpacing = 8.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                "Classic games. Modern feel.",
                style     = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(44.dp))

            // Play button
            Button(
                onClick  = onNavigateToGames,
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(56.dp)
            ) {
                Icon(Icons.Default.SportsEsports, null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.size(10.dp))
                Text("PLAY GAMES", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Achievements shortcut button
            OutlinedButton(
                onClick  = onNavigateToAchievements,
                shape    = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(44.dp)
            ) {
                Icon(
                    Icons.Default.EmojiEvents, null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    "ACHIEVEMENTS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color      = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats row
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HomeStatCard(label = "GAMES",  value = "5")
                HomeStatCard(label = "THEMES", value = "5")
                HomeStatCard(label = "MODES",  value = "∞")
            }
        }

        // Footer
        Text(
            "by Mohamed Hamdan",
            style    = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

@Composable
private fun HomeStatCard(label: String, value: String) {
    Card(
        shape    = RoundedCornerShape(10.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        ),
        modifier = Modifier.size(width = 88.dp, height = 72.dp)
    ) {
        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color      = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
