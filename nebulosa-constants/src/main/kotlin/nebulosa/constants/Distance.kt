@file:JvmName("DistanceConstants")

package nebulosa.constants

/**
 * Astronomical unit (m, IAU 2012).
 */
const val AU_M = 149597870700.0

/**
 * Astronomical unit (km, IAU 2012).
 */
const val AU_KM = 149597870.700

/**
 * Speed of light (m/s).
 */
const val SPEED_OF_LIGHT = 299792458.0

/**
 * Light time (au per s).
 */
const val LIGHT_TIME_AU_S = AU_M / SPEED_OF_LIGHT

/**
 * Schwarzschild radius of the Sun (au).
 */
const val SCHWARZSCHILD_RADIUS_OF_THE_SUN = 1.97412574336e-8

/**
 * Speed of light (au per s).
 */
const val SPEED_OF_LIGHT_AU_DAY = SPEED_OF_LIGHT * DAYSEC / AU_M
