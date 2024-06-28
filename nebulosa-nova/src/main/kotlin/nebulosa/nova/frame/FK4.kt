package nebulosa.nova.frame

/**
 * Fundamental Catalog (4). The FK4 reference
 * frame is derived from the [B1950] frame by
 * applying the equinox offset determined by
 * Fricke.
 */
@Suppress("FloatingPointLiteralPrecision")
data object FK4 : InertialFrame(
    0.9999256809514446605, 0.0111813717563032290, 0.0048589607363144413,
    -0.0111813722062681620, 0.9999374861373183740, -0.0000270733285476070,
    -0.0048589597008613673, -0.0000272585320447865, 0.9999881948141177111,
)
