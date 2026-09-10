package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AppLanguage
import com.example.model.AppStrings
import com.example.model.GridType
import com.example.model.TimerOption
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CameraBlack
import com.example.ui.theme.CameraBorder
import com.example.ui.theme.CameraDarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun QuickSettingsDialog(
    language: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    gridType: GridType,
    onGridTypeSelected: (GridType) -> Unit,
    showSpiritLevel: Boolean,
    onSpiritLevelToggle: () -> Unit,
    showHistogram: Boolean,
    onHistogramToggle: () -> Unit,
    timerOption: TimerOption,
    onTimerOptionSelected: (TimerOption) -> Unit,
    hapticsEnabled: Boolean,
    onHapticsToggle: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CameraDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CameraBorder, RoundedCornerShape(20.dp))
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Camera,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = AppStrings.settingsTitle(language),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("close_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = AppStrings.close(language),
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Language Selection Section
                Text(
                    text = AppStrings.languageSection(language),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberGold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (lang in AppLanguage.entries) {
                        val isSelected = language == lang
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AmberGold else CameraBlack)
                                .border(1.dp, if (isSelected) AmberGold else CameraBorder, RoundedCornerShape(10.dp))
                                .clickable { onLanguageSelected(lang) }
                                .padding(vertical = 10.dp)
                                .testTag("language_option_${lang.code}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = if (isSelected) CameraBlack else TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = lang.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) CameraBlack else TextPrimary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Composition Grids
                Text(
                    text = AppStrings.gridSection(language),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberGold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (g in GridType.entries) {
                        val isSelected = gridType == g
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AmberGold else CameraBlack)
                                .border(1.dp, if (isSelected) AmberGold else CameraBorder, RoundedCornerShape(8.dp))
                                .clickable { onGridTypeSelected(g) }
                                .padding(vertical = 8.dp)
                                .testTag("grid_option_${g.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = AppStrings.grid(language, g),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) CameraBlack else TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Self-Timer
                Text(
                    text = AppStrings.timerSection(language),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberGold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (t in TimerOption.entries) {
                        val isSelected = timerOption == t
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AmberGold else CameraBlack)
                                .border(1.dp, if (isSelected) AmberGold else CameraBorder, RoundedCornerShape(8.dp))
                                .clickable { onTimerOptionSelected(t) }
                                .padding(vertical = 8.dp)
                                .testTag("timer_option_${t.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = AppStrings.timer(language, t),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) CameraBlack else TextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // HUD Toggles
                SettingToggleRow(
                    icon = Icons.Default.Sensors,
                    title = AppStrings.spiritLevelTitle(language),
                    subtitle = AppStrings.spiritLevelSub(language),
                    isChecked = showSpiritLevel,
                    onCheckedChange = { onSpiritLevelToggle() }
                )

                SettingToggleRow(
                    icon = Icons.Default.ShowChart,
                    title = AppStrings.histogramTitle(language),
                    subtitle = AppStrings.histogramSub(language),
                    isChecked = showHistogram,
                    onCheckedChange = { onHistogramToggle() }
                )

                SettingToggleRow(
                    icon = Icons.Default.Vibration,
                    title = AppStrings.hapticsTitle(language),
                    subtitle = AppStrings.hapticsSub(language),
                    isChecked = hapticsEnabled,
                    onCheckedChange = { onHapticsToggle() }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Diagnostics summary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CameraBlack, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "FORMAT: JPEG/DNG", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                    Text(text = "ENGINE: CameraX 1.5", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isChecked) AmberGold else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(text = subtitle, fontSize = 10.sp, color = TextSecondary)
            }
        }

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CameraBlack,
                checkedTrackColor = AmberGold,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = CameraBlack
            )
        )
    }
}
