package com.hamdan.retroarcade.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hamdan.retroarcade.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text  = "ABOUT",
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Avatar ────────────────────────────────────────────────────────
            Box(
                modifier            = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    ),
                contentAlignment    = Alignment.Center
            ) {
                // Profile photo — uses the real photo from drawable
                Image(
                    painter            = painterResource(id = R.drawable.profile_photo),
                    contentDescription = "Profile Photo",
                    modifier           = Modifier.fillMaxSize(),
                    contentScale       = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Name & role ───────────────────────────────────────────────────
            Text(
                text      = "Mohamed Hamdan",
                style     = MaterialTheme.typography.headlineMedium.copy(
                    color      = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text      = "Student Developer",
                style     = MaterialTheme.typography.titleSmall.copy(
                    color = MaterialTheme.colorScheme.primary
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── About card ────────────────────────────────────────────────────
            Card(
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text  = "// ABOUT THE DEVELOPER",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text  = "Hi! I'm Mohamed Hamdan, a university student and aspiring software developer. " +
                                "I enjoy learning about technology, programming, and creating simple applications. " +
                                "This game app was developed as a fun project to explore mobile app development " +
                                "while creating a collection of simple and enjoyable games.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Info card ─────────────────────────────────────────────────────
            Card(
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text  = "// PROFILE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow(label = "Developer", value = "Mohamed Hamdan")
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color    = MaterialTheme.colorScheme.surfaceVariant
                    )
                    InfoRow(label = "Role", value = "Student Developer")
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color    = MaterialTheme.colorScheme.surfaceVariant
                    )
                    InfoRow(label = "Interests", value = "Programming · Data Analytics · Mobile Dev · Games")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Social links ──────────────────────────────────────────────────
            Card(
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text  = "// CONNECT",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SocialButton(
                            label    = "GitHub",
                            icon     = Icons.Default.Code,
                            modifier = Modifier.weight(1f),
                            onClick  = {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://github.com/HamdanGitHub")
                                    )
                                )
                            }
                        )
                        SocialButton(
                            label    = "LinkedIn",
                            icon     = Icons.Default.Person,
                            modifier = Modifier.weight(1f),
                            onClick  = {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://www.linkedin.com/in/mohamed-hamdan-254927299")
                                    )
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── App info ──────────────────────────────────────────────────────
            Card(
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text  = "// APP INFO",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow(label = "App Name", value = "RetroArcade")
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color    = MaterialTheme.colorScheme.surfaceVariant
                    )
                    InfoRow(label = "Version", value = "1.0")
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color    = MaterialTheme.colorScheme.surfaceVariant
                    )
                    InfoRow(label = "Games", value = "Snake · Tetris · Pong · Memory · Tic Tac Toe")
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color    = MaterialTheme.colorScheme.surfaceVariant
                    )
                    InfoRow(label = "Built with", value = "Kotlin + Jetpack Compose")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.Top
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.bodySmall.copy(
                color      = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text      = value,
            style     = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            textAlign = TextAlign.End,
            modifier  = Modifier.weight(0.6f)
        )
    }
}

@Composable
private fun SocialButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick  = onClick,
        shape    = RoundedCornerShape(10.dp),
        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        modifier = modifier.height(44.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = label,
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.primary
            )
        )
    }
}
