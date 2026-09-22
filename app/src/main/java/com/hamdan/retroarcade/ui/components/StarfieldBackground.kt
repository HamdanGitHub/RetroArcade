package com.hamdan.retroarcade.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

private data class Star(
    val x: Float,         // 0..1 normalised
    val y: Float,         // 0..1 normalised
    val radius: Float,    // px
    val speed: Float,     // scroll speed multiplier
    val alpha: Float      // base opacity
)

@Composable
fun StarfieldBackground(
    modifier: Modifier = Modifier,
    starCount: Int = 80,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable BoxScope.() -> Unit
) {
    val stars = remember {
        List(starCount) {
            Star(
                x      = Random.nextFloat(),
                y      = Random.nextFloat(),
                radius = Random.nextFloat() * 2.2f + 0.4f,
                speed  = Random.nextFloat() * 0.4f + 0.05f,
                alpha  = Random.nextFloat() * 0.6f + 0.2f
            )
        }
    }

    // Slow vertical scroll offset (0..1 looping)
    val transition = rememberInfiniteTransition(label = "starfield")
    val offset by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 18_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "starOffset"
    )

    // Twinkle pulse
    val twinkle by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = 2_400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Draw each star with parallax scroll
            stars.forEachIndexed { i, star ->
                val scrolledY = ((star.y + offset * star.speed) % 1f)
                val twinkleFactor = if (i % 3 == 0) {
                    star.alpha * (0.6f + 0.4f * twinkle)
                } else star.alpha

                drawCircle(
                    color  = if (i % 5 == 0) primaryColor.copy(alpha = twinkleFactor * 0.8f)
                             else Color.White.copy(alpha = twinkleFactor),
                    radius = star.radius,
                    center = Offset(star.x * w, scrolledY * h)
                )
            }

            // Subtle horizontal scan line effect
            for (i in 0..8) {
                val scanY = h * i / 8f
                drawLine(
                    color       = Color.White.copy(alpha = 0.015f),
                    start       = Offset(0f, scanY),
                    end         = Offset(w, scanY),
                    strokeWidth = 1f
                )
            }
        }

        content()
    }
}

/**
 * Lightweight version for overlaying on dark game backgrounds (no Box, just Canvas).
 * Use this inside existing game Boxes where you want stars behind game elements.
 */
@Composable
fun StarfieldCanvas(
    modifier: Modifier = Modifier,
    starCount: Int = 50,
    primaryColor: Color = Color(0xFF39FF14)
) {
    val stars = remember {
        List(starCount) {
            Star(
                x      = Random.nextFloat(),
                y      = Random.nextFloat(),
                radius = Random.nextFloat() * 1.8f + 0.3f,
                speed  = Random.nextFloat() * 0.3f + 0.04f,
                alpha  = Random.nextFloat() * 0.5f + 0.15f
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "starfieldCanvas")
    val offset by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(22_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "starOffsetCanvas"
    )

    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        stars.forEachIndexed { i, star ->
            val sy = ((star.y + offset * star.speed) % 1f)
            drawCircle(
                color  = if (i % 4 == 0) primaryColor.copy(alpha = star.alpha * 0.7f)
                         else Color.White.copy(alpha = star.alpha),
                radius = star.radius,
                center = Offset(star.x * w, sy * h)
            )
        }
    }
}
