package com.example.model

enum class AppLanguage(val code: String, val displayName: String, val shortCode: String) {
    ARABIC("ar", "العربية", "عربي"),
    ENGLISH("en", "English", "EN")
}

object AppStrings {
    fun appName(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "كاميرا احترافية"
        AppLanguage.ENGLISH -> "Pro Camera"
    }

    fun permissionTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "كاميرا احترافية يدوية"
        AppLanguage.ENGLISH -> "PRO MANUAL CAMERA"
    }

    fun permissionDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "يتطلب التطبيق إذن الكاميرا لتوفير محدد المنظر المباشر، وضوابط التعريض اليدوية، والتقاط صور فائقة الدقة."
        AppLanguage.ENGLISH -> "Camera access is required for real-time viewfinder, manual exposure controls, and high-resolution photo capture."
    }

    fun grantPermission(lang: AppLanguage): String = when (lang) {
        AppLanguage.ARABIC -> "منح إذن الكاميرا"
        AppLanguage.ENGLISH -> "GRANT CAMERA ACCESS"
    }

    fun flash(lang: AppLanguage, mode: FlashMode): String = when (mode) {
        FlashMode.OFF -> if (lang == AppLanguage.ARABIC) "إيقاف" else "OFF"
        FlashMode.AUTO -> if (lang == AppLanguage.ARABIC) "تلقائي" else "AUTO"
        FlashMode.ON -> if (lang == AppLanguage.ARABIC) "تشغيل" else "ON"
        FlashMode.TORCH -> if (lang == AppLanguage.ARABIC) "كشاف" else "TORCH"
    }

    fun grid(lang: AppLanguage, type: GridType): String = when (type) {
        GridType.NONE -> if (lang == AppLanguage.ARABIC) "إيقاف" else "OFF"
        GridType.THIRDS -> if (lang == AppLanguage.ARABIC) "أثلاث" else "3×3"
        GridType.GOLDEN_RATIO -> if (lang == AppLanguage.ARABIC) "ذهبي" else "Φ"
        GridType.SQUARE -> if (lang == AppLanguage.ARABIC) "مربع" else "1:1"
    }

    fun gridTitle(lang: AppLanguage, type: GridType): String = when (type) {
        GridType.NONE -> if (lang == AppLanguage.ARABIC) "بدون شبكة" else "Off"
        GridType.THIRDS -> if (lang == AppLanguage.ARABIC) "قاعدة الأثلاث" else "Rule of Thirds"
        GridType.GOLDEN_RATIO -> if (lang == AppLanguage.ARABIC) "النسبة الذهبية" else "Golden Ratio"
        GridType.SQUARE -> if (lang == AppLanguage.ARABIC) "إطار مربع" else "Square Frame"
    }

    fun level(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "ميزان" else "LEVEL"
    fun histo(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "رسم" else "HISTO"

    fun timer(lang: AppLanguage, option: TimerOption): String = when (option) {
        TimerOption.OFF -> if (lang == AppLanguage.ARABIC) "إيقاف" else "OFF"
        TimerOption.THREE -> if (lang == AppLanguage.ARABIC) "٣ ث" else "3s"
        TimerOption.TEN -> if (lang == AppLanguage.ARABIC) "١٠ ث" else "10s"
    }

    fun modeTitle(lang: AppLanguage, mode: ManualControlMode): String = when (mode) {
        ManualControlMode.EV -> if (lang == AppLanguage.ARABIC) "قيمة التعريض (EV)" else "Exposure Value (EV)"
        ManualControlMode.ISO -> if (lang == AppLanguage.ARABIC) "حساسية المستشعر (ISO)" else "Sensitivity (ISO)"
        ManualControlMode.SEC -> if (lang == AppLanguage.ARABIC) "سرعة الغالق (SEC)" else "Shutter Speed (SEC)"
        ManualControlMode.WB -> if (lang == AppLanguage.ARABIC) "توازن الأبيض (WB)" else "White Balance (WB)"
        ManualControlMode.FOCUS -> if (lang == AppLanguage.ARABIC) "التركيز البؤري (MF)" else "Focus Distance (MF)"
    }

    fun modeLabel(lang: AppLanguage, mode: ManualControlMode): String = when (mode) {
        ManualControlMode.EV -> "EV"
        ManualControlMode.ISO -> "ISO"
        ManualControlMode.SEC -> if (lang == AppLanguage.ARABIC) "غالق" else "SEC"
        ManualControlMode.WB -> if (lang == AppLanguage.ARABIC) "أبيض" else "WB"
        ManualControlMode.FOCUS -> if (lang == AppLanguage.ARABIC) "تركيز" else "FOCUS"
    }

    fun whiteBalance(lang: AppLanguage, preset: WhiteBalancePreset): String = when (preset) {
        WhiteBalancePreset.AUTO -> if (lang == AppLanguage.ARABIC) "تلقائي" else "AWB"
        WhiteBalancePreset.DAYLIGHT -> if (lang == AppLanguage.ARABIC) "ضوء النهار" else "Daylight"
        WhiteBalancePreset.CLOUDY -> if (lang == AppLanguage.ARABIC) "غائم" else "Cloudy"
        WhiteBalancePreset.SHADE -> if (lang == AppLanguage.ARABIC) "ظل" else "Shade"
        WhiteBalancePreset.TUNGSTEN -> if (lang == AppLanguage.ARABIC) "تنجستن" else "Tungsten"
        WhiteBalancePreset.FLUORESCENT -> if (lang == AppLanguage.ARABIC) "فلورسنت" else "Fluorescent"
    }

    fun focusMode(lang: AppLanguage, mode: FocusModeOption): String = when (mode) {
        FocusModeOption.AF_C -> if (lang == AppLanguage.ARABIC) "تركيز مستمر" else "AF-C"
        FocusModeOption.AF_S -> if (lang == AppLanguage.ARABIC) "تركيز مفرد" else "AF-S"
        FocusModeOption.MANUAL -> if (lang == AppLanguage.ARABIC) "تركيز يدوي" else "MF"
    }

    fun macro(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "ماكرو" else "MACRO"
    fun infinity(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "لانهاية" else "INFINITY"
    fun resetZero(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "إعادة ضبط (0.0)" else "RESET 0.0"

    fun settingsTitle(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "إعدادات الكاميرا الاحترافية" else "PRO SETTINGS"
    fun languageSection(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "لغة التطبيق / LANGUAGE" else "APP LANGUAGE"
    fun gridSection(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "شبكة التكوين" else "COMPOSITION GRID"
    fun timerSection(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "المؤقت الذاتي" else "SELF-TIMER"
    
    fun spiritLevelTitle(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "ميزان الأفق الرقمي" else "Spirit Level / Horizon"
    fun spiritLevelSub(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "مؤشر جيروسكوبي ثنائي المحور لتسوية الأفق" else "Real-time dual axis gyroscopic tilt"
    
    fun histogramTitle(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "الرسم البياني اللوني (RGB)" else "RGB Histogram"
    fun histogramSub(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "منحنى توزيع الإضاءة والألوان المباشر" else "Luminance & color distribution curve"
    
    fun hapticsTitle(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "الاهتزاز اللمسي (Haptic)" else "Haptic Feedback"
    fun hapticsSub(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "نبض لمسي عند الالتقاط وتوازن الأفق" else "Tactile click on shutter & level lock"

    fun captureParams(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "معلمات الالتقاط الاحترافية" else "CAPTURE PARAMETERS"
    fun savedPath(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "تم الحفظ في Pictures/ProCamera" else "Saved to Pictures/ProCamera"
    fun share(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "مشاركة" else "Share"
    fun close(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "إغلاق" else "Close"

    fun isoLabel(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "الحساسية" else "ISO"
    fun shutterLabel(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "الغالق" else "SHUTTER"
    fun evLabel(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "التعريض" else "EXPOSURE"
    fun wbLabel(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "توازن اللون" else "WHITE BAL"
    fun focalLabel(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "البؤري" else "FOCAL"

    // Modes
    fun shootingMode(lang: AppLanguage, mode: CameraShootingMode): String = when (mode) {
        CameraShootingMode.PHOTO -> if (lang == AppLanguage.ARABIC) "صورة" else "PHOTO"
        CameraShootingMode.VIDEO -> if (lang == AppLanguage.ARABIC) "فيديو" else "VIDEO"
        CameraShootingMode.CINEMATIC -> if (lang == AppLanguage.ARABIC) "سينما" else "CINEMA"
        CameraShootingMode.DUAL_PIP -> if (lang == AppLanguage.ARABIC) "مزدوج" else "DUAL"
    }

    // Cinematic & Filters
    fun filtersHeader(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "فلاتر سينمائية احترافية (LUTs)" else "CINEMATIC LOOKS & LUTS"
    fun filterName(lang: AppLanguage, filter: CinematicFilter): String = if (lang == AppLanguage.ARABIC) filter.titleAr else filter.titleEn
    fun filterSub(lang: AppLanguage, filter: CinematicFilter): String = if (lang == AppLanguage.ARABIC) filter.subtitleAr else filter.subtitleEn
    fun cinematicRatio(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "إطار سينمائي 2.39:1" else "2.39:1 CinemaScope"
    fun cinematicFps(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "نمط سينمائي 24 إطار/ث" else "24 FPS Film Look"

    // Dual Camera (PIP)
    fun dualCameraTitle(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "تصوير مزدوج (أمامية + خلفية)" else "Dual Camera (Front + Back)"
    fun swapPip(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "تبديل الكاميرتين" else "Swap Cameras"
    fun flipCamera(lang: AppLanguage, isBack: Boolean): String = if (lang == AppLanguage.ARABIC) {
        if (isBack) "الكاميرا الخلفية" else "الكاميرا الأمامية"
    } else {
        if (isBack) "Rear Camera" else "Front Camera"
    }
    fun switchCamera(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "تبديل الكاميرا" else "Switch Camera"
    fun mainCamera(lang: AppLanguage, isBack: Boolean): String = if (lang == AppLanguage.ARABIC) {
        if (isBack) "الرئيسية: خلفية" else "الرئيسية: سيلفي"
    } else {
        if (isBack) "Main: Rear" else "Main: Front"
    }
    fun pipCamera(lang: AppLanguage, isBack: Boolean): String = if (lang == AppLanguage.ARABIC) {
        if (isBack) "النافذة: أمامية" else "النافذة: خلفية"
    } else {
        if (isBack) "PIP: Front" else "PIP: Rear"
    }

    // Video Recording
    fun recording(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "تسجيل..." else "REC"
    fun videoSaved(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "تم حفظ الفيديو بنجاح (MP4)" else "Video saved successfully (MP4)"
    fun playVideo(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "تشغيل الفيديو" else "Play Video"
    fun videoFileIndicator(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "ملف فيديو MP4 محفوظ" else "Saved MP4 Video"
    fun takeDualPhoto(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "التقاط صورة مخرج مزدوجة" else "Capture Dual Shot"
    fun recordDualVideo(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) "تسجيل فيديو مزدوج" else "Record Dual Video"

    fun simulationModeNotice(lang: AppLanguage): String = if (lang == AppLanguage.ARABIC) {
        "وضع المحاكاة نشط (المستشعر الفعلي غير متصل بالمحاكي)"
    } else {
        "Simulation Mode Active (Sensor preview in test environment)"
    }
}
