package nebulosa.nova.position

import nebulosa.constants.TAU
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Distance
import nebulosa.math.Distance.Companion.au
import nebulosa.math.Distance.Companion.m
import nebulosa.math.Vector3D
import nebulosa.nova.astrometry.ICRF
import nebulosa.nova.frame.ITRS
import kotlin.math.*

/**
 * An Earth ellipsoid.
 *
 * Maps latitudes and longitudes to |xyz| positions.
 */
data class Geoid(
    val name: String,
    val radius: Distance,
    val inverseFlattening: Double,
) {

    private val omf = (inverseFlattening - 1.0) / inverseFlattening
    private val oneMinusFlatteningSquared = omf * omf

    /**
     * Returns the geographic position of a given [latitude], [longitude] and [elevation].
     *
     * Longitude is positive towards the east, so supply a negative
     * number for west.
     */
    fun latLon(
        latitude: Angle,
        longitude: Angle,
        elevation: Distance = Distance.ZERO,
    ): GeographicPosition {
        val sinphi = latitude.sin
        val cosphi = latitude.cos

        val c = 1.0 / sqrt(cosphi * cosphi + sinphi * sinphi * oneMinusFlatteningSquared)
        val s = oneMinusFlatteningSquared * c

        val ach = radius * c + elevation
        val ash = radius * s + elevation

        val ac = ach * cosphi
        val acsst = ac * longitude.sin
        val accst = ac * longitude.cos

        val itrs = Vector3D(accst.value, acsst.value, (ash * sinphi).value)

        return GeographicPosition(latitude, longitude, elevation, itrs, this)
    }

    /**
     * Return Earth latitude and longitude beneath a celestial [position].
     *
     * The input [position] should have a center of 399, the
     * geocenter. The return value is a [GeographicPosition] whose
     * latitude and longitude are the spot on the Earth’s
     * surface directly beneath the given [position], and whose
     * elevation is the position’s distance above (or depth below)
     * mean sea level.

     * The underlying computation is based on Dr. T.S. Kelso's quite
     * helpful article "Orbital Coordinate Systems, Part III" at
     * https://www.celestrak.com/columns/v02n03.
     */
    fun subpoint(position: ICRF): GeographicPosition {
        require(position.center.toInt() == 399) {
            "a geographic subpoint can only be calculated for positions measured from 399," +
                " the center of the Earth, but this' position has center ${position.center}"
        }

        val (xyz) = position.frame(ITRS)
        val (x, y, z) = xyz

        val r = sqrt(x * x + y * y)
        val lon = (atan2(y, x) - PI) % TAU - PI
        var lat = atan2(z, r)

        val a = radius.value
        val f = 1.0 / inverseFlattening
        val e2 = 2.0 * f - f * f
        var c = 1.0

        repeat(3) {
            c = 1.0 / sqrt(1.0 - e2 * sin(lat).pow(2))
            lat = atan2(z + a * c * e2 * sin(lat), r)
        }

        val elevation = ((r / cos(lat)) - a * c)

        return GeographicPosition(lat.rad, lon.rad, elevation.au, xyz, this)
    }

    companion object {

        /**
         * World Geodetic System 1984 [Geoid].
         *
         * This is the standard geoid used by the GPS system,
         * and is likely the standard that’s intended
         * if you are supplied a latitude and longitude
         * that don’t specify an alternative geoid.
         */
        @JvmStatic val WGS84 = Geoid("WGS84", 6378137.0.m, 298.257223563)

        /**
         * International Earth Rotation Service 2010 [Geoid].
         */
        @JvmStatic val IERS2010 = Geoid("IERS2010", 6378136.6.m, 298.25642)
    }
}
