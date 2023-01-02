package nebulosa.horizons;

enum class HorizonsQuantity(val code: Int, val size: Int) {
    ASTROMETRIC_RA_DEC(1, 2),
    APPARENT_AZ_ALT(4, 2),
    VISUAL_MAGNITUDE_SURFACE_BRIGHTNESS(9, 2),
    ILLUMINATED_FRACTION(10, 1),
    ANGULAR_DIAMETER(13, 1),
    CONSTELLATION(29, 1),
    APPARENT_HOUR_ANGLE(42, 1),
}
