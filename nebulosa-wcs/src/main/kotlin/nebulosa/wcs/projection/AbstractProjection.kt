package nebulosa.wcs.projection

import nebulosa.constants.PIOVERTWO
import nebulosa.constants.TAU
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.PairOfAngle
import nebulosa.wcs.projection.Projection.Companion.aatan2
import kotlin.math.*

abstract class AbstractProjection : Projection {

    internal var celestialCoordinateNativePole = PairOfAngle(Angle.ZERO, Angle.ZERO)
        private set

    abstract override var phi0: Angle
        internal set

    abstract override var theta0: Angle
        internal set

    override var phip = Angle.ZERO
        internal set

    override var thetap = Angle.QUARTER
        internal set

    override fun computeCelestialSphericalCoordinate(x: Double, y: Double): PairOfAngle {
        val (phi, theta) = project(x, y)
        return computeCelestialSphericalCoordinateFromNative(phi, theta)
    }

    override fun computeProjectionPlaneCoordinate(rightAscension: Angle, declination: Angle): DoubleArray {
        val (phi, theta) = computeNativeSphericalCoordinateFromCelestial(rightAscension.normalized, declination)
        return unproject(normalizePhi(phi), theta)
    }

    /**
     * Computes the default value for ϕp.
     *
     * The default value of ϕp will be [LONPOLE_0] for δ0 ≥ θ0 or
     * [LONPOLE_PI] for δ0 < θ0.
     */
    protected fun computeDefaultValueForPhip(): Angle {
        return if (crval2.value >= theta0.value) LONPOLE_0
        else LONPOLE_PI
    }

    protected fun computeCelestialCoordinateOfNativePole(phi: Angle) {
        celestialCoordinateNativePole = if (phi0.value == 0.0 && theta0.value == PIOVERTWO) {
            PairOfAngle(crval1, crval2)
        } else {
            val deltap = computeLatitudeNativePole(phi)
            val alphap = computeLongitudeNativePole(deltap, phi)
            PairOfAngle(deltap, alphap)
        }
    }

    internal fun computeLatitudeNativePole(phi: Angle): Angle {
        if (theta0.value == 0.0 &&
            crval2.value == 0.0 &&
            abs(phi.value - phi0.value) == PIOVERTWO
        ) {
            return thetap
        } else {
            val deltapArg = aatan2(theta0.sin, theta0.cos * (phi - phi0).cos)
            val deltapAcos = acos(crval2.sin / sqrt(1.0 - theta0.cos.pow(2.0) * (phi - phi0).sin.pow(2.0)))
            val deltap1 = deltapArg + deltapAcos
            val deltap2 = deltapArg - deltapAcos

            if (theta0.value == 0.0 &&
                crval2.value == 0.0 &&
                abs(phip.value - phi0.value) == PIOVERTWO
            ) {
                return thetap
            } else {
                val validInterval = -PIOVERTWO..PIOVERTWO
                val isDeltap1InInterval = deltap1 in validInterval
                val isDeltap2InInterval = deltap2 in validInterval

                return if (isDeltap1InInterval && isDeltap2InInterval) {
                    val diff1 = abs(deltap1 - thetap.value)
                    val diff2 = abs(deltap2 - thetap.value)
                    if (diff1 < diff2) deltap1.rad else deltap2.rad
                } else if (isDeltap1InInterval) {
                    deltap1.rad
                } else if (isDeltap2InInterval) {
                    deltap2.rad
                } else {
                    throw IllegalStateException("no valid solution for thetap")
                }
            }
        }
    }

    internal fun computeLongitudeNativePole(deltap: Angle, phip: Angle): Angle {
        return if (abs(crval2.value) == PIOVERTWO) crval1
        else if (deltap.value == PIOVERTWO) crval1 + phip - phi0 - PI
        else if (deltap.value == -PIOVERTWO) crval1 - phip + phi0
        else {
            val das = (phip - phi0).sin * theta0.cos / crval2.cos
            val dac = (theta0.sin - deltap.value * crval2.sin) / (deltap.cos * crval2.cos)
            crval1 - aatan2(das, dac)
        }
    }

    /**
     * Computes the celestial spherical coordinates from the
     * native spherical coordinates.
     *
     * The computation is performed by applying the
     * spherical coordinate rotation.
     */
    internal fun computeCelestialSphericalCoordinateFromNative(phi: Angle, theta: Angle): PairOfAngle {
        val (ap, dp) = celestialCoordinateNativePole

        return when (dp.value) {
            PIOVERTWO -> {
                PairOfAngle((ap + phi - phip - PI).normalized, theta)
            }
            -PIOVERTWO -> {
                PairOfAngle((ap - phi + phip).normalized, -theta)
            }
            else -> {
                // α = αp + atan2(sinθ cosδp − cosθ sinδp cos(φ − φp), −cosθ sin(φ − φp))
                val y = -(theta.cos) * (phi - phip).sin
                var x = theta.sin * dp.cos - theta.cos * dp.sin * (phi - phip).cos
                val rightAscension = ap + aatan2(y, x)
                // δ = asin(sinθ sinδp + cosθ cosδp cos(φ − φp))
                x = theta.sin * dp.sin + theta.cos * dp.cos * (phi - phip).cos
                val declination = asin(x).rad
                PairOfAngle(rightAscension.normalized, declination)
            }
        }
    }

    /**
     * Computes the native spherical coordinates from
     * the celestial spherical coordinates.
     *
     * The computation is performed by applying the inverse
     * of the spherical coordinate rotation.
     */
    internal fun computeNativeSphericalCoordinateFromCelestial(rightAscension: Angle, declination: Angle): PairOfAngle {
        val (rap, decp) = celestialCoordinateNativePole

        return when (decp.value) {
            PIOVERTWO -> {
                PairOfAngle(phip + rightAscension - rap, declination)
            }
            -PIOVERTWO -> {
                PairOfAngle(phip - rightAscension + rap, -declination)
            }
            else -> {
                //  φ = φp + atan2(sinδ cosδp − cosδ sinδp cos(α − αp), −cosδ sin(α − αp))
                val y = -(declination.cos) * (rightAscension - rap).sin
                var x = declination.sin * decp.cos - declination.cos * decp.sin * (rightAscension - rap).cos
                val phi = phip + aatan2(y, x)
                // θ = asin(sinδ sinδp + cosδ cosδp cos(α − αp))
                x = declination.sin * decp.sin + declination.cos * decp.cos * (rightAscension - rap).cos
                val theta = asin(x).rad
                PairOfAngle(phi, theta)
            }
        }
    }

    private fun normalizePhi(phi: Angle): Angle {
        val phiCorrect = phi.value % TAU
        return if (phiCorrect > PI) (phiCorrect - TAU).rad
        else if (phiCorrect < -PI) (phiCorrect + TAU).rad
        else phiCorrect.rad
    }

    companion object {

        @JvmStatic val LONPOLE_0 = Angle.ZERO
        @JvmStatic val LONPOLE_PI = Angle.SEMICIRCLE
    }
}
