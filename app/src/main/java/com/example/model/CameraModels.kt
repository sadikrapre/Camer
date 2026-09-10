package com.example.model

import android.net.Uri

enum class ManualControlMode(val label: String, val title: String) {
    EV("EV", "Exposure Value"),
    ISO("ISO", "Sensitivity"),
    SEC("SEC", "Shutter Speed"),
    WB("WB", "White Balance"),
    FOCUS("MF", "Focus Distance")
}

enum class GridType(val displayName: String, val shortCode: String) {
    NONE("Off", "OFF"),
    THIRDS("Rule of Thirds", "3×3"),
    GOLDEN_RATIO("Golden Ratio", "Φ"),
    SQUARE("Square Frame", "1:1")
}

enum class FlashMode(val displayName: String) {
    OFF("Off"),
    AUTO("Auto"),
    ON("On"),
    TORCH("Torch")
}

enum class LensOption(val label: String, val zoomRatio: Float) {
    ULTRA_WIDE("0.5×", 0.5f),
    WIDE("1.0×", 1.0f),
    TELE_2X("2.0×", 2.0f),
    TELE_5X("5.0×", 5.0f)
}

enum class WhiteBalancePreset(val label: String, val kelvinText: String) {
    AUTO("AWB", "Auto"),
    DAYLIGHT("Daylight", "5500K"),
    CLOUDY("Cloudy", "6500K"),
    SHADE("Shade", "7500K"),
    TUNGSTEN("Tungsten", "3200K"),
    FLUORESCENT("Fluorescent", "4000K")
}

enum class FocusModeOption(val label: String) {
    AF_C("AF-C"),
    AF_S("AF-S"),
    MANUAL("MF")
}

enum class TimerOption(val seconds: Int, val label: String) {
    OFF(0, "Off"),
    THREE(3, "3s"),
    TEN(10, "10s")
}

enum class CameraShootingMode(val labelEn: String, val labelAr: String) {
    PHOTO("PHOTO", "صورة"),
    VIDEO("VIDEO", "فيديو"),
    CINEMATIC("CINEMA", "سينمائي"),
    DUAL_PIP("DUAL PIP", "كاميرا مزدوجة")
}

enum class CinematicFilter(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val subtitleEn: String,
    val subtitleAr: String,
    val primaryColorHex: Long
) {
    NONE("none", "Natural", "طبيعي", "Standard Clean Look", "ألوان محايدة نقية", 0xFFE0E0E0),
    TEAL_ORANGE("teal_orange", "Teal & Orange", "تيل وبرتقالي", "Hollywood Blockbuster", "التباين السينمائي الهوليودي", 0xFF00ADB5),
    NOIR("noir", "Classic Noir", "نوير كلاسيكي", "High Contrast Monochrome", "أبيض وأسود فائق التباين", 0xFFEEEEEE),
    VINTAGE_35MM("vintage", "35mm Film", "فيلم 35 ملم", "Warm Analog Kodak Glow", "دفء شريط السينما التناظري", 0xFFFFB300),
    CYBERPUNK("cyberpunk", "Cyberpunk", "سايبربانك", "Neon Magenta & Cyan", "ألوان نيون مستقبلية حادة", 0xFFE040FB),
    EMERALD("emerald", "Emerald", "زمرد سينمائي", "Moody Nordic Forest", "درجات خضراء باردة درامية", 0xFF00E676),
    CINEMA_LOG("flat_log", "Cinema Flat LOG", "LOG سينمائي", "Maximum Dynamic Range", "نطاق ديناميكي مسطح للمونتاج", 0xFF90A4AE)
}

enum class PipPosition {
    TOP_START,
    TOP_END,
    BOTTOM_START,
    BOTTOM_END
}

data class CapturedPhoto(
    val uri: Uri,
    val timestamp: Long = System.currentTimeMillis(),
    val iso: String = "ISO 100",
    val shutterSpeed: String = "1/125s",
    val ev: String = "0.0 EV",
    val wb: String = "AWB",
    val focalLength: String = "26mm (1.0×)",
    val filterName: String = "Natural",
    val isCinematic: Boolean = false,
    val isDualPip: Boolean = false,
    val isVideo: Boolean = false
)

data class CameraUiState(
    val isInitialized: Boolean = false,
    val isBackCamera: Boolean = true,
    val shootingMode: CameraShootingMode = CameraShootingMode.PHOTO,
    val flashMode: FlashMode = FlashMode.AUTO,
    val selectedLens: LensOption = LensOption.WIDE,
    val currentZoomRatio: Float = 1.0f,
    val minZoomRatio: Float = 0.5f,
    val maxZoomRatio: Float = 8.0f,
    val gridType: GridType = GridType.THIRDS,
    val showSpiritLevel: Boolean = true,
    val showHistogram: Boolean = false,
    val timerOption: TimerOption = TimerOption.OFF,
    val timerCountdownSeconds: Int? = null,
    val activeControlMode: ManualControlMode = ManualControlMode.EV,
    val evCompensationIndex: Int = 0, // -6 to +6 (for -2.0 to +2.0 EV with 0.33 step)
    val evStep: Float = 0.33333334f,
    val selectedIso: Int = 0, // 0 = Auto
    val selectedShutterSpeedIndex: Int = 0, // 0 = Auto
    val selectedWbPreset: WhiteBalancePreset = WhiteBalancePreset.AUTO,
    val focusMode: FocusModeOption = FocusModeOption.AF_C,
    val manualFocusDistance: Float = 0.5f, // 0.0 (macro) to 1.0 (infinity)
    val selectedFilter: CinematicFilter = CinematicFilter.NONE,
    val showFilterSelector: Boolean = false,
    // Dual Camera / PIP Mode
    val isDualPipActive: Boolean = false,
    val isPipSwapped: Boolean = false, // false = Main is Back, Pip is Front; true = Main is Front, Pip is Back
    val pipPosition: PipPosition = PipPosition.TOP_END,
    // Video Recording
    val isRecordingVideo: Boolean = false,
    val videoDurationSeconds: Int = 0,
    // Cinematic Mode settings
    val isCinematicAnamorphic: Boolean = false,
    val filmFps: Int = 24,
    // Photos & feedback
    val lastCapturedPhoto: CapturedPhoto? = null,
    val isCapturing: Boolean = false,
    val focusPoint: Pair<Float, Float>? = null, // Normalized x, y in viewfinder
    val focusLocked: Boolean = false,
    val language: AppLanguage = AppLanguage.ARABIC
)
