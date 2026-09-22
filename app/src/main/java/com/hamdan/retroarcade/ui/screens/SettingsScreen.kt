package com.hamdan.retroarcade.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hamdan.retroarcade.ui.theme.AccentColor
import com.hamdan.retroarcade.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeViewModel: ThemeViewModel,
    onBack: () -> Unit
) {
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
    val accentColor by themeViewModel.accentColor.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "SETTINGS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint               = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Appearance section ─────────────────────────────────────────
            SectionLabel(text = "// APPEARANCE")

            Card(
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment  = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector        = if (isDarkTheme) Icons.Default.DarkMode
                                                 else Icons.Default.LightMode,
                            contentDescription = "Theme mode",
                            tint               = MaterialTheme.colorScheme.primary,
                            modifier           = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text  = if (isDarkTheme) "Dark Mode" else "Light Mode",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color      = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text  = if (isDarkTheme) "Retro dark theme active"
                                        else "Light theme active",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                    Switch(
                        checked         = isDarkTheme,
                        onCheckedChange = { themeViewModel.setDarkTheme(it) },
                        colors          = SwitchDefaults.colors(
                            checkedThumbColor       = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor       = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor     = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor     = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Accent colour section ──────────────────────────────────────
            SectionLabel(text = "// ACCENT COLOR")

            Card(
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text  = "Choose your accent colour",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Grid of colour swatches
                    val chunked = AccentColor.entries.chunked(3)
                    chunked.forEach { row ->
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { accent ->
                                ColorSwatch(
                                    accent    = accent,
                                    selected  = accent == accentColor,
                                    onClick   = { themeViewModel.setAccentColor(accent) },
                                    modifier  = Modifier.weight(1f)
                                )
                            }
                            // Fill empty slots if row < 3
                            repeat(3 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── About this app ─────────────────────────────────────────────
            SectionLabel(text = "// APP")

            Card(
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    SettingInfoRow(label = "App Name",   value = "RetroArcade")
                    SettingInfoRow(label = "Version",    value = "1.0")
                    SettingInfoRow(label = "Developer",  value = "Mohamed Hamdan")
                    SettingInfoRow(label = "Platform",   value = "Android · Jetpack Compose")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ColorSwatch(
    accent: AccentColor,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(accent.primary)
                .border(
                    width  = if (selected) 3.dp else 0.dp,
                    color  = if (selected) MaterialTheme.colorScheme.onBackground
                             else          accent.primary,
                    shape  = CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector        = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint               = accent.onPrimary,
                    modifier           = Modifier.size(24.dp)
                )
            }
        }
        Text(
            text  = accent.label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (selected) MaterialTheme.colorScheme.primary
                        else          MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text  = text,
        style = MaterialTheme.typography.labelMedium.copy(
            color = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun SettingInfoRow(label: String, value: String) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        )
        Text(
            text  = value,
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
