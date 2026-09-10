package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.model.AppStrings
import com.example.model.PipPosition
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CameraBlack
import com.example.ui.theme.ShutterRed
import com.example.ui.theme.TextPrimary

@Composable
fun BoxScope.DualPipOverlay(
    language: AppLanguage,
    isPipActive: Boolean,
    isPipSwapped: Boolean,
    pipPosition: PipPosition,
    onSwapPip: () -> Unit,
    onCyclePosition: () -> Unit,
    isRecording: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (!isPipActive) return

    val pipAlignment = when (pipPosition) {
        PipPosition.TOP_START -> Alignment.TopStart
        PipPosition.TOP_END -> Alignment.TopEnd
        PipPosition.BOTTOM_START -> Alignment.BottomStart
        PipPosition.BOTTOM_END -> Alignment.BottomEnd
    }

    Box(
        modifier = modifier
            .align(pipAlignment)
            .padding(12.dp)
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp))
            .size(width = 135.dp, height = 180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = if (isPipSwapped) {
                        listOf(Color(0xFF1B232E), Color(0xFF0F141B))
                    } else {
                        listOf(Color(0xFF2E1C18), Color(0xFF160E0C))
                    }
                )
            )
            .border(2.dp, AmberGold, RoundedCornerShape(16.dp))
            .testTag("dual_pip_window")
    ) {
        // Simulated Secondary Lens Stream Frame
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Simulated sensor grid & face placeholder
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, AmberGold.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPipSwapped) Icons.Default.PhotoCamera else Icons.Default.Person,
                        contentDescription = null,
                        tint = AmberGold,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isPipSwapped) "REAR 1.0X" else "FRONT 1080P",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberGold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Top Status Badge (PIP Lens Info + REC dot if active)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(CameraBlack.copy(alpha = 0.75f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isRecording) {
                Icon(
                    imageVector = Icons.Default.FiberManualRecord,
                    contentDescription = null,
                    tint = ShutterRed,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text(
                text = if (isPipSwapped) "REAR" else "SELFIE",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )
        }

        // Bottom Action Controls (Swap Cameras / Move Position)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxSize()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Swap PIP with Main camera
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(CameraBlack.copy(alpha = 0.85f))
                    .border(1.dp, AmberGold.copy(alpha = 0.7f), CircleShape)
                    .clickable(onClick = onSwapPip)
                    .testTag("swap_pip_cameras_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Swap Main/Pip",
                    tint = AmberGold,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Reposition PIP window to next corner
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(CameraBlack.copy(alpha = 0.85f))
                    .border(1.dp, AmberGold.copy(alpha = 0.7f), CircleShape)
                    .clickable(onClick = onCyclePosition)
                    .testTag("cycle_pip_position_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.OpenWith,
                    contentDescription = "Move Window",
                    tint = AmberGold,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
