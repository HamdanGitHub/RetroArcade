package com.hamdan.retroarcade

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.hamdan.retroarcade.navigation.RetroArcadeNavGraph
import com.hamdan.retroarcade.ui.theme.RetroArcadeTheme
import com.hamdan.retroarcade.viewmodel.GameDataViewModel
import com.hamdan.retroarcade.viewmodel.ThemeViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel     = viewModel()
            val gameDataViewModel: GameDataViewModel = viewModel()

            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            val accentColor by themeViewModel.accentColor.collectAsState()
            val newAchievement by gameDataViewModel.newAchievement.collectAsState()

            RetroArcadeTheme(darkTheme = isDarkTheme, accent = accentColor) {

                Box(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    RetroArcadeNavGraph(
                        navController     = navController,
                        themeViewModel    = themeViewModel,
                        gameDataViewModel = gameDataViewModel
                    )

                    // ── Achievement toast ──────────────────────────────────
                    var showToast by remember { mutableStateOf(false) }

                    LaunchedEffect(newAchievement) {
                        if (newAchievement != null) {
                            showToast = true
                            delay(3200L)
                            showToast = false
                            delay(400L)
                            gameDataViewModel.clearNewAchievement()
                        }
                    }

                    AnimatedVisibility(
                        visible = showToast,
                        enter   = slideInVertically { -it } + fadeIn(),
                        exit    = slideOutVertically { -it } + fadeOut(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 56.dp, start = 16.dp, end = 16.dp)
                    ) {
                        newAchievement?.let { ach ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(ach.emoji, style = MaterialTheme.typography.headlineSmall)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Achievement Unlocked!",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Text(
                                        ach.title,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        ach.description,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
