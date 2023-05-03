package nebulosa.guiding.internal

enum class GuiderState {
    UNINITIALIZED,
    SELECTING,
    SELECTED,
    CALIBRATING,
    CALIBRATED,
    GUIDING,
    STOP,
}
