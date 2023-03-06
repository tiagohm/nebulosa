package nebulosa.wcs.projection

import nebulosa.constants.PIOVERTWO
import nebulosa.erfa.CartesianCoordinate
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Distance
import kotlin.math.atan2
import kotlin.math.hypot

/**
 *  Zenithal or azimuthal projections map the sphere directly onto a plane.
 *
 *  The native coordinate system is chosen to have the polar axis orthogonal to
 *  the plane of projection at the refer.
 */
abstract class ZenithalProjection(
    override val crval1: Angle,
    override val crval2: Angle,
) : AbstractProjection() {

    override var phi0 = DEFAULT_PHI0

    override var theta0 = DEFAULT_THETA0

    protected open fun computeRadius(x: Double, y: Double): Double {
        return hypot(x, y)
    }

    protected open fun computeX(radius: Double, phi: Angle): Double {
        return radius * phi.sin
    }

    protected open fun computeY(radius: Double, phi: Angle): Double {
        return -radius * phi.cos
    }

    protected open fun computePhi(x: Double, y: Double, radius: Double): Angle {
        return if (radius == 0.0) Angle.ZERO else atan2(x, -y).rad
    }

    override fun inside(longitude: Angle, latitude: Angle): Boolean {
        val a = CartesianCoordinate.of(crval1, crval2, Distance.ONE)
        val b = CartesianCoordinate.of(longitude, latitude, Distance.ONE)
        val dist = a.angularDistance(b)
        return dist.value <= PIOVERTWO
    }

    override fun isLineToDraw(x0: Double, y0: Double, x1: Double, y1: Double): Boolean {
        return true
    }

    companion object {

        @JvmStatic val DEFAULT_PHI0 = Angle.ZERO
        @JvmStatic val DEFAULT_THETA0 = Angle.QUARTER
    }
}
