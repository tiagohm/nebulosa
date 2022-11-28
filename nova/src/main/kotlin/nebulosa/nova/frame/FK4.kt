package nebulosa.nova.frame

/**
 * Fundamental Catalog (4). The FK4 reference
 * frame is derived from the [B1950] frame by
 * applying the equinox offset determined by
 * Fricke.
 */
@Suppress("FloatingPointLiteralPrecision")
object FK4 : InertialFrame(
    0.99992567949568767, 0.011181483239171792, 0.0048590037723143858,
    -0.01118148322046629, 0.99993748489331347, -2.7170293744002029E-05,
    -0.0048590038153592712, -2.7162594714247048E-05, 0.9999881946023742,
)
