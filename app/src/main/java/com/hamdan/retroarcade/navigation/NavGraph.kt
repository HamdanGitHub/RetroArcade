package com.hamdan.retroarcade.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hamdan.retroarcade.ui.games.MemoryGameScreen
import com.hamdan.retroarcade.ui.games.PongGameScreen
import com.hamdan.retroarcade.ui.games.SnakeGameScreen
import com.hamdan.retroarcade.ui.games.TetrisGameScreen
import com.hamdan.retroarcade.ui.games.TicTacToeGameScreen
import com.hamdan.retroarcade.ui.screens.AboutScreen
import com.hamdan.retroarcade.ui.screens.AchievementsScreen
import com.hamdan.retroarcade.ui.screens.GamesScreen
import com.hamdan.retroarcade.ui.screens.HomeScreen
import com.hamdan.retroarcade.ui.screens.SettingsScreen
import com.hamdan.retroarcade.viewmodel.GameDataViewModel
import com.hamdan.retroarcade.viewmodel.ThemeViewModel

@Composable
fun RetroArcadeNavGraph(
    navController: NavHostController,
    themeViewModel: ThemeViewModel,
    gameDataViewModel: GameDataViewModel
) {
    NavHost(
        navController    = navController,
        startDestination = NavRoutes.HOME
    ) {
        composable(NavRoutes.HOME) {
            HomeScreen(
                onNavigateToGames        = { navController.navigate(NavRoutes.GAMES) },
                onNavigateToAbout        = { navController.navigate(NavRoutes.ABOUT) },
                onNavigateToSettings     = { navController.navigate(NavRoutes.SETTINGS) },
                onNavigateToAchievements = { navController.navigate(NavRoutes.ACHIEVEMENTS) }
            )
        }
        composable(NavRoutes.GAMES) {
            GamesScreen(
                onGameSelected    = { route -> navController.navigate(route) },
                onBack            = { navController.popBackStack() },
                gameDataViewModel = gameDataViewModel
            )
        }
        composable(NavRoutes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                themeViewModel = themeViewModel,
                onBack         = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.ACHIEVEMENTS) {
            AchievementsScreen(
                gameDataViewModel = gameDataViewModel,
                onBack            = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.SNAKE) {
            SnakeGameScreen(
                onBack            = { navController.popBackStack() },
                gameDataViewModel = gameDataViewModel
            )
        }
        composable(NavRoutes.TETRIS) {
            TetrisGameScreen(
                onBack            = { navController.popBackStack() },
                gameDataViewModel = gameDataViewModel
            )
        }
        composable(NavRoutes.PONG) {
            PongGameScreen(
                onBack            = { navController.popBackStack() },
                gameDataViewModel = gameDataViewModel
            )
        }
        composable(NavRoutes.MEMORY) {
            MemoryGameScreen(
                onBack            = { navController.popBackStack() },
                gameDataViewModel = gameDataViewModel
            )
        }
        composable(NavRoutes.TIC_TAC_TOE) {
            TicTacToeGameScreen(
                onBack            = { navController.popBackStack() },
                gameDataViewModel = gameDataViewModel
            )
        }
    }
}
