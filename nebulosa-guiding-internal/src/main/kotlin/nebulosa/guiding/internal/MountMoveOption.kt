package nebulosa.guiding.internal

internal enum class MountMoveOption {
    ALGORITHM_RESULT, // Filter move through guide algorithm.
    ALGORITHM_DEDUCE, // Use guide algorithm to deduce the move amount (when paused or star lost).
    USE_BACKSLASH_COMPENSATION, // Use backlash comp for this move.
    MANUAL, // Manual move - allow even when guiding disabled.
}
