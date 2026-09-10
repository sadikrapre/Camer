package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AvTimer
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.AppStrings
import com.example.model.CinematicFilter
import com.example.model.FlashMode
import com.example.model.GridType
import com.example.model.LensOption
import com.example.model.TimerOption
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CameraBlack
import com.example.ui.theme.CameraBorder
import com.example.ui.theme.CameraDarkSurface
import com.example.ui.theme.LevelGreen
import com.example.ui.theme.ShutterRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun TopBarControls(
    language: AppLanguage,
    onLanguageToggle: () -> Unit,
    flashMode: FlashMode,
    onFlashModeCycle: () -> Unit,
    currentLens: LensOption,
    onLensSelected: (LensOption) -> Unit,
    gridType: GridType,
    onGridTypeCycle: () -> Unit,
    showSpiritLevel: Boolean,
    onSpiritLevelToggle: () -> Unit,
    showHistogram: Boolean,
    onHistogramToggle: () -> Unit,
    timerOption: TimerOption,
    onTimerCycle: () -> Unit,
    onFlipCamera: () -> Unit,
    // New Pro additions
    selectedFilter: CinematicFilter,
    isFilterSelectorOpen: Boolean,
    onToggleFilterSelector: () -> Unit,
    isCinematicActive: Boolean,
    onToggleCinematic: () -> Unit,
    isDualPipActive: Boolean,
    onToggleDualPip: () -> Unit,
    isRecording: Boolean,
    recordingDurationSeconds: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rec_blink")
    val recAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rec_dot_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CameraBlack.copy(alpha = 0.92f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Video Recording LIVE Banner
        AnimatedVisibility(visible = isRecording) {
            val mins = recordingDurationSeconds / 60
            val secs = recordingDurationSeconds % 60
            val durStr = String.format(Locale.US, "%02d:%02d", mins, secs)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2A0D0D))
                    .border(1.dp, ShutterRed, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FiberManualRecord,
                    contentDescription = null,
                    tint = ShutterRed.copy(alpha = recAlpha),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "REC  $durStr",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                )
                if (isDualPipActive) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "• DUAL CAM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberGold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                if (isCinematicActive) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• 2.39:1 (24FPS)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = LevelGreen,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Upper utility icons row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flash Mode
            IconButton(
                onClick = onFlashModeCycle,
                modifier = Modifier
                    .size(42.dp)
                    .testTag("flash_toggle_button")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val icon = when (flashMode) {
                        FlashMode.OFF -> Icons.Default.FlashOff
                        FlashMode.AUTO -> Icons.Default.FlashAuto
                        FlashMode.ON -> Icons.Default.FlashOn
                        FlashMode.TORCH -> Icons.Default.Highlight
                    }
                    val tint = if (flashMode == FlashMode.OFF) TextSecondary else AmberGold
                    Icon(imageVector = icon, contentDescription = "Flash", tint = tint, modifier = Modifier.size(17.dp))
                    Text(
                        text = AppStrings.flash(language, flashMode),
                        fontSize = 8.sp,
                        color = tint,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Cinematic Filter / LUTs Picker
            IconButton(
                onClick = onToggleFilterSelector,
                modifier = Modifier
                    .size(42.dp)
                    .testTag("cinematic_filters_button")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val hasFilter = selectedFilter != CinematicFilter.NONE
                    val tint = if (isFilterSelectorOpen || hasFilter) Color(selectedFilter.primaryColorHex) else TextSecondary
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Filters", tint = tint, modifier = Modifier.size(17.dp))
                    Text(
                        text = if (hasFilter) selectedFilter.titleEn.take(4).uppercase() else "LUT",
                        fontSize = 8.sp,
                        color = tint,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Cinematic 2.39:1 Toggle
            IconButton(
                onClick = onToggleCinematic,
                modifier = Modifier
                    .size(42.dp)
                    .testTag("cinematic_aspect_button")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val tint = if (isCinematicActive) AmberGold else TextMuted
                    Icon(imageVector = Icons.Default.Movie, contentDescription = "Cinematic 2.39:1", tint = tint, modifier = Modifier.size(17.dp))
                    Text(
                        text = if (isCinematicActive) "2.39" else "16:9",
                        fontSize = 8.sp,
                        color = tint,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Dual PIP Mode Toggle (Front + Back simultaneously)
            IconButton(
                onClick = onToggleDualPip,
                modifier = Modifier
                    .size(42.dp)
                    .testTag("dual_pip_toggle_button")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val tint = if (isDualPipActive) AmberGold else TextMuted
                    Icon(imageVector = Icons.Default.PictureInPicture, contentDescription = "Dual PIP", tint = tint, modifier = Modifier.size(17.dp))
                    Text(
                        text = "DUAL",
                        fontSize = 8.sp,
                        color = tint,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Grid Overlay Toggle
            IconButton(
                onClick = onGridTypeCycle,
                modifier = Modifier
                    .size(42.dp)
                    .testTag("grid_toggle_button")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val tint = if (gridType != GridType.NONE) AmberGold else TextSecondary
                    Icon(imageVector = Icons.Default.GridOn, contentDescription = "Grid", tint = tint, modifier = Modifier.size(17.dp))
                    Text(
                        text = AppStrings.grid(language, gridType),
                        fontSize = 8.sp,
                        color = tint,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Spirit Level Toggle
            IconButton(
                onClick = onSpiritLevelToggle,
                modifier = Modifier
                    .size(42.dp)
                    .testTag("spirit_level_toggle_button")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val tint = if (showSpiritLevel) LevelGreen else TextMuted
                    Icon(imageVector = Icons.Default.Speed, contentDescription = "Level", tint = tint, modifier = Modifier.size(17.dp))
                    Text(
                        text = AppStrings.level(language),
                        fontSize = 8.sp,
                        color = tint,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Live Histogram Toggle
            IconButton(
                onClick = onHistogramToggle,
                modifier = Modifier
                    .size(42.dp)
                    .testTag("histogram_toggle_button")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val tint = if (showHistogram) AmberGold else TextMuted
                    Icon(imageVector = Icons.Default.ShowChart, contentDescription = "Histogram", tint = tint, modifier = Modifier.size(17.dp))
                    Text(
                        text = AppStrings.histo(language),
                        fontSize = 8.sp,
                        color = tint,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Flip Camera
            IconButton(
                onClick = onFlipCamera,
                modifier = Modifier
                    .size(42.dp)
                    .testTag("camera_switch_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Switch Camera",
                    tint = TextPrimary,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        // Lower Row: Lens Switcher Pill & Quick Language Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lens Switcher: 0.5x, 1.0x, 2.0x, 5.0x
            Row(
                modifier = Modifier
                    .background(CameraBlack.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .border(0.5.dp, CameraBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (lens in LensOption.entries) {
                    val isSelected = currentLens == lens
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) AmberGold else Color.Transparent)
                            .clickable { onLensSelected(lens) }
                            .testTag("lens_option_${lens.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = lens.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) CameraBlack else TextPrimary,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Language Switcher Badge (عربي / EN)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CameraBlack.copy(alpha = 0.7f))
                    .border(1.dp, AmberGold.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                    .clickable { onLanguageToggle() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("language_toggle_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Change Language",
                        tint = AmberGold,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (language == AppLanguage.ARABIC) "العربية" else "English",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberGold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

