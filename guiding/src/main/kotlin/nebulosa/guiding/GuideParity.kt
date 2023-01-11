package nebulosa.guiding

enum class GuideParity {
    EVEN,      // Guide(NORTH) moves scope north
    ODD,      // Guide(NORTH) moves scope south
    UNKNOWN,   // we don't know or care
    UNCHANGED, // special case for SetCalibration, leave value unchanged
}
