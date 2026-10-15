package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.QuickActionItem
import com.example.ui.theme.NovaAccent
import com.example.ui.theme.NovaSurface
import com.example.ui.theme.NovaSurfaceBorder
import com.example.ui.theme.NovaSurfaceVariant
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary

val defaultQuickActions = listOf(
    QuickActionItem("call", "📞", "Call Family", "মা / परिवार को कॉल", "call"),
    QuickActionItem("message", "💬", "Send Message", "বার্তা / संदेश भेजें", "message"),
    QuickActionItem("apps", "📱", "Open Apps", "অ্যাপ্স / ऐप्स खोलें", "apps"),
    QuickActionItem("notifs", "🔔", "Read Alerts", "নোটিফিকেশন / सूचनाएं", "notifs"),
    QuickActionItem("weather", "🌦️", "Weather", "আবহাওয়া / मौसम", "weather"),
    QuickActionItem("alarm", "⏰", "Set Alarm", "অ্যালার্ম / अलार्म", "alarm"),
    QuickActionItem("joke", "😄", "Tell a Joke", "কৌতুক / चुटकुला", "joke")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickActionChips(
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quick_actions_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NovaSurface),
        border = BorderStroke(1.dp, NovaSurfaceBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                Text(
                    text = "Quick Voice Commands",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NovaTextPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "দ্রুত নির্দেশাবলী",
                    fontSize = 12.sp,
                    color = NovaAccent
                )
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 2
            ) {
                defaultQuickActions.forEach { item ->
                    ActionChipItem(
                        item = item,
                        onClick = { onActionClick(item.commandPrompt) },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 64.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ActionChipItem(
    item: QuickActionItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("quick_action_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NovaSurfaceVariant),
        border = BorderStroke(1.dp, NovaSurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${item.iconEmoji} ${item.titleEn}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = NovaTextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.titleNative,
                fontSize = 11.sp,
                color = NovaTextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}
