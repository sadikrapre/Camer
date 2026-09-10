package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.model.AppLanguage
import com.example.model.AppStrings
import com.example.model.CameraShootingMode
import com.example.model.CapturedPhoto
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CameraBlack
import com.example.ui.theme.CameraBorder
import com.example.ui.theme.CameraDarkSurface
import com.example.ui.theme.ShutterRed
import com.example.ui.theme.ShutterRedDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BottomShutterBar(
    language: AppLanguage,
    shootingMode: CameraShootingMode,
    onShootingModeChanged: (CameraShootingMode) -> Unit,
    isRecordingVideo: Boolean,
    lastCapturedPhoto: CapturedPhoto?,
    isCapturing: Boolean,
    timerCountdown: Int?,
    onShutterClick: () -> Unit,
    onQuickSnapPhoto: () -> Unit,
    onThumbnailClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CameraBlack)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Mode Selector Slider (PHOTO, VIDEO, CINEMA, DUAL)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (mode in CameraShootingMode.entries) {
                val isSelected = shootingMode == mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) AmberGold.copy(alpha = 0.18f) else Color.Transparent)
                        .clickable(enabled = !isRecordingVideo) { onShootingModeChanged(mode) }
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                        .testTag("mode_${mode.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AppStrings.shootingMode(language, mode),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                        color = if (isSelected) AmberGold else TextMuted,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // 2. Shutter and Quick Action Controls Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(95.dp)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Thumbnail of last captured photo
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(CameraDarkSurface)
                    .border(2.dp, if (lastCapturedPhoto != null) AmberGold else CameraBorder, CircleShape)
                    .clickable(enabled = lastCapturedPhoto != null, onClick = onThumbnailClick)
                    .testTag("gallery_thumbnail_button"),
                contentAlignment = Alignment.Center
            ) {
                if (lastCapturedPhoto != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = lastCapturedPhoto.uri),
                        contentDescription = "Last captured media",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(54.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = "No photos yet",
                        tint = TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Center: Dynamic Shutter / Record Button
            val isVideoOrCinema = shootingMode == CameraShootingMode.VIDEO || shootingMode == CameraShootingMode.CINEMATIC
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.90f else 1.0f,
                animationSpec = spring(dampingRatio = 0.4f),
                label = "shutter_scale"
            )

            val recTransition = rememberInfiniteTransition(label = "pulse")
            val recGlow by recTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "rec_glow"
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Quick Photo Snap Button when in Video/Cinema mode ("همشي تحولي الي صور")
                if (isVideoOrCinema || shootingMode == CameraShootingMode.DUAL_PIP) {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CameraDarkSurface)
                            .border(1.5.dp, AmberGold, CircleShape)
                            .clickable(onClick = onQuickSnapPhoto)
                            .testTag("quick_snap_photo_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Take Photo",
                            tint = AmberGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(if (isRecordingVideo) recGlow else scale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = if (isVideoOrCinema || isRecordingVideo) {
                                    listOf(Color(0xFF4A1010), Color(0xFF260808), Color(0xFF140404))
                                } else {
                                    listOf(Color(0xFF3A3F4B), Color(0xFF1E2129), Color(0xFF121419))
                                }
                            )
                        )
                        .border(
                            2.5.dp,
                            if (isRecordingVideo) ShutterRed else if (isVideoOrCinema) Color(0xFFB71C1C) else Color(0xFF6C7383),
                            CircleShape
                        )
                        .padding(6.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            enabled = !isCapturing,
                            onClick = onShutterClick
                        )
                        .testTag("camera_shutter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    // Inner Core Button
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(if (isRecordingVideo) RoundedCornerShape(12.dp) else CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = if (isVideoOrCinema || isRecordingVideo) {
                                        listOf(ShutterRed, ShutterRedDark)
                                    } else {
                                        listOf(Color(0xFFFFFFFF), Color(0xFFECEFF1), Color(0xFFB0BEC5))
                                    }
                                )
                            )
                            .border(1.5.dp, if (isVideoOrCinema) Color.White else ShutterRed, if (isRecordingVideo) RoundedCornerShape(12.dp) else CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRecordingVideo) {
                            // Stop Recording Square
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White)
                            )
                        } else if (timerCountdown != null && timerCountdown > 0) {
                            Text(
                                text = "$timerCountdown",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = ShutterRedDark,
                                fontFamily = FontFamily.Monospace
                            )
                        } else if (isCapturing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = if (isVideoOrCinema) Color.White else ShutterRed,
                                strokeWidth = 3.dp
                            )
                        } else if (!isVideoOrCinema) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(ShutterRed, ShutterRedDark)
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            // Right: Quick Settings / Pro Info Trigger
            IconButton(
                onClick = onOpenSettingsClick,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(CameraDarkSurface)
                    .border(1.5.dp, CameraBorder, CircleShape)
                    .testTag("pro_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Camera Settings",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

