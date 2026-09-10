package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.AppStrings
import com.example.model.FocusModeOption
import com.example.model.ManualControlMode
import com.example.model.WhiteBalancePreset
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CameraBlack
import com.example.ui.theme.CameraBorder
import com.example.ui.theme.CameraDarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

val ISO_OPTIONS = listOf(0, 50, 100, 200, 400, 800, 1600, 3200, 6400)
val SHUTTER_SPEEDS = listOf(
    "AUTO" to 0L,
    "1/4000" to 250_000L,
    "1/2000" to 500_000L,
    "1/1000" to 1_000_000L,
    "1/500" to 2_000_000L,
    "1/250" to 4_000_000L,
    "1/125" to 8_000_000L,
    "1/60" to 16_666_667L,
    "1/30" to 33_333_333L,
    "1/15" to 66_666_667L,
    "1/8" to 125_000_000L,
    "1/4" to 250_000_000L,
    "1/2" to 500_000_000L,
    "1\"" to 1_000_000_000L,
    "2\"" to 2_000_000_000L
)

@Composable
fun ManualControlsBar(
    language: AppLanguage,
    activeMode: ManualControlMode,
    onModeSelected: (ManualControlMode) -> Unit,
    evIndex: Int,
    evStep: Float,
    onEvChanged: (Int) -> Unit,
    selectedIso: Int,
    onIsoChanged: (Int) -> Unit,
    selectedShutterIndex: Int,
    onShutterChanged: (Int) -> Unit,
    selectedWb: WhiteBalancePreset,
    onWbChanged: (WhiteBalancePreset) -> Unit,
    focusMode: FocusModeOption,
    onFocusModeChanged: (FocusModeOption) -> Unit,
    manualFocusDistance: Float,
    onManualFocusDistanceChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CameraBlack.copy(alpha = 0.94f))
            .padding(vertical = 8.dp)
    ) {
        // Active mode title header
        Text(
            text = AppStrings.modeTitle(language, activeMode),
            fontSize = 11.sp,
            color = AmberGold,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 4.dp)
        )

        // 1. Parameter Adjuster Row (Animated based on active mode)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = activeMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "parameter_adjuster"
            ) { mode ->
                when (mode) {
                    ManualControlMode.EV -> {
                        EvSliderBar(
                            language = language,
                            evIndex = evIndex,
                            evStep = evStep,
                            onEvChanged = onEvChanged
                        )
                    }
                    ManualControlMode.ISO -> {
                        IsoPickerRow(
                            selectedIso = selectedIso,
                            onIsoChanged = onIsoChanged
                        )
                    }
                    ManualControlMode.SEC -> {
                        ShutterSpeedPickerRow(
                            selectedIndex = selectedShutterIndex,
                            onShutterChanged = onShutterChanged
                        )
                    }
                    ManualControlMode.WB -> {
                        WhiteBalancePickerRow(
                            language = language,
                            selectedWb = selectedWb,
                            onWbChanged = onWbChanged
                        )
                    }
                    ManualControlMode.FOCUS -> {
                        FocusControlRow(
                            language = language,
                            focusMode = focusMode,
                            onFocusModeChanged = onFocusModeChanged,
                            focusDistance = manualFocusDistance,
                            onFocusDistanceChanged = onManualFocusDistanceChanged
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Control Mode Tabs (DSLR Mode Selector)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // EV Tab
            val evText = String.format(Locale.US, "%+.1f", evIndex * evStep)
            ControlModeTab(
                label = "EV",
                value = evText,
                isActive = activeMode == ManualControlMode.EV,
                onClick = { onModeSelected(ManualControlMode.EV) },
                testTag = "control_mode_ev"
            )

            // ISO Tab
            val isoText = if (selectedIso == 0) "AUTO" else "$selectedIso"
            ControlModeTab(
                label = "ISO",
                value = isoText,
                isActive = activeMode == ManualControlMode.ISO,
                onClick = { onModeSelected(ManualControlMode.ISO) },
                testTag = "control_mode_iso"
            )

            // SEC Tab
            val secText = SHUTTER_SPEEDS.getOrNull(selectedShutterIndex)?.first ?: "AUTO"
            ControlModeTab(
                label = if (language == AppLanguage.ARABIC) "غالق" else "SEC",
                value = secText,
                isActive = activeMode == ManualControlMode.SEC,
                onClick = { onModeSelected(ManualControlMode.SEC) },
                testTag = "control_mode_sec"
            )

            // WB Tab
            ControlModeTab(
                label = if (language == AppLanguage.ARABIC) "أبيض" else "WB",
                value = selectedWb.kelvinText,
                isActive = activeMode == ManualControlMode.WB,
                onClick = { onModeSelected(ManualControlMode.WB) },
                testTag = "control_mode_wb"
            )

            // FOCUS Tab
            val focusText = when (focusMode) {
                FocusModeOption.AF_C -> "AF-C"
                FocusModeOption.AF_S -> "AF-S"
                FocusModeOption.MANUAL -> String.format(Locale.US, "%.1fm", manualFocusDistance * 3.0f)
            }
            ControlModeTab(
                label = if (language == AppLanguage.ARABIC) "تركيز" else "FOCUS",
                value = focusText,
                isActive = activeMode == ManualControlMode.FOCUS,
                onClick = { onModeSelected(ManualControlMode.FOCUS) },
                testTag = "control_mode_focus"
            )
        }
    }
}

@Composable
fun ControlModeTab(
    label: String,
    value: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) AmberGold.copy(alpha = 0.18f) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (isActive) AmberGold else CameraBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isActive) AmberGold else TextSecondary,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) Color.White else TextMuted,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun EvSliderBar(
    language: AppLanguage,
    evIndex: Int,
    evStep: Float,
    onEvChanged: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "-2.0",
            fontSize = 10.sp,
            color = TextSecondary,
            fontFamily = FontFamily.Monospace
        )

        Slider(
            value = evIndex.toFloat(),
            onValueChange = { onEvChanged(it.toInt()) },
            valueRange = -6f..6f,
            steps = 11,
            colors = SliderDefaults.colors(
                thumbColor = AmberGold,
                activeTrackColor = AmberGold,
                inactiveTrackColor = CameraBorder
            ),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .testTag("ev_slider")
        )

        Text(
            text = "+2.0",
            fontSize = 10.sp,
            color = TextSecondary,
            fontFamily = FontFamily.Monospace
        )

        // Reset to 0 EV button
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .clip(CircleShape)
                .background(CameraDarkSurface)
                .border(1.dp, CameraBorder, CircleShape)
                .clickable { onEvChanged(0) }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "0",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (evIndex == 0) AmberGold else TextSecondary,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun IsoPickerRow(
    selectedIso: Int,
    onIsoChanged: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (iso in ISO_OPTIONS) {
            val isSelected = selectedIso == iso
            val label = if (iso == 0) "AUTO" else "$iso"
            PillOptionChip(
                label = label,
                isSelected = isSelected,
                onClick = { onIsoChanged(iso) },
                testTag = "iso_chip_$label"
            )
        }
    }
}

@Composable
fun ShutterSpeedPickerRow(
    selectedIndex: Int,
    onShutterChanged: (Int) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SHUTTER_SPEEDS.forEachIndexed { index, (label, _) ->
            val isSelected = selectedIndex == index
            PillOptionChip(
                label = label,
                isSelected = isSelected,
                onClick = { onShutterChanged(index) },
                testTag = "shutter_chip_$label"
            )
        }
    }
}

@Composable
fun WhiteBalancePickerRow(
    language: AppLanguage,
    selectedWb: WhiteBalancePreset,
    onWbChanged: (WhiteBalancePreset) -> Unit
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (preset in WhiteBalancePreset.entries) {
            val isSelected = selectedWb == preset
            val name = AppStrings.whiteBalance(language, preset)
            PillOptionChip(
                label = "$name (${preset.kelvinText})",
                isSelected = isSelected,
                onClick = { onWbChanged(preset) },
                testTag = "wb_chip_${preset.name}"
            )
        }
    }
}

@Composable
fun FocusControlRow(
    language: AppLanguage,
    focusMode: FocusModeOption,
    onFocusModeChanged: (FocusModeOption) -> Unit,
    focusDistance: Float,
    onFocusDistanceChanged: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mode switch pills: AF-C, AF-S, MF
        Row(
            modifier = Modifier
                .background(CameraDarkSurface, RoundedCornerShape(8.dp))
                .border(1.dp, CameraBorder, RoundedCornerShape(8.dp))
                .padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            for (mode in FocusModeOption.entries) {
                val isSelected = focusMode == mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) AmberGold else Color.Transparent)
                        .clickable { onFocusModeChanged(mode) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("focus_mode_${mode.name}")
                ) {
                    Text(
                        text = mode.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) CameraBlack else TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // If in MF mode, display distance slider
        if (focusMode == FocusModeOption.MANUAL) {
            Icon(
                imageVector = Icons.Default.LocalFlorist,
                contentDescription = AppStrings.macro(language),
                tint = AmberGold,
                modifier = Modifier.size(18.dp)
            )
            Slider(
                value = focusDistance,
                onValueChange = onFocusDistanceChanged,
                valueRange = 0.0f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = AmberGold,
                    activeTrackColor = AmberGold,
                    inactiveTrackColor = CameraBorder
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .testTag("manual_focus_slider")
            )
            Icon(
                imageVector = Icons.Default.AllInclusive,
                contentDescription = AppStrings.infinity(language),
                tint = AmberGold,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = if (focusMode == FocusModeOption.AF_C) {
                    if (language == AppLanguage.ARABIC) "تركيز تلقائي مستمر نشط" else "Continuous Autofocus Active"
                } else {
                    if (language == AppLanguage.ARABIC) "تركيز تلقائي مفرد مقفل" else "Single Autofocus Locked"
                },
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
fun PillOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) AmberGold else CameraDarkSurface)
            .border(
                1.dp,
                if (isSelected) AmberGold else CameraBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) CameraBlack else TextPrimary,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )
    }
}
