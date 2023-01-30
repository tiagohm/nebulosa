package nebulosa.guiding.local

enum class GuiderState {
    UNINITIALIZED,
    SELECTING,
    SELECTED,
    CALIBRATING_PRIMARY,
    CALIBRATING_SECONDARY,
    CALIBRATED,
    GUIDING,
    STOP,
}
