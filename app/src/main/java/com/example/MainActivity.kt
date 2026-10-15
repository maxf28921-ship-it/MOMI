package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AssistantStatus
import com.example.ui.components.HistoryDialog
import com.example.ui.components.LiquidVoiceOrb
import com.example.ui.components.QuickActionChips
import com.example.ui.components.ResponseCard
import com.example.ui.components.SettingsDialog
import com.example.ui.theme.MyApplicationTheme
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
import com.example.viewmodel.NovaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                NovaAssistantApp()
            }
        }
    }
}

@Composable
fun NovaAssistantApp(viewModel: NovaViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            viewModel.toggleListening(hasAudioPermission = true)
        } else {
            Toast.makeText(
                context,
                "Microphone permission not granted. Running in interactive speech simulation mode.",
                Toast.LENGTH_LONG
            ).show()
            viewModel.toggleListening(hasAudioPermission = false)
        }
    }

    var textQueryInput by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaBg)
            .statusBarsPadding(),
        containerColor = NovaBg,
        topBar = {
            NovaTopBar(
                historyCount = uiState.messages.size,
                onOpenHistory = { viewModel.setHistoryOpen(true) },
                onOpenSettings = { viewModel.setSettingsOpen(true) }
            )
        },
        bottomBar = {
            NovaBottomInputBar(
                queryText = textQueryInput,
                onQueryChange = { textQueryInput = it },
                onSend = {
                    if (textQueryInput.isNotBlank()) {
                        viewModel.processQuery(textQueryInput)
                        textQueryInput = ""
                    }
                },
                onMicClick = {
                    if (hasAudioPermission) {
                        viewModel.toggleListening(true)
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                isListening = uiState.status == AssistantStatus.LISTENING,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Liquid Voice Orb
                LiquidVoiceOrb(
                    status = uiState.status,
                    audioAmplitude = uiState.audioAmplitude,
                    onOrbClick = {
                        if (hasAudioPermission) {
                            viewModel.toggleListening(true)
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                )

                // Response Card
                ResponseCard(
                    responseText = uiState.currentResponse,
                    isSpeaking = uiState.status == AssistantStatus.SPEAKING,
                    onRepeatSpeech = { viewModel.repeatSpeech() },
                    onStopSpeech = { viewModel.stopSpeaking() }
                )

                // Quick Prompt Suggestion Pills
                VoiceSuggestionPills(
                    onSelect = { prompt -> viewModel.processQuery(prompt) }
                )

                // Quick Voice Commands Grid
                QuickActionChips(
                    onActionClick = { actionType ->
                        viewModel.triggerQuickAction(actionType)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Settings Modal
    if (uiState.isSettingsOpen) {
        SettingsDialog(
            initialApiKey = uiState.apiKey,
            currentProfile = uiState.voiceProfile,
            initialPitch = uiState.pitchFactor,
            initialRate = uiState.rateFactor,
            onSaveApiKey = { key -> viewModel.saveApiKey(key) },
            onSelectProfile = { profile -> viewModel.updateVoiceProfile(profile) },
            onSaveVoiceSliders = { pitch, rate -> viewModel.updateVoiceSliders(pitch, rate) },
            onDismiss = { viewModel.setSettingsOpen(false) }
        )
    }

    // History Dialog
    if (uiState.isHistoryOpen) {
        HistoryDialog(
            messages = uiState.messages,
            onSpeakMessage = { text ->
                viewModel.processQuery(text)
                viewModel.setHistoryOpen(false)
            },
            onClearHistory = { viewModel.clearHistory() },
            onDismiss = { viewModel.setHistoryOpen(false) }
        )
    }
}

@Composable
fun NovaTopBar(
    historyCount: Int,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("nova_header"),
        color = NovaSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, NovaSurfaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar 'N'
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(NovaAccent, NovaPurple, NovaPink))
                        )
                        .testTag("nova_avatar"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "N",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Column {
                    Text(
                        text = "Nova Assistant",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = NovaTextPrimary
                    )
                    Text(
                        text = "Voice AI Companion",
                        fontSize = 12.sp,
                        color = NovaTextSecondary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onOpenHistory,
                    modifier = Modifier.testTag("history_button")
                ) {
                    if (historyCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = NovaPink,
                                    contentColor = Color.White
                                ) {
                                    Text(historyCount.toString(), fontSize = 10.sp)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "History",
                                tint = NovaTextSecondary
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            tint = NovaTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = NovaTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceSuggestionPills(onSelect: (String) -> Unit) {
    val suggestions = listOf(
        "नमस्ते नोवा!" to "नमस्ते नोवा! आप कैसी हैं?",
        "আজকের আবহাওয়া কেমন?" to "আজকের আবহাওয়া কেমন?",
        "Tell me a joke" to "Tell me a funny joke",
        "কৌতুক শোনাও" to "একটি মজার কৌতুক শোনাও",
        "समय क्या हुआ है?" to "अभी समय क्या हुआ है?",
        "Who created you?" to "Who created you?"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        suggestions.forEach { (label, fullQuery) ->
            Card(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onSelect(fullQuery) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NovaSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, NovaSurfaceBorder)
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = NovaTextSecondary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
fun NovaBottomInputBar(
    queryText: String,
    onQueryChange: (String) -> Unit,
    onSend: () -> Unit,
    onMicClick: () -> Unit,
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bottom_input_bar"),
        color = NovaSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, NovaSurfaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(
                onClick = onMicClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isListening) NovaPink else NovaSurfaceVariant)
                    .testTag("mic_input_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Input",
                    tint = if (isListening) Color.White else NovaAccent,
                    modifier = Modifier.size(22.dp)
                )
            }

            OutlinedTextField(
                value = queryText,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = "Type message or tap mic...",
                        fontSize = 14.sp,
                        color = NovaTextMuted
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NovaAccent,
                    unfocusedBorderColor = NovaSurfaceBorder,
                    focusedTextColor = NovaTextPrimary,
                    unfocusedTextColor = NovaTextPrimary,
                    focusedContainerColor = NovaBg,
                    unfocusedContainerColor = NovaBg
                ),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("query_text_input")
            )

            IconButton(
                onClick = onSend,
                enabled = queryText.isNotBlank(),
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (queryText.isNotBlank()) NovaAccent else NovaSurfaceVariant)
                    .testTag("send_query_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (queryText.isNotBlank()) Color.White else NovaTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
