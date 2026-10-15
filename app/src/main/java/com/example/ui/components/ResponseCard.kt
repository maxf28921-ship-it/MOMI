package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NovaAccent
import com.example.ui.theme.NovaBg
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaPurple
import com.example.ui.theme.NovaSurfaceBorder
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary

@Composable
fun ResponseCard(
    responseText: String,
    isSpeaking: Boolean,
    onRepeatSpeech: () -> Unit,
    onStopSpeech: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AnimatedVisibility(
        visible = responseText.isNotBlank(),
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("response_box"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NovaBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, NovaSurfaceBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(listOf(NovaAccent, NovaPurple))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "N",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Nova Voice Response",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NovaTextSecondary
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                if (isSpeaking) {
                                    onStopSpeech()
                                } else {
                                    onRepeatSpeech()
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("replay_voice_button")
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                contentDescription = if (isSpeaking) "Stop voice" else "Repeat voice",
                                tint = if (isSpeaking) NovaPink else NovaAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Nova Response", responseText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("copy_response_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy text",
                                tint = NovaTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = responseText,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = NovaTextPrimary,
                    modifier = Modifier.testTag("response_text")
                )
            }
        }
    }
}
