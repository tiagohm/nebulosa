package nebulosa.nova.frame

/**
 * Ecliptic coordinates based upon the [J2000] frame.
 *
 * Ecliptic obliquity at J2000.0 = 23.4392803055555555555556 degrees
 * M = {1.0, 0.0, 0.0, 0.0, cos(ecliptic), sin(ecliptic), 0.0, -sin(ecliptic), cos(ecliptic)}
 */
@Suppress("FloatingPointLiteralPrecision")
data object EclipticJ2000 : InertialFrame(
    1.0, 0.0, 0.0,
    0.0, 0.917482137086962521575615807374, 0.397776982901650696710316869067,
    0.0, -0.397776982901650696710316869067, 0.917482137086962521575615807374,
)
