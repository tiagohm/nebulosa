package nebulosa.api.alignment.polar.tppa

enum class TPPAState {
    SLEWING,
    SOLVING,
    SOLVED,
    PAUSED,
    COMPUTED,
    FAILED,
    FINISHED,
}
