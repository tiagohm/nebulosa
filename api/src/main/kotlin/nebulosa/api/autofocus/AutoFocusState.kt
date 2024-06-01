package nebulosa.api.autofocus

enum class AutoFocusState {
    IDLE,
    MOVING,
    EXPOSURING,
    EXPOSURED,
    ANALYSING,
    ANALYSED,
    CURVE_FITTED,
    FAILED,
    FINISHED,
}
