package nebulosa.nova.frame

/**
 * Ecliptic coordinates based upon the [J2000] frame.
 *
 * Ecliptic obliquity at J2000.0 = 23.4392803055555555555556 degrees
 * M = {1.0, 0.0, 0.0, 0.0, cos(ecliptic), sin(ecliptic), 0.0, -sin(ecliptic), cos(ecliptic)}
 */
object EclipticJ2000 : InertialFrame(
    1.0, 0.0, 0.0,
    0.0, 0.9174821370869625, 0.3977769829016507,
    0.0, -0.3977769829016507, 0.9174821370869625,
)
