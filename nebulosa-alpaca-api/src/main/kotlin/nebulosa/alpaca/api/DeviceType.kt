package nebulosa.alpaca.api

import com.fasterxml.jackson.annotation.JsonValue

enum class DeviceType(@JsonValue val type: String) {
    CAMERA("Camera"),
    TELESCOPE("Telescope"),
    FOCUSER("Focuser"),
    FILTER_WHEEL("FilterWheel"),
    ROTATOR("Rotator"),
    DOME("Dome"),
    SWITCH("Switch"),
    COVER_CALIBRATOR("CoverCalibrator"),
    OBSERVING_CONDITIONS("ObservingConditions"),
    SAFETY_MONITOR("SafetyMonitor"),
    VIDEO("Video"),
}
