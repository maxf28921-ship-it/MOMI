package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.VoiceProfileType
import com.example.ui.theme.NovaAccent
import com.example.ui.theme.NovaBg
import com.example.ui.theme.NovaPink
import com.example.ui.theme.NovaPurple
import com.example.ui.theme.NovaSuccess
import com.example.ui.theme.NovaSurface
import com.example.ui.theme.NovaSurfaceBorder
import com.example.ui.theme.NovaSurfaceVariant
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    initialApiKey: String,
    currentProfile: VoiceProfileType,
    initialPitch: Float,
    initialRate: Float,
    onSaveApiKey: (String) -> Unit,
    onSelectProfile: (VoiceProfileType) -> Unit,
    onSaveVoiceSliders: (Float, Float) -> Unit,
    onDismiss: () -> Unit
) {
    var apiKeyText by remember { mutableStateOf(initialApiKey) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var showSavedMessage by remember { mutableStateOf(false) }

    var expandedDropdown by remember { mutableStateOf(false) }
    var selectedProfile by remember { mutableStateOf(currentProfile) }

    var pitchValue by remember { mutableFloatStateOf(initialPitch) }
    var rateValue by remember { mutableFloatStateOf(initialRate) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("settings_modal"),
            shape = RoundedCornerShape(24.dp),
            color = NovaSurface,
            border = BorderStroke(1.dp, NovaSurfaceBorder),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Settings / सेटिंग्स",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaTextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("settings_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Settings",
                            tint = NovaTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // API Key Section
                Text(
                    text = "Gemini / Assistant API Key:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = NovaTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = apiKeyText,
                        onValueChange = { apiKeyText = it },
                        placeholder = { Text("Enter your API key...", color = NovaTextMuted, fontSize = 13.sp) },
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle visibility",
                                    tint = NovaTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
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
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("api_key_input")
                    )

                    Button(
                        onClick = {
                            onSaveApiKey(apiKeyText)
                            showSavedMessage = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NovaAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("save_api_key_button")
                    ) {
                        Text("Save", fontWeight = FontWeight.SemiBold)
                    }
                }

                AnimatedVisibility(visible = showSavedMessage) {
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .testTag("settings_success_message"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = NovaSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Settings saved successfully! (सेटिंग्स सुरक्षित हो गई!)",
                            fontSize = 12.sp,
                            color = NovaSuccess
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Voice Profile Selection
                Text(
                    text = "Nova Voice Profile / आवाज़ चयन:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = NovaTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = "${selectedProfile.title} (${selectedProfile.subtitle})",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NovaAccent,
                            unfocusedBorderColor = NovaSurfaceBorder,
                            focusedTextColor = NovaTextPrimary,
                            unfocusedTextColor = NovaTextPrimary,
                            focusedContainerColor = NovaBg,
                            unfocusedContainerColor = NovaBg
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("voice_profile_dropdown")
                    )

                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false },
                        modifier = Modifier
                            .background(NovaSurfaceVariant)
                            .border(1.dp, NovaSurfaceBorder, RoundedCornerShape(8.dp))
                    ) {
                        VoiceProfileType.entries.forEach { profile ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = profile.title,
                                            fontWeight = FontWeight.SemiBold,
                                            color = NovaTextPrimary,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = profile.subtitle,
                                            color = NovaTextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                },
                                onClick = {
                                    selectedProfile = profile
                                    expandedDropdown = false
                                    onSelectProfile(profile)
                                },
                                modifier = Modifier.testTag("voice_option_${profile.id}")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Pitch & Speed Sliders
                Text(
                    text = "Voice Pitch (सुर): ${(pitchValue * 100).toInt()}%",
                    fontSize = 13.sp,
                    color = NovaTextSecondary
                )
                Slider(
                    value = pitchValue,
                    onValueChange = {
                        pitchValue = it
                        onSaveVoiceSliders(pitchValue, rateValue)
                    },
                    valueRange = 0.6f..1.6f,
                    colors = SliderDefaults.colors(
                        thumbColor = NovaAccent,
                        activeTrackColor = NovaAccent,
                        inactiveTrackColor = NovaSurfaceBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Speaking Speed (गति): ${(rateValue * 100).toInt()}%",
                    fontSize = 13.sp,
                    color = NovaTextSecondary
                )
                Slider(
                    value = rateValue,
                    onValueChange = {
                        rateValue = it
                        onSaveVoiceSliders(pitchValue, rateValue)
                    },
                    valueRange = 0.7f..1.5f,
                    colors = SliderDefaults.colors(
                        thumbColor = NovaPink,
                        activeTrackColor = NovaPink,
                        inactiveTrackColor = NovaSurfaceBorder
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Test Voice Button
                Button(
                    onClick = {
                        onSelectProfile(selectedProfile)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NovaSurfaceVariant),
                    border = BorderStroke(1.dp, NovaAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_voice_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = "Test voice",
                        tint = NovaAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Test Nova Voice (आवाज़ सुनें)",
                        color = NovaTextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Engine info banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = NovaBg),
                    border = BorderStroke(1.dp, NovaSurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "💡 Dual AI Architecture",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = NovaAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Nova works seamlessly offline with high-speed built-in multilingual knowledge, and automatically connects to Gemini 3.5 Flash when an API key is active.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = NovaTextSecondary
                        )
                    }
                }
            }
        }
    }
}
