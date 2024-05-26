package nebulosa.api.autofocus

enum class AutoFocusState {
    IDLE,
    MOVING,
    EXPOSURING,
    COMPUTING,
    FOCUS_POINT_ADDED,
    FAILED,
    FINISHED,
}
