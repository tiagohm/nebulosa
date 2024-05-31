package nebulosa.api.autofocus

enum class AutoFocusState {
    IDLE,
    MOVING,
    EXPOSURING,
    EXPOSURED,
    ANALYSING,
    ANALYSED,
    FOCUS_POINT_ADDED,
    CURVE_FITTED,
    FAILED,
    FINISHED,
}
