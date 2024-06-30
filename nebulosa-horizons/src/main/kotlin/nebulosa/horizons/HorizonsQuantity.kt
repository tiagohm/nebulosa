package nebulosa.horizons

enum class HorizonsQuantity(
    @JvmField internal val code: Int,
    @JvmField internal val numberOfColumns: Int,
    @JvmField internal vararg val title: String,
) {
    /**
     * Adjusted for light-time aberration only. With respect to the reference
     * plane and equinox of the chosen system (ICRF or FK4/B1950). If the
     * FK4/B1950 frame output is selected, elliptic aberration terms are added.
     * Astrometric RA/DEC is generally used when comparing or reducing data
     * against a star catalog.
     */
    ASTROMETRIC_RA(1, 1, "R.A.", "(ICRF)"),
    ASTROMETRIC_DEC(1, 1, "DEC", "(ICRF)"),

    /**
     * Apparent coordinates are with respect to the true-equator and Earth
     * equinox of-date coordinate system (EOP-corrected IAU76/80 precession
     * and nutation of the spin-pole) and adjusted to model light-time, the
     * gravitational deflection of light, and stellar aberration, with an
     * optional (approximate) correction for atmospheric yellow-light
     * refraction.
     */
    APPARENT_RA(2, 1, "R.A.", "(a-app)"),
    APPARENT_DEC(2, 1, "DEC", "(a-app)"),

    /**
     * Apparent azimuth and elevation of target. Adjusted for light-time,
     * the gravitational deflection of light, stellar aberration, precession and
     * nutation. There is an optional (approximate) adjustment for atmospheric
     * refraction (Earth only). Azimuth is measured clockwise from north.
     *
     * Elevation angle is with respect to plane perpendicular to local zenith
     * direction.
     */
    APPARENT_AZ(4, 1, "Azimuth", "(a-app)"),
    APPARENT_ALT(4, 1, "Elevation", "(a-app)"),
    APPARENT_REFRACTED_AZ(4, 1, "Azimuth", "(r-app)"),
    APPARENT_REFRACTED_ALT(4, 1, "Elevation", "(r-app)"),

    /**
     * The angle measured westward in the body true-equator of-date plane
     * from the meridian containing the body-fixed observer to the meridian
     * containing the true Earth equinox (defined by intersection of the true
     * equator of date with the ecliptic of date).
     */
    LOCAL_APPARENT_SIDEREAL_TIME(7, 1, "L_Ap_Sid_Time"),

    /**
     * Approximate airless visual magnitude & surface brightness, where surface
     * brightness is the average airless visual magnitude of a square-arcsecond
     * of the illuminated portion of the apparent disk.
     */
    VISUAL_MAGNITUDE(9, 1, "APmag"),

    /**
     * Surface brightness. S-brt= V + 2.5*log10(k*PI*a*b')
     */
    SURFACE_BRIGHTNESS(9, 1, "S-brt"),

    /**
     * Percent of target objects' assumed circular disk illuminated by Sun
     * (phase), as seen by observer.
     */
    ILLUMINATED_FRACTION(10, 1, "Illu%"),

    /**
     * The equatorial angular width of the target body full disk, if it were
     * fully illuminated and visible to the observer. If the target body diameter
     * is unknown, "n.a." is output.
     */
    ANGULAR_DIAMETER(13, 1, "Ang-diam"),

    /**
     * Target one-way down-leg light-time, as seen by observer. The elapsed
     * time since light (observed at print-time) left or reflected off the
     * target. Unit in minutes.
     */
    ONE_WAY_LIGHT_TIME(21, 1, "1-way_down_LT"),

    /**
     * Sun-Observer-Target apparent SOLAR ELONGATION ANGLE seen from the
     * observers' location at print-time.
     *
     * /T indicates target TRAILS Sun (evening sky: rises and sets AFTER Sun).
     * /L indicates target LEADS Sun (morning sky: rises and sets BEFORE Sun).
     */
    SUN_OBSERVER_TARGET_ELONGATION_ANGLE(23, 2, "S-O-T"),

    /**
     * The 3-letter abbreviation for the constellation name of targets'
     * astrometric position, as defined by the IAU (1930) boundary delineation.
     * See documentation for list of abbreviations.
     */
    CONSTELLATION(29, 1, "Cnst"),

    /**
     * Local Apparent SOLAR Time at observing site.
     */
    LOCAL_APPARENT_SOLAR_TIME(34, 1, "L_Ap_SOL_Time"),

    /**
     * Local apparent HOUR ANGLE of target at observing site. The angle between
     * the observers' meridian plane, containing Earth's axis of-date and local
     * zenith direction, and a great circle passing through Earth's axis-of-date
     * and the targets' direction, measured westward from the zenith meridian to
     * target meridian along the equator. Negative values are angular times UNTIL
     * transit. Positive values are angular times SINCE transit.
     */
    APPARENT_HOUR_ANGLE(42, 1, "L_Ap_Hour_Ang");

    internal fun matches(text: String) = title.all { text.contains(it, true) }

    companion object {

        @JvmStatic internal val ENTRIES = entries.toTypedArray()

        @JvmStatic
        fun parse(text: String) = ENTRIES.firstOrNull { it.matches(text) }
    }
}
