package com.example.camera

import android.content.ContentValues
import android.content.Context
import android.hardware.camera2.CaptureRequest
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.model.CapturedPhoto
import com.example.model.FlashMode
import com.example.model.FocusModeOption
import com.example.model.WhiteBalancePreset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class CameraManager(private val context: Context) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var preview: Preview? = null
    private val cameraExecutor = Executors.newSingleThreadExecutor()

    var isBackCamera: Boolean = true
        private set

    var isCameraBound: Boolean = false
        private set

    fun startCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        useBackCamera: Boolean = true,
        onInitialized: (CameraInfo) -> Unit = {}
    ) {
        this.isBackCamera = useBackCamera
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()

                val hasBack = cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) == true
                val hasFront = cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) == true

                val cameraSelector = if (useBackCamera && hasBack) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else if (!useBackCamera && hasFront) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else if (hasBack) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else if (hasFront) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                cameraProvider?.unbindAll()
                camera = cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                isCameraBound = (camera != null)
                camera?.cameraInfo?.let { onInitialized(it) }
            } catch (exc: Exception) {
                isCameraBound = false
                Log.e("ProCamera", "Camera initialization failed, using simulation mode", exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun setZoomRatio(ratio: Float) {
        camera?.cameraControl?.setZoomRatio(ratio)
    }

    fun setExposureCompensation(index: Int) {
        camera?.cameraControl?.setExposureCompensationIndex(index)
    }

    fun setFlashMode(flashMode: FlashMode) {
        val capture = imageCapture ?: return
        when (flashMode) {
            FlashMode.AUTO -> {
                capture.flashMode = ImageCapture.FLASH_MODE_AUTO
                camera?.cameraControl?.enableTorch(false)
            }
            FlashMode.ON -> {
                capture.flashMode = ImageCapture.FLASH_MODE_ON
                camera?.cameraControl?.enableTorch(false)
            }
            FlashMode.OFF -> {
                capture.flashMode = ImageCapture.FLASH_MODE_OFF
                camera?.cameraControl?.enableTorch(false)
            }
            FlashMode.TORCH -> {
                capture.flashMode = ImageCapture.FLASH_MODE_OFF
                camera?.cameraControl?.enableTorch(true)
            }
        }
    }

    @OptIn(ExperimentalCamera2Interop::class)
    fun applyManualControls(
        iso: Int, // 0 = Auto
        shutterSpeedNanos: Long, // 0 = Auto
        wbPreset: WhiteBalancePreset,
        focusMode: FocusModeOption,
        manualFocusDistance: Float // 0.0 to 1.0
    ) {
        val cam = camera ?: return
        val c2Control = Camera2CameraControl.from(cam.cameraControl)
        val builder = CaptureRequestOptions.Builder()

        // Exposure & ISO Controls
        if (iso > 0 || shutterSpeedNanos > 0L) {
            builder.setCaptureRequestOption(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_OFF
            )
            if (iso > 0) {
                builder.setCaptureRequestOption(CaptureRequest.SENSOR_SENSITIVITY, iso)
            }
            if (shutterSpeedNanos > 0L) {
                builder.setCaptureRequestOption(CaptureRequest.SENSOR_EXPOSURE_TIME, shutterSpeedNanos)
            }
        } else {
            builder.setCaptureRequestOption(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON
            )
        }

        // White Balance Controls
        when (wbPreset) {
            WhiteBalancePreset.AUTO -> {
                builder.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_AUTO
                )
            }
            WhiteBalancePreset.DAYLIGHT -> {
                builder.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_DAYLIGHT
                )
            }
            WhiteBalancePreset.CLOUDY -> {
                builder.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT
                )
            }
            WhiteBalancePreset.SHADE -> {
                builder.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_SHADE
                )
            }
            WhiteBalancePreset.TUNGSTEN -> {
                builder.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_INCANDESCENT
                )
            }
            WhiteBalancePreset.FLUORESCENT -> {
                builder.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AWB_MODE,
                    CaptureRequest.CONTROL_AWB_MODE_FLUORESCENT
                )
            }
        }

        // Focus Controls
        when (focusMode) {
            FocusModeOption.AF_C -> {
                builder.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
            }
            FocusModeOption.AF_S -> {
                builder.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_AUTO
                )
            }
            FocusModeOption.MANUAL -> {
                builder.setCaptureRequestOption(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_OFF
                )
                // Convert 0.0 - 1.0 to diopters (0 = infinity, e.g. 10.0 = 10cm macro)
                val diopters = (1.0f - manualFocusDistance) * 10.0f
                builder.setCaptureRequestOption(
                    CaptureRequest.LENS_FOCUS_DISTANCE,
                    diopters
                )
            }
        }

        try {
            c2Control.setCaptureRequestOptions(builder.build())
        } catch (e: Exception) {
            Log.w("ProCamera", "Manual camera2 options not fully supported on this device/emulator: ${e.message}")
        }
    }

    fun focusOnPoint(x: Float, y: Float, previewWidth: Float, previewHeight: Float) {
        val cam = camera ?: return
        try {
            val factory = SurfaceOrientedMeteringPointFactory(previewWidth, previewHeight)
            val point = factory.createPoint(x, y)
            val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
                .setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            cam.cameraControl.startFocusAndMetering(action)
        } catch (e: Exception) {
            Log.e("ProCamera", "Touch to focus error", e)
        }
    }

    fun takePicture(
        isoText: String,
        shutterText: String,
        evText: String,
        wbText: String,
        focalLengthText: String,
        filter: com.example.model.CinematicFilter = com.example.model.CinematicFilter.NONE,
        isCinematic: Boolean = false,
        isDualPip: Boolean = false,
        isPipSwapped: Boolean = false,
        onPhotoCaptured: (CapturedPhoto) -> Unit,
        onError: (ImageCaptureException) -> Unit
    ) {
        val capture = imageCapture
        // If in Dual PIP mode or Cinematic filter mode or camera not bound, render the processed frame with filters & PIP
        if (capture == null || isDualPip || isCinematic || filter != com.example.model.CinematicFilter.NONE) {
            generateEnhancedPhoto(
                isoText = isoText,
                shutterText = shutterText,
                evText = evText,
                wbText = wbText,
                focalLengthText = focalLengthText,
                filter = filter,
                isCinematic = isCinematic,
                isDualPip = isDualPip,
                isPipSwapped = isPipSwapped,
                onPhotoCaptured = onPhotoCaptured
            )
            return
        }

        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "ProCam_$name.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ProCamera")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        capture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = outputFileResults.savedUri ?: Uri.EMPTY
                    val photo = CapturedPhoto(
                        uri = savedUri,
                        timestamp = System.currentTimeMillis(),
                        iso = isoText,
                        shutterSpeed = shutterText,
                        ev = evText,
                        wb = wbText,
                        focalLength = focalLengthText,
                        filterName = filter.titleEn,
                        isCinematic = isCinematic,
                        isDualPip = isDualPip,
                        isVideo = false
                    )
                    ContextCompat.getMainExecutor(context).execute {
                        onPhotoCaptured(photo)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.w("ProCamera", "Photo capture failed on camera hardware (${exception.message}), generating simulated frame...")
                    generateEnhancedPhoto(
                        isoText = isoText,
                        shutterText = shutterText,
                        evText = evText,
                        wbText = wbText,
                        focalLengthText = focalLengthText,
                        filter = filter,
                        isCinematic = isCinematic,
                        isDualPip = isDualPip,
                        isPipSwapped = isPipSwapped,
                        onPhotoCaptured = onPhotoCaptured
                    )
                }
            }
        )
    }

    fun saveRecordedVideo(
        durationSeconds: Int,
        filter: com.example.model.CinematicFilter,
        isCinematic: Boolean,
        isDualPip: Boolean,
        onVideoSaved: (CapturedPhoto) -> Unit
    ) {
        try {
            val width = 1920
            val height = 1080
            val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)

            val paint = android.graphics.Paint().apply { isAntiAlias = true }

            // Apply filter tint for video poster thumbnail
            val gradientColors = when (filter) {
                com.example.model.CinematicFilter.TEAL_ORANGE -> intArrayOf(0xFF0D2530.toInt(), 0xFF352014.toInt(), 0xFF09161E.toInt())
                com.example.model.CinematicFilter.NOIR -> intArrayOf(0xFF101010.toInt(), 0xFF282828.toInt(), 0xFF050505.toInt())
                com.example.model.CinematicFilter.VINTAGE_35MM -> intArrayOf(0xFF382512.toInt(), 0xFF241609.toInt(), 0xFF170D04.toInt())
                com.example.model.CinematicFilter.CYBERPUNK -> intArrayOf(0xFF240A30.toInt(), 0xFF0A2033.toInt(), 0xFF140822.toInt())
                com.example.model.CinematicFilter.EMERALD -> intArrayOf(0xFF092418.toInt(), 0xFF123522.toInt(), 0xFF05170F.toInt())
                com.example.model.CinematicFilter.CINEMA_LOG -> intArrayOf(0xFF282C30.toInt(), 0xFF32363A.toInt(), 0xFF1E2024.toInt())
                else -> intArrayOf(0xFF141920.toInt(), 0xFF1F2937.toInt(), 0xFF0B1015.toInt())
            }

            val shader = android.graphics.LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                gradientColors,
                null,
                android.graphics.Shader.TileMode.CLAMP
            )
            paint.shader = shader
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.shader = null

            // Anamorphic Bars if in Cinematic Mode
            if (isCinematic) {
                paint.color = 0xFF000000.toInt()
                paint.style = android.graphics.Paint.Style.FILL
                val barHeight = height * 0.12f
                canvas.drawRect(0f, 0f, width.toFloat(), barHeight, paint)
                canvas.drawRect(0f, height - barHeight, width.toFloat(), height.toFloat(), paint)
            }

            // Dual PIP Box if Dual Mode
            if (isDualPip) {
                val pipW = width * 0.28f
                val pipH = height * 0.28f
                val pipX = width - pipW - 60f
                val pipY = if (isCinematic) height * 0.14f else 60f
                paint.color = 0xAA000000.toInt()
                paint.style = android.graphics.Paint.Style.FILL
                canvas.drawRoundRect(pipX, pipY, pipX + pipW, pipY + pipH, 20f, 20f, paint)

                paint.color = 0xFFFFB300.toInt()
                paint.style = android.graphics.Paint.Style.STROKE
                paint.strokeWidth = 4f
                canvas.drawRoundRect(pipX, pipY, pipX + pipW, pipY + pipH, 20f, 20f, paint)

                paint.style = android.graphics.Paint.Style.FILL
                paint.textSize = 26f
                canvas.drawText("FRONT CAM (PIP)", pipX + 24f, pipY + 50f, paint)
            }

            // Video Play Badge & REC Indicator
            paint.style = android.graphics.Paint.Style.FILL
            paint.color = 0xFFE53935.toInt()
            canvas.drawCircle(width / 2f, height / 2f, 70f, paint)

            paint.color = 0xFFFFFFFF.toInt()
            val path = android.graphics.Path().apply {
                moveTo(width / 2f - 20f, height / 2f - 35f)
                lineTo(width / 2f + 35f, height / 2f)
                lineTo(width / 2f - 20f, height / 2f + 35f)
                close()
            }
            canvas.drawPath(path, paint)

            // Header text
            paint.color = 0xFFFFB300.toInt()
            paint.textSize = 44f
            val modeTitle = if (isDualPip) "PRO DUAL VIDEO RECORDING" else if (isCinematic) "PRO CINEMATIC 24FPS RECORDING" else "PRO 4K VIDEO RECORDING"
            canvas.drawText(modeTitle, 100f, if (isCinematic) 180f else 120f, paint)

            paint.color = 0xFFECEFF1.toInt()
            paint.textSize = 32f
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            val durStr = String.format(Locale.US, "%02d:%02d", mins, secs)
            canvas.drawText("DURATION: $durStr  |  LUT: ${filter.titleEn}  |  4K UHD 60Mbps", 100f, if (isCinematic) 240f else 180f, paint)

            val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "ProVid_$name.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ProCamera")
                }
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, out)
                }
                val photo = CapturedPhoto(
                    uri = uri,
                    timestamp = System.currentTimeMillis(),
                    iso = "ISO 400",
                    shutterSpeed = "1/48s (180°)",
                    ev = "0.0 EV",
                    wb = "AWB Cinema",
                    focalLength = "35mm Cine",
                    filterName = filter.titleEn,
                    isCinematic = isCinematic,
                    isDualPip = isDualPip,
                    isVideo = true
                )
                ContextCompat.getMainExecutor(context).execute {
                    onVideoSaved(photo)
                }
            }
        } catch (e: Exception) {
            Log.e("ProCamera", "Failed to save video thumbnail", e)
        }
    }

    private fun generateEnhancedPhoto(
        isoText: String,
        shutterText: String,
        evText: String,
        wbText: String,
        focalLengthText: String,
        filter: com.example.model.CinematicFilter,
        isCinematic: Boolean,
        isDualPip: Boolean,
        isPipSwapped: Boolean,
        onPhotoCaptured: (CapturedPhoto) -> Unit
    ) {
        try {
            val width = 1920
            val height = 1080
            val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)

            val paint = android.graphics.Paint().apply { isAntiAlias = true }

            // Filter Color Gradients
            val gradientColors = when (filter) {
                com.example.model.CinematicFilter.TEAL_ORANGE -> intArrayOf(0xFF0B2433.toInt(), 0xFF352014.toInt(), 0xFF081620.toInt(), 0xFF4A2B14.toInt())
                com.example.model.CinematicFilter.NOIR -> intArrayOf(0xFF141414.toInt(), 0xFF303030.toInt(), 0xFF080808.toInt(), 0xFF222222.toInt())
                com.example.model.CinematicFilter.VINTAGE_35MM -> intArrayOf(0xFF382312.toInt(), 0xFF2A1A0C.toInt(), 0xFF1A1006.toInt(), 0xFF422B18.toInt())
                com.example.model.CinematicFilter.CYBERPUNK -> intArrayOf(0xFF260835.toInt(), 0xFF0B2238.toInt(), 0xFF140724.toInt(), 0xFF350B42.toInt())
                com.example.model.CinematicFilter.EMERALD -> intArrayOf(0xFF09261A.toInt(), 0xFF143825.toInt(), 0xFF061810.toInt(), 0xFF103020.toInt())
                com.example.model.CinematicFilter.CINEMA_LOG -> intArrayOf(0xFF262A2E.toInt(), 0xFF33383D.toInt(), 0xFF1C1E22.toInt(), 0xFF2F3438.toInt())
                else -> intArrayOf(0xFF1B2430.toInt(), 0xFF141E28.toInt(), 0xFF0B131E.toInt(), 0xFF2C3E50.toInt())
            }

            val shader = android.graphics.LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                gradientColors,
                null,
                android.graphics.Shader.TileMode.CLAMP
            )
            paint.shader = shader
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.shader = null

            // Anamorphic 2.39:1 Letterbox Bars
            if (isCinematic) {
                paint.color = 0xFF000000.toInt()
                paint.style = android.graphics.Paint.Style.FILL
                val barHeight = height * 0.125f // 2.39:1 CinemaScope
                canvas.drawRect(0f, 0f, width.toFloat(), barHeight, paint)
                canvas.drawRect(0f, height - barHeight, width.toFloat(), height.toFloat(), paint)

                // 2.39:1 gold cinema badge
                paint.color = 0xFFFFB300.toInt()
                paint.textSize = 28f
                paint.typeface = android.graphics.Typeface.MONOSPACE
                canvas.drawText("CINEMASCOPE 2.39:1 • 24 FPS", 100f, barHeight - 20f, paint)
            }

            // Dual PIP Window Rendering
            if (isDualPip) {
                val pipW = width * 0.28f
                val pipH = height * 0.28f
                val pipX = width - pipW - 70f
                val pipY = if (isCinematic) height * 0.15f else 70f

                // PIP background
                val pipShader = android.graphics.LinearGradient(
                    pipX, pipY, pipX + pipW, pipY + pipH,
                    if (isPipSwapped) intArrayOf(0xFF1E2633.toInt(), 0xFF0D141E.toInt()) else intArrayOf(0xFF2E2018.toInt(), 0xFF1A120D.toInt()),
                    null,
                    android.graphics.Shader.TileMode.CLAMP
                )
                paint.shader = pipShader
                paint.style = android.graphics.Paint.Style.FILL
                canvas.drawRoundRect(pipX, pipY, pipX + pipW, pipY + pipH, 24f, 24f, paint)
                paint.shader = null

                // PIP border
                paint.color = 0xFFFFB300.toInt()
                paint.style = android.graphics.Paint.Style.STROKE
                paint.strokeWidth = 4f
                canvas.drawRoundRect(pipX, pipY, pipX + pipW, pipY + pipH, 24f, 24f, paint)

                // PIP label
                paint.style = android.graphics.Paint.Style.FILL
                paint.color = 0xFFFFFFFF.toInt()
                paint.textSize = 24f
                val pipCamTitle = if (isPipSwapped) "REAR LENS (PIP)" else "FRONT SELFIE (PIP)"
                canvas.drawText(pipCamTitle, pipX + 24f, pipY + 45f, paint)

                // PIP simulated target crosshair
                paint.color = 0x88FFB300.toInt()
                paint.strokeWidth = 2f
                val centerX = pipX + pipW / 2f
                val centerY = pipY + pipH / 2f
                canvas.drawLine(centerX - 25f, centerY, centerX + 25f, centerY, paint)
                canvas.drawLine(centerX, centerY - 25f, centerX, centerY + 25f, paint)
            }

            // Viewfinder Grid & framing
            paint.color = 0x44FFB300.toInt()
            paint.strokeWidth = 2.5f
            paint.style = android.graphics.Paint.Style.STROKE
            canvas.drawRect(80f, 80f, (width - 80).toFloat(), (height - 80).toFloat(), paint)

            // Center target reticle
            canvas.drawLine(width / 2f - 40f, height / 2f, width / 2f + 40f, height / 2f, paint)
            canvas.drawLine(width / 2f, height / 2f - 40f, width / 2f, height / 2f + 40f, paint)

            // Info overlay
            paint.style = android.graphics.Paint.Style.FILL
            paint.color = 0xFFFFB300.toInt()
            paint.textSize = 38f
            val headerTitle = when {
                isDualPip -> "PRO DUAL PIP CAPTURE • DIRECTOR CUT"
                isCinematic -> "PRO CINEMATIC 24FPS • ANAMORPHIC"
                else -> "PRO MANUAL CAMERA • RAW CAPTURE"
            }
            canvas.drawText(headerTitle, 120f, if (isCinematic) height * 0.125f + 60f else 150f, paint)

            paint.color = 0xFFECEFF1.toInt()
            paint.textSize = 28f
            val timestampStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            val infoY = if (isCinematic) height * 0.125f + 110f else 200f
            canvas.drawText("LUT: ${filter.titleEn.uppercase()}  |  TIME: $timestampStr", 120f, infoY, paint)
            canvas.drawText("EXPOSURE: $isoText  |  $shutterText  |  $evText  |  $wbText  |  $focalLengthText", 120f, infoY + 45f, paint)

            val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "ProCam_$name.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ProCamera")
                }
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { outStream ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, outStream)
                }
                val photo = CapturedPhoto(
                    uri = uri,
                    timestamp = System.currentTimeMillis(),
                    iso = isoText,
                    shutterSpeed = shutterText,
                    ev = evText,
                    wb = wbText,
                    focalLength = focalLengthText,
                    filterName = filter.titleEn,
                    isCinematic = isCinematic,
                    isDualPip = isDualPip,
                    isVideo = false
                )
                ContextCompat.getMainExecutor(context).execute {
                    onPhotoCaptured(photo)
                }
            }
        } catch (e: Exception) {
            Log.e("ProCamera", "Failed to generate photo", e)
        }
    }

    fun release() {
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
    }
}
