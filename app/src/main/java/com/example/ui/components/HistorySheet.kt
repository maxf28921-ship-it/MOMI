package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ChatMessage
import com.example.ui.theme.NovaAccent
import com.example.ui.theme.NovaBg
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaPurple
import com.example.ui.theme.NovaSurface
import com.example.ui.theme.NovaSurfaceBorder
import com.example.ui.theme.NovaSurfaceVariant
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryDialog(
    messages: List<ChatMessage>,
    onSpeakMessage: (String) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(520.dp)
                .testTag("history_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = NovaSurface,
            border = BorderStroke(1.dp, NovaSurfaceBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Voice History / কথোপকথন",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaTextPrimary
                    )

                    Row {
                        if (messages.isNotEmpty()) {
                            IconButton(
                                onClick = onClearHistory,
                                modifier = Modifier.testTag("clear_history_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = "Clear History",
                                    tint = NovaTextMuted
                                )
                            }
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = NovaTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No voice interactions yet.\nTap the glowing orb to start speaking!",
                            color = NovaTextMuted,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(messages) { msg ->
                            HistoryMessageItem(
                                message = msg,
                                onSpeak = { onSpeakMessage(msg.text) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryMessageItem(
    message: ChatMessage,
    onSpeak: () -> Unit
) {
    val isUser = message.isUser
    val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUser) NovaSurfaceVariant else NovaBg
        ),
        border = BorderStroke(1.dp, NovaSurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                if (isUser) {
                                    Brush.linearGradient(listOf(NovaPink, NovaPurple))
                                } else {
                                    Brush.linearGradient(listOf(NovaAccent, NovaPurple))
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isUser) "U" else "N",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isUser) "You" else "Nova",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NovaTextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeStr,
                        fontSize = 10.sp,
                        color = NovaTextMuted
                    )
                    if (!isUser) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Listen",
                                tint = NovaAccent,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message.text,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = NovaTextPrimary
            )
        }
    }
}
