package nebulosa.api.alignment.polar.tppa

enum class TPPAState {
    IDLE,
    SLEWING,
    SOLVING,
    SOLVED,
    PAUSED,
    COMPUTED,
    FAILED,
    FINISHED,
}
