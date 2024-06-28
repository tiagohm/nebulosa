package nebulosa.nova.frame

/**
 * Ecliptic coordinates based upon the [B1950] frame.
 */
@Suppress("FloatingPointLiteralPrecision")
data object EclipticB1950 : InertialFrame(
    0.99992570795236291, 0.011178938126427691, 0.0048590038414544293,
    -0.012189277138214926, 0.91736881787898283, 0.39785157220522011,
    -9.9405009203520217E-06, -0.3978812427417045, 0.91743692784599817,
)
