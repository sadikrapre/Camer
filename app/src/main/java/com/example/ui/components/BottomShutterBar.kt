package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
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
import com.example.model.CapturedPhoto
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CameraBlack
import com.example.ui.theme.CameraBorder
import com.example.ui.theme.CameraDarkSurface
import com.example.ui.theme.ShutterRed
import com.example.ui.theme.ShutterRedDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BottomShutterBar(
    lastCapturedPhoto: CapturedPhoto?,
    isCapturing: Boolean,
    timerCountdown: Int?,
    onShutterClick: () -> Unit,
    onThumbnailClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CameraBlack)
            .navigationBarsPadding()
            .height(100.dp)
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
                    contentDescription = "Last captured photo thumbnail",
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

        // Center: Animated DSLR Shutter Button
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.90f else 1.0f,
            animationSpec = spring(dampingRatio = 0.4f),
            label = "shutter_scale"
        )

        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF3A3F4B), Color(0xFF1E2129), Color(0xFF121419))
                    )
                )
                .border(2.5.dp, Color(0xFF6C7383), CircleShape)
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
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color(0xFFFFFFFF), Color(0xFFECEFF1), Color(0xFFB0BEC5))
                        )
                    )
                    .border(1.5.dp, ShutterRed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Red accent center dot or countdown timer
                if (timerCountdown != null && timerCountdown > 0) {
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
                        color = ShutterRed,
                        strokeWidth = 3.dp
                    )
                } else {
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
