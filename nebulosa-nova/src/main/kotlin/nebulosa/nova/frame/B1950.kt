package nebulosa.nova.frame

/**
 * Reference frame of the Earth’s mean equator and equinox at B1950.
 */
@Suppress("FloatingPointLiteralPrecision")
data object B1950 : InertialFrame(
    0.99992570795236291, 0.011178938126427691, 0.0048590038414544293,
    -0.011178938137770135, 0.9999375133499887, -2.715792625851078E-05,
    -0.0048590038153592712, -2.7162594714247048E-05, 0.9999881946023742,
)
