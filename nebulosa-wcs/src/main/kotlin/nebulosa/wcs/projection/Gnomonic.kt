package nebulosa.wcs.projection

import nebulosa.constants.DEG2RAD
import nebulosa.constants.RAD2DEG
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.PairOfAngle
import nebulosa.wcs.projection.Projection.Companion.aatan2

/**
 * Gnomonic.
 *
 * The zenithal perspective projection with mu = 0, the gnomonic projection, is
 * widely used in optical astronomy and was given its own code within the AIPS
 * convention, namely TAN.
 *
 * @see <a href="http://www.atnf.csiro.au/people/mcalabre/WCS/ccs.pdf">Reference (page 12)</a>
 */
class Gnomonic internal constructor(
    crval1: Double,
    crval2: Double,
) : ZenithalProjection(crval1.deg, crval2.deg) {

    override val type = ProjectionType.TAN

    init {
        phip = computeDefaultValueForPhip()
        computeCelestialCoordinateOfNativePole(phip)
    }

    override fun project(x: Double, y: Double): PairOfAngle {
        val radius = computeRadius(x * DEG2RAD, y * DEG2RAD)
        val phi = computePhi(x, y, radius)
        val theta = aatan2(1.0, radius).rad
        return PairOfAngle(phi, theta)
    }

    override fun unproject(longitude: Angle, latitude: Angle): DoubleArray {
        val s = latitude.sin
        require(s != 0.0) { "solution not defined for latitude == 0°" }
        val r = latitude.cos / s
        val x = computeX(r, longitude)
        val y = computeY(r, longitude)
        return doubleArrayOf(x * RAD2DEG, y * RAD2DEG)
    }

    override fun inside(longitude: Angle, latitude: Angle): Boolean {
        val (_, theta) = computeNativeSphericalCoordinateFromCelestial(longitude.normalized, latitude)
        return theta.value != 0.0 && super.inside(longitude, latitude)
    }
}
