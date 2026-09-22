package com.hamdan.retroarcade.viewmodel

import android.app.Application
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val android.content.Context.gameDataStore
        by preferencesDataStore(name = "retro_arcade_game_data")

// ── Achievement definitions ────────────────────────────────────────────────────
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    var unlocked: Boolean = false
)

val ALL_ACHIEVEMENTS = listOf(
    // Snake
    Achievement("snake_first",    "First Bite",       "Score your first point in Snake",           "🐍"),
    Achievement("snake_10",       "Growing Up",       "Reach a score of 10 in Snake",              "🌱"),
    Achievement("snake_25",       "Long Boy",         "Reach a score of 25 in Snake",              "🐉"),
    Achievement("snake_50",       "Serpent King",     "Reach a score of 50 in Snake",              "👑"),
    // Tetris
    Achievement("tetris_first",   "Block Party",      "Clear your first line in Tetris",           "🧱"),
    Achievement("tetris_tetris",  "TETRIS!",          "Clear 4 lines at once",                     "✨"),
    Achievement("tetris_level5",  "Level Up",         "Reach level 5 in Tetris",                   "🚀"),
    Achievement("tetris_score500","500 Club",         "Score 500 points in Tetris",                "🏅"),
    // Pong
    Achievement("pong_first_win", "Pong Pro",         "Beat the AI in Pong",                       "🏓"),
    Achievement("pong_shutout",   "Shutout",          "Win Pong without letting AI score",         "🧊"),
    Achievement("pong_hard",      "Unbeatable",       "Beat the AI on Hard difficulty",            "🔥"),
    // Memory
    Achievement("memory_first",   "Good Memory",      "Complete your first Memory game",           "🃏"),
    Achievement("memory_perfect", "Perfect Mind",     "Complete Memory with no mismatches",        "🧠"),
    Achievement("memory_fast",    "Speed Reader",     "Complete 4x4 Memory in under 20 moves",     "⚡"),
    // Tic Tac Toe
    Achievement("ttt_first_win",  "X Marks the Spot", "Win your first Tic Tac Toe game",          "❌"),
    Achievement("ttt_beat_ai",    "Outsmarted",       "Beat the AI in Tic Tac Toe",                "🤖"),
    Achievement("ttt_beat_hard",  "Grandmaster",      "Beat the AI on Hard difficulty",            "🎓"),
    // General
    Achievement("all_games",      "Arcade Master",    "Play all 5 games at least once",            "🕹️"),
    Achievement("total_10",       "Dedicated",        "Earn 10 total achievements",                "🏆")
)

class GameDataViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.gameDataStore

    // ── Preference keys ────────────────────────────────────────────────────────
    private object Keys {
        val SNAKE_HIGH    = intPreferencesKey("snake_high")
        val TETRIS_HIGH   = intPreferencesKey("tetris_high")
        val PONG_WINS     = intPreferencesKey("pong_wins")
        val MEMORY_BEST   = intPreferencesKey("memory_best_moves")
        val TTT_WINS      = intPreferencesKey("ttt_wins")
        val ACHIEVEMENTS  = stringPreferencesKey("achievements_unlocked") // comma-sep ids
        // "played" flags for arcade master achievement
        val PLAYED_GAMES  = stringPreferencesKey("played_games")
    }

    // ── State flows ────────────────────────────────────────────────────────────
    private val _snakeHigh  = MutableStateFlow(0)
    val snakeHigh: StateFlow<Int> = _snakeHigh.asStateFlow()

    private val _tetrisHigh = MutableStateFlow(0)
    val tetrisHigh: StateFlow<Int> = _tetrisHigh.asStateFlow()

    private val _pongWins   = MutableStateFlow(0)
    val pongWins: StateFlow<Int> = _pongWins.asStateFlow()

    private val _memoryBest = MutableStateFlow(Int.MAX_VALUE)
    val memoryBest: StateFlow<Int> = _memoryBest.asStateFlow()

    private val _tttWins    = MutableStateFlow(0)
    val tttWins: StateFlow<Int> = _tttWins.asStateFlow()

    private val _achievements = MutableStateFlow(ALL_ACHIEVEMENTS.map { it.copy() })
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _newAchievement = MutableStateFlow<Achievement?>(null)
    val newAchievement: StateFlow<Achievement?> = _newAchievement.asStateFlow()

    init {
        viewModelScope.launch {
            dataStore.data
                .catch { }
                .map { prefs ->
                    val unlockedIds = (prefs[Keys.ACHIEVEMENTS] ?: "")
                        .split(",").filter { it.isNotBlank() }.toSet()
                    val updated = ALL_ACHIEVEMENTS.map { it.copy(unlocked = it.id in unlockedIds) }
                    Triple(
                        Triple(
                            prefs[Keys.SNAKE_HIGH]  ?: 0,
                            prefs[Keys.TETRIS_HIGH] ?: 0,
                            prefs[Keys.PONG_WINS]   ?: 0
                        ),
                        Pair(
                            prefs[Keys.MEMORY_BEST] ?: Int.MAX_VALUE,
                            prefs[Keys.TTT_WINS]    ?: 0
                        ),
                        updated
                    )
                }
                .collect { (scores, extra, achs) ->
                    _snakeHigh.value  = scores.first
                    _tetrisHigh.value = scores.second
                    _pongWins.value   = scores.third
                    _memoryBest.value = extra.first
                    _tttWins.value    = extra.second
                    _achievements.value = achs
                }
        }
    }

    // ── Public update functions ────────────────────────────────────────────────
    fun submitSnakeScore(score: Int) {
        viewModelScope.launch {
            if (score > _snakeHigh.value) {
                dataStore.edit { it[Keys.SNAKE_HIGH] = score }
            }
            markGamePlayed("snake")
            if (score >= 1)  unlock("snake_first")
            if (score >= 10) unlock("snake_10")
            if (score >= 25) unlock("snake_25")
            if (score >= 50) unlock("snake_50")
        }
    }

    fun submitTetrisScore(score: Int, linesCleared: Int, maxCombo: Int, level: Int) {
        viewModelScope.launch {
            if (score > _tetrisHigh.value) {
                dataStore.edit { it[Keys.TETRIS_HIGH] = score }
            }
            markGamePlayed("tetris")
            if (linesCleared >= 1)   unlock("tetris_first")
            if (maxCombo >= 4)        unlock("tetris_tetris")
            if (level >= 5)           unlock("tetris_level5")
            if (score >= 500)         unlock("tetris_score500")
        }
    }

    fun submitPongWin(difficulty: String, shutout: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[Keys.PONG_WINS] = _pongWins.value + 1 }
            markGamePlayed("pong")
            unlock("pong_first_win")
            if (shutout)               unlock("pong_shutout")
            if (difficulty == "HARD")  unlock("pong_hard")
        }
    }

    fun submitMemoryResult(moves: Int, mismatches: Int, gridSize: Int) {
        viewModelScope.launch {
            if (moves < (_memoryBest.value.takeIf { it != Int.MAX_VALUE } ?: Int.MAX_VALUE)) {
                dataStore.edit { it[Keys.MEMORY_BEST] = moves }
            }
            markGamePlayed("memory")
            unlock("memory_first")
            if (mismatches == 0)                          unlock("memory_perfect")
            if (gridSize == 8 && moves <= 20)             unlock("memory_fast")
        }
    }

    fun submitTttWin(vsAi: Boolean, difficulty: String) {
        viewModelScope.launch {
            dataStore.edit { it[Keys.TTT_WINS] = _tttWins.value + 1 }
            markGamePlayed("ttt")
            unlock("ttt_first_win")
            if (vsAi)                        unlock("ttt_beat_ai")
            if (vsAi && difficulty == "HARD") unlock("ttt_beat_hard")
        }
    }

    fun clearNewAchievement() { _newAchievement.value = null }

    // ── Vibration helpers (public so screens can call them) ───────────────────
    fun vibrateLight() = vibrate(30L)
    fun vibrateMedium() = vibrate(60L)
    fun vibrateSuccess() = vibrate(longArrayOf(0, 40, 30, 40))
    fun vibrateGameOver() = vibrate(longArrayOf(0, 100, 50, 100, 50, 100))

    private fun vibrate(ms: Long) {
        val ctx = getApplication<Application>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = ctx.getSystemService(VibratorManager::class.java)
            vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val v = ctx.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
            v?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    private fun vibrate(pattern: LongArray) {
        val ctx = getApplication<Application>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = ctx.getSystemService(VibratorManager::class.java)
            vm?.defaultVibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            val v = ctx.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
            v?.vibrate(VibrationEffect.createWaveform(pattern, -1))
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────
    private suspend fun unlock(id: String) {
        val current = _achievements.value
        val already = current.firstOrNull { it.id == id }?.unlocked ?: true
        if (already) return

        val newList = current.map { if (it.id == id) it.copy(unlocked = true) else it }
        _achievements.value = newList
        val ids = newList.filter { it.unlocked }.map { it.id }.joinToString(",")
        dataStore.edit { it[Keys.ACHIEVEMENTS] = ids }
        _newAchievement.value = newList.first { it.id == id }

        // Meta achievements
        val unlockedCount = newList.count { it.unlocked }
        if (unlockedCount >= 10) unlock("total_10")
    }

    private suspend fun markGamePlayed(game: String) {
        val played = dataStore.data.map { it[Keys.PLAYED_GAMES] ?: "" }
            .catch { emit("") }
            .let {
                var v = ""
                it.collect { s -> v = s; return@collect }
                v
            }
        val set = played.split(",").filter { it.isNotBlank() }.toMutableSet()
        set.add(game)
        dataStore.edit { it[Keys.PLAYED_GAMES] = set.joinToString(",") }
        if (set.containsAll(listOf("snake","tetris","pong","memory","ttt"))) unlock("all_games")
    }
}
