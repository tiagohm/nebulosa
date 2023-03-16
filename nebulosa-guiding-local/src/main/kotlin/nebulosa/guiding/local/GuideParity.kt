package nebulosa.guiding.local

enum class GuideParity {
    EVEN,      // Guide(NORTH) moves scope north.
    ODD,       // Guide(NORTH) moves scope south.
    UNKNOWN,   // We don't know or care.
    UNCHANGED, // Special case for SetCalibration, leave value unchanged.
}
