package nebulosa.nova.frame

/**
 * The FK5 is an equatorial coordinate system
 * (coordinate system linked to the Earth) based on its J2000 position.
 * As any equatorial frame, the FK5-based follows
 * the long-term Earth motion (precession).
 */
@Suppress("FloatingPointLiteralPrecision")
object FK5 : InertialFrame(
    0.9999999999999928638, 0.0000001110223372305, 0.0000000441180342698,
    -0.0000001110223329741, 0.9999999999999891830, -0.0000000964779274389,
    -0.0000000441180449810, 0.0000000964779225408, 0.9999999999999943728,
)
