package nebulosa.api.alignment.polar.tppa

enum class TPPAState {
    IDLE,
    SLEWING,
    SLEWED,
    SETTLING,
    SOLVING,
    SOLVED,
    COMPUTED,
    FINISHED,
    FAILED,
}
