package com.hamdan.retroarcade.viewmodel

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hamdan.retroarcade.ui.theme.AccentColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// DataStore instance tied to Application context
private val android.content.Context.dataStore: DataStore<Preferences>
        by preferencesDataStore(name = "retro_arcade_prefs")

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = application.dataStore

    private object Keys {
        val DARK_THEME  = booleanPreferencesKey("dark_theme")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
    }

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _accentColor = MutableStateFlow(AccentColor.NEON_GREEN)
    val accentColor: StateFlow<AccentColor> = _accentColor.asStateFlow()

    init {
        viewModelScope.launch {
            dataStore.data
                .catch { /* emit defaults on error */ }
                .map { prefs ->
                    val dark   = prefs[Keys.DARK_THEME] ?: true
                    val accent = prefs[Keys.ACCENT_COLOR]
                        ?.let { name -> AccentColor.entries.firstOrNull { it.name == name } }
                        ?: AccentColor.NEON_GREEN
                    Pair(dark, accent)
                }
                .collect { (dark, accent) ->
                    _isDarkTheme.value  = dark
                    _accentColor.value  = accent
                }
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[Keys.DARK_THEME] = enabled }
        }
    }

    fun setAccentColor(accent: AccentColor) {
        viewModelScope.launch {
            dataStore.edit { it[Keys.ACCENT_COLOR] = accent.name }
        }
    }
}
