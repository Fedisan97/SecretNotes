package com.secretnotes.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.secretnotes.app.ui.theme.Gold
import com.secretnotes.app.ui.theme.GoldDim
import com.secretnotes.app.ui.theme.Ink
import kotlin.random.Random

@Composable
fun VaultStage(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val spin by rememberInfiniteTransition(label = "vault").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(48_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )
    val grain = remember {
        List(180) {
            Offset(Random.nextFloat(), Random.nextFloat()) to Random.nextFloat()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0B0806), Ink, Color(0xFF050403))
                )
            )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height * 0.28f
            rotate(spin, pivot = Offset(cx, cy)) {
                drawCircle(
                    color = Gold.copy(alpha = 0.18f),
                    radius = size.minDimension * 0.42f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.2f)
                )
            }
            rotate(-spin * 0.6f, pivot = Offset(cx, cy)) {
                drawCircle(
                    color = GoldDim.copy(alpha = 0.35f),
                    radius = size.minDimension * 0.28f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.4f)
                )
            }
            drawCircle(
                color = Gold.copy(alpha = 0.08f),
                radius = size.minDimension * 0.16f,
                center = Offset(cx, cy)
            )
            grain.forEach { (p, a) ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.025f + a * 0.04f),
                    radius = 1.1f,
                    center = Offset(p.x * size.width, p.y * size.height)
                )
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(28.dp)
                .align(Alignment.TopCenter)
                .background(Color.Black)
        )
        Box(
            Modifier
                .fillMaxWidth()
                .height(28.dp)
                .align(Alignment.BottomCenter)
                .background(Color.Black)
        )
        content()
    }
}
