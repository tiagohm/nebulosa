package nebulosa.guiding

enum class GuideState {
    STOPPED,
    SELECTED,
    CALIBRATING,
    GUIDING,
    LOST_LOCK,
    PAUSED,
    LOOPING,
}
