@file:JvmName("DateConstants")

package nebulosa.constants

/**
 * Reference epoch (J2000.0), Julian Date.
 */
const val J2000 = 2451545.0

/**
 * Reference epoch (B1950.0), Julian Date.
 */
const val B1950 = 2433282.4235

/**
 * Seconds per day.
 */
const val DAYSEC = 86400.0

/**
 * Minutes per day.
 */
const val DAYMIN = 1440.0

/**
 * Days per Julian year.
 */
const val DAYSPERJY = 365.25

/**
 * Days per Julian century.
 */
const val DAYSPERJC = 36525.0

/**
 * Days per Julian millennium.
 */
const val DAYSPERJM = 365250.0

/**
 * Julian Date of Modified Julian Date zero.
 */
const val MJD0 = 2400000.5

/**
 * Reference epoch (J2000.0), Modified Julian Date.
 */
const val MJD2000 = 51544.5

/**
 * 1977 Jan 1.0 as MJD.
 */
const val MJD1977 = 43144.0

/**
 * Length of tropical year B1900 (days).
 */
const val DTY = 365.242198781

/**
 * TT minus TAI (s).
 */
const val TTMINUSTAI = 32.184

/**
 * L_G = 1 - d(TT)/d(TCG).
 */
const val ELG = 6.969290134E-10

/**
 * L_B = 1 - d(TDB)/d(TCB).
 */
const val ELB = 1.550519768E-8

/**
 * TDB (s) at TAI 1977/1/1.0.
 */
const val TDB0 = -6.55E-5
