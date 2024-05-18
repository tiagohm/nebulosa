package nebulosa.api.alignment.polar.tppa

enum class TPPAState {
    IDLE,
    SLEWING,
    SLEWED,
    SETTLING,
    EXPOSURING,
    SOLVING,
    SOLVED,
    COMPUTED,
    PAUSING,
    PAUSED,
    FINISHED,
    FAILED,
}
