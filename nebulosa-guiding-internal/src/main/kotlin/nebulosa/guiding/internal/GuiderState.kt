package nebulosa.guiding.internal

enum class GuiderState {
    UNINITIALIZED,
    SELECTING,
    SELECTED,
    CALIBRATING,
    CALIBRATED,
    GUIDING,
    STOP;

    /**
     * Returns true for looping, but non-guiding states.
     */
    val looping
        get() = this <= SELECTED
}
