package nebulosa.nova.position

import nebulosa.constants.TAU
import nebulosa.math.*
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
     * Returns the geographic position of a given [longitude], [latitude] and [elevation].
     *
     * Longitude is positive towards the east, so supply a negative
     * number for west.
     */
    fun latLon(
        longitude: Angle, latitude: Angle,
        elevation: Distance = 0.0,
    ): GeographicPosition {
        val sinphi = latitude.sin
        val cosphi = latitude.cos

        val c = 1.0 / sqrt(cosphi * cosphi + sinphi * sinphi * oneMinusFlatteningSquared)
        val s = oneMinusFlatteningSquared * c

        val radiusXY = radius * c + elevation
        val radiusZ = radius * s + elevation

        val xy = radiusXY * cosphi
        val x = xy * longitude.cos
        val y = xy * longitude.sin

        val itrs = Vector3D(y, x, (radiusZ * sinphi))

        return GeographicPosition(longitude, latitude, elevation, itrs, this)
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

        val a = radius
        val f = 1.0 / inverseFlattening
        val e2 = 2.0 * f - f * f
        var c = 1.0

        repeat(3) {
            c = 1.0 / sqrt(1.0 - e2 * sin(lat).pow(2))
            lat = atan2(z + a * c * e2 * sin(lat), r)
        }

        val elevation = r / cos(lat) - a * c

        return GeographicPosition(lon.rad, lat.rad, elevation.au, xyz, this)
    }

    companion object {

        @JvmStatic val GRS80 = Geoid("GRS80", 6378137.0.m, 298.257222101)

        @JvmStatic val WGS72 = Geoid("WGS72", 6378135.0.m, 298.26)

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
