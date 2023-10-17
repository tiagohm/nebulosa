package nebulosa.horizons

enum class HorizonsQuantity(
    @JvmField internal val code: Int,
    @JvmField internal val numberOfColumns: Int,
    @JvmField internal vararg val title: String,
) {
    ASTROMETRIC_RA(1, 1, "R.A.", "(ICRF)"),
    ASTROMETRIC_DEC(1, 1, "DEC", "(ICRF)"),
    APPARENT_RA(2, 1, "R.A.", "(a-app)"),
    APPARENT_DEC(2, 1, "DEC", "(a-app)"),
    APPARENT_AZ(4, 1, "Azimuth", "(a-app)"),
    APPARENT_ALT(4, 1, "Elevation", "(a-app)"),
    LOCAL_APPARENT_SIDEREAL_TIME(7, 1, "L_Ap_Sid_Time"),
    VISUAL_MAGNITUDE(9, 1, "APmag"),
    SURFACE_BRIGHTNESS(9, 1, "S-brt"),
    ILLUMINATED_FRACTION(10, 1, "Illu%"),
    ANGULAR_DIAMETER(13, 1, "Ang-diam"),
    ONE_WAY_LIGHT_TIME(21, 1, "1-way_down_LT"),
    SUN_OBSERVER_TARGET_ELONGATION_ANGLE(23, 2, "S-O-T"),
    CONSTELLATION(29, 1, "Cnst"),
    LOCAL_APPARENT_SOLAR_TIME(34, 1, "L_Ap_SOL_Time"),
    APPARENT_HOUR_ANGLE(42, 1, "L_Ap_Hour_Ang");

    internal fun matches(text: String) = title.all { text.contains(it, true) }

    companion object {

        @JvmStatic internal val ENTRIES = entries.toTypedArray()

        @JvmStatic
        fun parse(text: String) = ENTRIES.firstOrNull { it.matches(text) }
    }
}
