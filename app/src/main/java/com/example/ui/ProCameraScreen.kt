package com.example.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.camera.CameraManager
import com.example.model.AppLanguage
import com.example.model.CameraShootingMode
import com.example.model.CameraUiState
import com.example.model.CapturedPhoto
import com.example.model.CinematicFilter
import com.example.model.FlashMode
import com.example.model.FocusModeOption
import com.example.model.GridType
import com.example.model.LensOption
import com.example.model.ManualControlMode
import com.example.model.PipPosition
import com.example.model.TimerOption
import com.example.model.WhiteBalancePreset
import com.example.sensor.rememberSpiritLevelState
import com.example.ui.components.BottomShutterBar
import com.example.ui.components.CinematicFilterSelector
import com.example.ui.components.DualPipOverlay
import com.example.ui.components.ManualControlsBar
import com.example.ui.components.PhotoViewerDialog
import com.example.ui.components.QuickSettingsDialog
import com.example.ui.components.SHUTTER_SPEEDS
import com.example.ui.components.TopBarControls
import com.example.ui.components.ViewfinderOverlay
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CameraBlack
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ProCameraScreen(
    initialLanguage: AppLanguage = AppLanguage.ARABIC,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val cameraManager = remember { CameraManager(context) }
    val levelState = rememberSpiritLevelState()

    var uiState by remember { mutableStateOf(CameraUiState(language = initialLanguage)) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var viewingPhoto by remember { mutableStateOf<CapturedPhoto?>(null) }
    var hapticsEnabled by remember { mutableStateOf(true) }

    // Flash animation on photo capture
    val flashAnim = remember { Animatable(0f) }

    // Video recording timer loop
    LaunchedEffect(uiState.isRecordingVideo) {
        if (uiState.isRecordingVideo) {
            while (isActive && uiState.isRecordingVideo) {
                delay(1000)
                uiState = uiState.copy(videoDurationSeconds = uiState.videoDurationSeconds + 1)
            }
        }
    }

    // Haptic feedback helper
    val triggerHaptic = {
        if (hapticsEnabled) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(30)
                }
            } catch (_: Exception) {}
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraManager.release()
        }
    }

    // Apply manual controls whenever state changes
    LaunchedEffect(
        uiState.selectedIso,
        uiState.selectedShutterSpeedIndex,
        uiState.selectedWbPreset,
        uiState.focusMode,
        uiState.manualFocusDistance
    ) {
        val shutterNanos = SHUTTER_SPEEDS.getOrNull(uiState.selectedShutterSpeedIndex)?.second ?: 0L
        cameraManager.applyManualControls(
            iso = uiState.selectedIso,
            shutterSpeedNanos = shutterNanos,
            wbPreset = uiState.selectedWbPreset,
            focusMode = uiState.focusMode,
            manualFocusDistance = uiState.manualFocusDistance
        )
    }

    // Apply flash mode changes
    LaunchedEffect(uiState.flashMode) {
        cameraManager.setFlashMode(uiState.flashMode)
    }

    // Apply exposure compensation
    LaunchedEffect(uiState.evCompensationIndex) {
        cameraManager.setExposureCompensation(uiState.evCompensationIndex)
    }

    // Shutter capture execution
    val executePhotoCapture = {
        uiState = uiState.copy(isCapturing = true)
        triggerHaptic()

        // Flash screen
        coroutineScope.launch {
            flashAnim.snapTo(0.85f)
            flashAnim.animateTo(0f, animationSpec = tween(180))
        }

        val isoText = if (uiState.selectedIso == 0) "ISO AUTO" else "ISO ${uiState.selectedIso}"
        val shutterText = SHUTTER_SPEEDS.getOrNull(uiState.selectedShutterSpeedIndex)?.first ?: "AUTO"
        val evText = String.format(Locale.US, "%+.1f EV", uiState.evCompensationIndex * uiState.evStep)
        val wbText = uiState.selectedWbPreset.label
        val focalText = "${uiState.selectedLens.label} (${(26 * uiState.selectedLens.zoomRatio).toInt()}mm)"

        cameraManager.takePicture(
            isoText = isoText,
            shutterText = shutterText,
            evText = evText,
            wbText = wbText,
            focalLengthText = focalText,
            filter = uiState.selectedFilter,
            isCinematic = uiState.isCinematicAnamorphic || uiState.shootingMode == CameraShootingMode.CINEMATIC,
            isDualPip = uiState.isDualPipActive || uiState.shootingMode == CameraShootingMode.DUAL_PIP,
            isPipSwapped = uiState.isPipSwapped,
            onPhotoCaptured = { photo ->
                uiState = uiState.copy(
                    isCapturing = false,
                    lastCapturedPhoto = photo,
                    timerCountdownSeconds = null
                )
                triggerHaptic()
            },
            onError = {
                uiState = uiState.copy(
                    isCapturing = false,
                    timerCountdownSeconds = null
                )
            }
        )
    }

    // Toggle video recording
    val toggleVideoRecording = {
        if (uiState.isRecordingVideo) {
            val duration = uiState.videoDurationSeconds
            cameraManager.saveRecordedVideo(
                durationSeconds = duration,
                filter = uiState.selectedFilter,
                isCinematic = uiState.isCinematicAnamorphic || uiState.shootingMode == CameraShootingMode.CINEMATIC,
                isDualPip = uiState.isDualPipActive || uiState.shootingMode == CameraShootingMode.DUAL_PIP,
                onVideoSaved = { videoPhoto ->
                    uiState = uiState.copy(
                        isRecordingVideo = false,
                        videoDurationSeconds = 0,
                        lastCapturedPhoto = videoPhoto
                    )
                    triggerHaptic()
                }
            )
        } else {
            uiState = uiState.copy(isRecordingVideo = true, videoDurationSeconds = 0)
            triggerHaptic()
        }
    }

    val onShutterPressed = {
        val isVideoMode = uiState.shootingMode == CameraShootingMode.VIDEO || uiState.shootingMode == CameraShootingMode.CINEMATIC
        if (isVideoMode) {
            toggleVideoRecording()
        } else {
            if (uiState.timerOption != TimerOption.OFF) {
                coroutineScope.launch {
                    for (remaining in uiState.timerOption.seconds downTo 1) {
                        uiState = uiState.copy(timerCountdownSeconds = remaining)
                        triggerHaptic()
                        delay(1000)
                    }
                    uiState = uiState.copy(timerCountdownSeconds = null)
                    executePhotoCapture()
                }
            } else {
                executePhotoCapture()
            }
        }
    }

    val layoutDirection = if (uiState.language == AppLanguage.ARABIC) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(CameraBlack)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. Top Bar Controls (Bilingual Toggle, Flash, Lenses, Grids, Timer, Camera Flip, Filters, Cinematic, Dual PIP)
                TopBarControls(
                    language = uiState.language,
                    onLanguageToggle = {
                        val nextLang = if (uiState.language == AppLanguage.ARABIC) AppLanguage.ENGLISH else AppLanguage.ARABIC
                        uiState = uiState.copy(language = nextLang)
                        triggerHaptic()
                    },
                    flashMode = uiState.flashMode,
                    onFlashModeCycle = {
                        val nextIndex = (uiState.flashMode.ordinal + 1) % FlashMode.entries.size
                        uiState = uiState.copy(flashMode = FlashMode.entries[nextIndex])
                        triggerHaptic()
                    },
                    currentLens = uiState.selectedLens,
                    onLensSelected = { lens ->
                        uiState = uiState.copy(selectedLens = lens, currentZoomRatio = lens.zoomRatio)
                        cameraManager.setZoomRatio(lens.zoomRatio)
                        triggerHaptic()
                    },
                    gridType = uiState.gridType,
                    onGridTypeCycle = {
                        val nextIndex = (uiState.gridType.ordinal + 1) % GridType.entries.size
                        uiState = uiState.copy(gridType = GridType.entries[nextIndex])
                        triggerHaptic()
                    },
                    showSpiritLevel = uiState.showSpiritLevel,
                    onSpiritLevelToggle = {
                        uiState = uiState.copy(showSpiritLevel = !uiState.showSpiritLevel)
                        triggerHaptic()
                    },
                    showHistogram = uiState.showHistogram,
                    onHistogramToggle = {
                        uiState = uiState.copy(showHistogram = !uiState.showHistogram)
                        triggerHaptic()
                    },
                    timerOption = uiState.timerOption,
                    onTimerCycle = {
                        val nextIndex = (uiState.timerOption.ordinal + 1) % TimerOption.entries.size
                        uiState = uiState.copy(timerOption = TimerOption.entries[nextIndex])
                        triggerHaptic()
                    },
                    onFlipCamera = {
                        val newFacing = !uiState.isBackCamera
                        uiState = uiState.copy(isBackCamera = newFacing)
                        triggerHaptic()
                    },
                    selectedFilter = uiState.selectedFilter,
                    isFilterSelectorOpen = uiState.showFilterSelector,
                    onToggleFilterSelector = {
                        uiState = uiState.copy(showFilterSelector = !uiState.showFilterSelector)
                        triggerHaptic()
                    },
                    isCinematicActive = uiState.isCinematicAnamorphic || uiState.shootingMode == CameraShootingMode.CINEMATIC,
                    onToggleCinematic = {
                        val next = !uiState.isCinematicAnamorphic
                        uiState = uiState.copy(
                            isCinematicAnamorphic = next,
                            shootingMode = if (next) CameraShootingMode.CINEMATIC else CameraShootingMode.PHOTO
                        )
                        triggerHaptic()
                    },
                    isDualPipActive = uiState.isDualPipActive || uiState.shootingMode == CameraShootingMode.DUAL_PIP,
                    onToggleDualPip = {
                        val next = !uiState.isDualPipActive
                        uiState = uiState.copy(
                            isDualPipActive = next,
                            shootingMode = if (next) CameraShootingMode.DUAL_PIP else CameraShootingMode.PHOTO
                        )
                        triggerHaptic()
                    },
                    isRecording = uiState.isRecordingVideo,
                    recordingDurationSeconds = uiState.videoDurationSeconds,
                    modifier = Modifier.statusBarsPadding()
                )

                // Cinematic Filter Carousel Drawer
                CinematicFilterSelector(
                    language = uiState.language,
                    visible = uiState.showFilterSelector,
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = { filter ->
                        uiState = uiState.copy(selectedFilter = filter)
                        triggerHaptic()
                    },
                    onClose = {
                        uiState = uiState.copy(showFilterSelector = false)
                        triggerHaptic()
                    }
                )

                // 2. Viewfinder Window (Live Camera + Overlays)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Black)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                uiState = uiState.copy(
                                    focusPoint = offset.x to offset.y,
                                    focusLocked = true
                                )
                                cameraManager.focusOnPoint(offset.x, offset.y, size.width.toFloat(), size.height.toFloat())
                                triggerHaptic()
                            }
                        }
                        .pointerInput(Unit) {
                            detectTransformGestures { _, _, zoom, _ ->
                                val newZoom = (uiState.currentZoomRatio * zoom).coerceIn(uiState.minZoomRatio, uiState.maxZoomRatio)
                                uiState = uiState.copy(currentZoomRatio = newZoom)
                                cameraManager.setZoomRatio(newZoom)
                            }
                        }
                ) {
                    // Viewfinder Backdrop (Simulated photo view for emulators or prior to hardware bind)
                    val evBrightness = (1f + (uiState.evCompensationIndex * 0.12f)).coerceIn(0.4f, 1.8f)
                    val tintColor = when (uiState.selectedWbPreset) {
                        WhiteBalancePreset.CLOUDY, WhiteBalancePreset.SHADE -> Color(0xFF332211)
                        WhiteBalancePreset.FLUORESCENT -> Color(0xFF1B2B38)
                        WhiteBalancePreset.TUNGSTEN -> Color(0xFF3A240E)
                        else -> Color(0xFF14181E)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0D0F14),
                                        tintColor,
                                        Color(0xFF08090C)
                                    )
                                )
                            )
                    )

                    // Live Camera PreviewView
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                                cameraManager.startCamera(
                                    lifecycleOwner = lifecycleOwner,
                                    previewView = this,
                                    useBackCamera = if (uiState.isPipSwapped) !uiState.isBackCamera else uiState.isBackCamera
                                ) { cameraInfo ->
                                    val zoomState = cameraInfo.zoomState.value
                                    if (zoomState != null) {
                                        uiState = uiState.copy(
                                            minZoomRatio = zoomState.minZoomRatio,
                                            maxZoomRatio = zoomState.maxZoomRatio
                                        )
                                    }
                                }
                            }
                        },
                        update = { previewView ->
                            cameraManager.startCamera(
                                lifecycleOwner = lifecycleOwner,
                                previewView = previewView,
                                useBackCamera = if (uiState.isPipSwapped) !uiState.isBackCamera else uiState.isBackCamera
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Real-time Cinematic LUT Color Grading Tint Overlay
                    if (uiState.selectedFilter != CinematicFilter.NONE) {
                        val filterColor = when (uiState.selectedFilter) {
                            CinematicFilter.TEAL_ORANGE -> Color(0xFF00ADB5).copy(alpha = 0.14f)
                            CinematicFilter.NOIR -> Color(0xFF000000).copy(alpha = 0.38f)
                            CinematicFilter.VINTAGE_35MM -> Color(0xFFFFB300).copy(alpha = 0.16f)
                            CinematicFilter.CYBERPUNK -> Color(0xFFE040FB).copy(alpha = 0.15f)
                            CinematicFilter.EMERALD -> Color(0xFF00E676).copy(alpha = 0.12f)
                            CinematicFilter.CINEMA_LOG -> Color(0xFF90A4AE).copy(alpha = 0.18f)
                            else -> Color.Transparent
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(filterColor)
                        )
                    }

                    // Viewfinder Overlays (Grid, Spirit Level, EV ladder, Histogram, Focus Reticle)
                    ViewfinderOverlay(
                        language = uiState.language,
                        gridType = uiState.gridType,
                        showSpiritLevel = uiState.showSpiritLevel,
                        showHistogram = uiState.showHistogram,
                        levelState = levelState,
                        focusPoint = uiState.focusPoint,
                        focusLocked = uiState.focusLocked,
                        evIndex = uiState.evCompensationIndex,
                        evStep = uiState.evStep
                    )

                    // Cinematic 2.39:1 Anamorphic Letterbox Bars
                    val isCinematic = uiState.isCinematicAnamorphic || uiState.shootingMode == CameraShootingMode.CINEMATIC
                    if (isCinematic) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.12f)
                                    .background(Color.Black),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.padding(start = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "2.39:1 CINEMASCOPE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = AmberGold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "• 24 FPS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.weight(0.76f))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.12f)
                                    .background(Color.Black)
                            )
                        }
                    }

                    // Dual Camera (Front + Back) Floating PIP Window
                    val isDualActive = uiState.isDualPipActive || uiState.shootingMode == CameraShootingMode.DUAL_PIP
                    DualPipOverlay(
                        language = uiState.language,
                        isPipActive = isDualActive,
                        isPipSwapped = uiState.isPipSwapped,
                        pipPosition = uiState.pipPosition,
                        onSwapPip = {
                            uiState = uiState.copy(isPipSwapped = !uiState.isPipSwapped)
                            triggerHaptic()
                        },
                        onCyclePosition = {
                            val next = when (uiState.pipPosition) {
                                PipPosition.TOP_END -> PipPosition.BOTTOM_END
                                PipPosition.BOTTOM_END -> PipPosition.BOTTOM_START
                                PipPosition.BOTTOM_START -> PipPosition.TOP_START
                                PipPosition.TOP_START -> PipPosition.TOP_END
                            }
                            uiState = uiState.copy(pipPosition = next)
                            triggerHaptic()
                        },
                        isRecording = uiState.isRecordingVideo
                    )

                    // Capture Flash Effect Overlay
                    if (flashAnim.value > 0.01f) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = flashAnim.value))
                        )
                    }

                    // Countdown Timer Overlay
                    uiState.timerCountdownSeconds?.let { countdown ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$countdown",
                                fontSize = 84.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // 3. Interactive Manual Controls Bar (EV, ISO, SEC, WB, MF sliders)
                ManualControlsBar(
                    language = uiState.language,
                    activeMode = uiState.activeControlMode,
                    onModeSelected = { mode ->
                        uiState = uiState.copy(activeControlMode = mode)
                        triggerHaptic()
                    },
                    evIndex = uiState.evCompensationIndex,
                    evStep = uiState.evStep,
                    onEvChanged = { ev ->
                        uiState = uiState.copy(evCompensationIndex = ev)
                        triggerHaptic()
                    },
                    selectedIso = uiState.selectedIso,
                    onIsoChanged = { iso ->
                        uiState = uiState.copy(selectedIso = iso)
                        triggerHaptic()
                    },
                    selectedShutterIndex = uiState.selectedShutterSpeedIndex,
                    onShutterChanged = { index ->
                        uiState = uiState.copy(selectedShutterSpeedIndex = index)
                        triggerHaptic()
                    },
                    selectedWb = uiState.selectedWbPreset,
                    onWbChanged = { wb ->
                        uiState = uiState.copy(selectedWbPreset = wb)
                        triggerHaptic()
                    },
                    focusMode = uiState.focusMode,
                    onFocusModeChanged = { fm ->
                        uiState = uiState.copy(focusMode = fm)
                        triggerHaptic()
                    },
                    manualFocusDistance = uiState.manualFocusDistance,
                    onManualFocusDistanceChanged = { dist ->
                        uiState = uiState.copy(manualFocusDistance = dist)
                    }
                )

                // 4. Bottom Shutter Bar with Mode Selector & Quick Photo Snap
                BottomShutterBar(
                    language = uiState.language,
                    shootingMode = uiState.shootingMode,
                    onShootingModeChanged = { mode ->
                        val isDual = mode == CameraShootingMode.DUAL_PIP
                        val isCinema = mode == CameraShootingMode.CINEMATIC
                        uiState = uiState.copy(
                            shootingMode = mode,
                            isDualPipActive = isDual,
                            isCinematicAnamorphic = isCinema
                        )
                        triggerHaptic()
                    },
                    isRecordingVideo = uiState.isRecordingVideo,
                    lastCapturedPhoto = uiState.lastCapturedPhoto,
                    isCapturing = uiState.isCapturing,
                    timerCountdown = uiState.timerCountdownSeconds,
                    onShutterClick = { onShutterPressed() },
                    onQuickSnapPhoto = { executePhotoCapture() },
                    onThumbnailClick = {
                        viewingPhoto = uiState.lastCapturedPhoto
                    },
                    onOpenSettingsClick = {
                        showSettingsDialog = true
                        triggerHaptic()
                    },
                    onFlipCamera = {
                        val newFacing = !uiState.isBackCamera
                        uiState = uiState.copy(isBackCamera = newFacing)
                        triggerHaptic()
                    },
                    isBackCamera = uiState.isBackCamera
                )
            }

            // Settings Dialog Modal (Full Bilingual Support)
            if (showSettingsDialog) {
                QuickSettingsDialog(
                    language = uiState.language,
                    onLanguageSelected = { newLang ->
                        uiState = uiState.copy(language = newLang)
                        triggerHaptic()
                    },
                    gridType = uiState.gridType,
                    onGridTypeSelected = { uiState = uiState.copy(gridType = it) },
                    showSpiritLevel = uiState.showSpiritLevel,
                    onSpiritLevelToggle = { uiState = uiState.copy(showSpiritLevel = !uiState.showSpiritLevel) },
                    showHistogram = uiState.showHistogram,
                    onHistogramToggle = { uiState = uiState.copy(showHistogram = !uiState.showHistogram) },
                    timerOption = uiState.timerOption,
                    onTimerOptionSelected = { uiState = uiState.copy(timerOption = it) },
                    hapticsEnabled = hapticsEnabled,
                    onHapticsToggle = { hapticsEnabled = !hapticsEnabled },
                    onDismiss = { showSettingsDialog = false }
                )
            }

            // Full Screen Photo Preview Dialog (Full Bilingual Support)
            viewingPhoto?.let { photo ->
                PhotoViewerDialog(
                    photo = photo,
                    language = uiState.language,
                    onDismiss = { viewingPhoto = null }
                )
            }
        }
    }
}
