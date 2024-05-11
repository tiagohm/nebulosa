package nebulosa.api.alignment.polar.tppa

enum class TPPAState {
    IDLE,
    SLEWING,
    SLEWED,
    SOLVING,
    SOLVED,
    COMPUTED,
    FINISHED,
    FAILED,
}
