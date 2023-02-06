package nebulosa.query.horizons;

enum class HorizonsQuantity(
    @JvmField internal val code: Int,
    @JvmField internal val match: String,
) {
    ASTROMETRIC_RA(1, "R.A.___(ICRF)"),
    ASTROMETRIC_DEC(1, "DEC____(ICRF)"),
    APPARENT_RA(2, "R.A.__(a-app)"),
    APPARENT_DEC(2, "DEC___(a-app)"),
    APPARENT_AZ(4, "Azimuth_(a-app)"),
    APPARENT_ALT(4, "Elevation_(a-app)"),
    LOCAL_APPARENT_SIDEREAL_TIME(7, "L_Ap_Sid_Time"),
    VISUAL_MAGNITUDE(9, "APmag"),
    SURFACE_BRIGHTNESS(9, "S-brt"),
    ILLUMINATED_FRACTION(10, "Illu%"),
    ANGULAR_DIAMETER(13, "Ang-diam"),
    CONSTELLATION(29, "Cnst"),
    LOCAL_APPARENT_SOLAR_TIME(34, "L_Ap_SOL_Time"),
    APPARENT_HOUR_ANGLE(42, "L_Ap_Hour_Ang");

    fun matches(text: String) = match.equals(text, true)

    companion object {

        @JvmStatic internal val ENTRIES = values()

        @JvmStatic
        fun parse(text: String) = ENTRIES.firstOrNull { it.matches(text) }
    }
}
