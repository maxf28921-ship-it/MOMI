package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssistantStatus
import com.example.ui.theme.NovaAccent
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaPurple
import com.example.ui.theme.NovaSurface
import com.example.ui.theme.NovaSurfaceBorder
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary

@Composable
fun LiquidVoiceOrb(
    status: AssistantStatus,
    audioAmplitude: Float,
    onOrbClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

    // Ambient pulsing scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Outer glow ring expand
    val ringExpand by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_expand"
    )

    // Ring alpha fade
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_alpha"
    )

    // Gradient shift angle
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_rotation"
    )

    val isListening = status == AssistantStatus.LISTENING
    val isSpeaking = status == AssistantStatus.SPEAKING
    val isThinking = status == AssistantStatus.THINKING

    // Dynamic scale influenced by audio amplitude
    val liveScale = if (isListening || isSpeaking) {
        1.0f + (audioAmplitude * 0.16f)
    } else {
        pulseScale
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("voice_orb_container"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = NovaSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, NovaSurfaceBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Orb stack with outer canvas aura
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onOrbClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Outer sonic rings canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val baseRadius = size.minDimension / 2.7f

                    if (isListening || isSpeaking) {
                        // Dynamic sonic waves
                        val waveRadius1 = baseRadius * (1.1f + audioAmplitude * 0.35f)
                        val waveRadius2 = baseRadius * ringExpand

                        drawCircle(
                            color = NovaPink.copy(alpha = if (isListening) 0.5f else 0.35f),
                            radius = waveRadius1,
                            center = center,
                            style = Stroke(width = 3.dp.toPx())
                        )
                        drawCircle(
                            color = NovaAccent.copy(alpha = ringAlpha),
                            radius = waveRadius2,
                            center = center,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    } else {
                        // Ambient resting halo
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    NovaPurple.copy(alpha = 0.3f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = baseRadius * 1.35f
                            ),
                            radius = baseRadius * 1.35f,
                            center = center
                        )
                    }
                }

                // Middle blur glow backing
                Box(
                    modifier = Modifier
                        .size(145.dp)
                        .scale(if (isListening) 1.15f else 1.05f)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = if (isListening) {
                                    listOf(NovaPink.copy(alpha = 0.7f), NovaPurple.copy(alpha = 0.3f), Color.Transparent)
                                } else if (isSpeaking) {
                                    listOf(NovaCyan.copy(alpha = 0.6f), NovaAccent.copy(alpha = 0.3f), Color.Transparent)
                                } else {
                                    listOf(NovaAccent.copy(alpha = 0.5f), NovaPurple.copy(alpha = 0.2f), Color.Transparent)
                                }
                            )
                        )
                )

                // Core Liquid Orb
                val orbGradient = if (isListening) {
                    Brush.sweepGradient(
                        listOf(NovaPink, NovaPurple, NovaAccent, NovaPink)
                    )
                } else if (isSpeaking) {
                    Brush.sweepGradient(
                        listOf(NovaCyan, NovaAccent, NovaPurple, NovaCyan)
                    )
                } else if (isThinking) {
                    Brush.sweepGradient(
                        listOf(NovaPurple, NovaAccent, NovaPink, NovaPurple)
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(NovaAccent, NovaPurple, NovaPink),
                        start = Offset(0f, 0f),
                        end = Offset(130f, 130f)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(liveScale)
                        .clip(CircleShape)
                        .background(orbGradient)
                        .testTag("voice_orb_button"),
                    contentAlignment = Alignment.Center
                ) {
                    // Glass shine highlight overlay
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                                center = Offset(size.width * 0.35f, size.height * 0.35f),
                                radius = size.minDimension * 0.45f
                            )
                        )
                    }

                    // Center Icon
                    when (status) {
                        AssistantStatus.LISTENING -> {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Listening Audio",
                                tint = Color.White,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                        AssistantStatus.SPEAKING -> {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop Speaking",
                                tint = Color.White,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                        AssistantStatus.THINKING -> {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Thinking",
                                tint = Color.White,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                        AssistantStatus.IDLE -> {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Tap to Talk to Nova",
                                tint = Color.White,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sound Wave Audio Visualizer (Live frequency bars when active)
            if (isListening || isSpeaking || isThinking) {
                AudioWaveVisualizer(
                    amplitude = audioAmplitude,
                    isSpeaking = isSpeaking,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(26.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Primary Status text
            val statusTitle = when (status) {
                AssistantStatus.IDLE -> "Tap the orb to talk to Nova"
                AssistantStatus.LISTENING -> "Listening... सुन रही हूँ... শুনছি..."
                AssistantStatus.THINKING -> "Thinking... सोच रही हूँ... ভাবছি..."
                AssistantStatus.SPEAKING -> "Nova is speaking... बोल रही हूँ..."
            }

            Text(
                text = statusTitle,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isListening) NovaPink else NovaTextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("status_text")
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Secondary Subtitle
            val statusSubtitle = when (status) {
                AssistantStatus.IDLE -> "Supports Hindi & Bengali (\"सुन रही हूँ\", \"শুনছি\")"
                AssistantStatus.LISTENING -> "Speak clearly into your phone"
                AssistantStatus.THINKING -> "Processing your voice command..."
                AssistantStatus.SPEAKING -> "Tap the orb to interrupt"
            }

            Text(
                text = statusSubtitle,
                fontSize = 13.sp,
                color = NovaTextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AudioWaveVisualizer(
    amplitude: Float,
    isSpeaking: Boolean,
    modifier: Modifier = Modifier
) {
    val barCount = 11
    val baseColor = if (isSpeaking) NovaCyan else NovaPink

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            // Distance from center
            val dist = kotlin.math.abs(i - (barCount / 2f)) / (barCount / 2f)
            val factor = (1f - dist * 0.6f)
            val barHeight = (8f + (18f * amplitude * factor)).dp.coerceIn(4.dp, 24.dp)

            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = barHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(baseColor.copy(alpha = 0.5f + (0.5f * factor)))
            )
        }
    }
}
