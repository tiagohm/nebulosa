package nebulosa.nova.astrometry

import nebulosa.constants.DAYSEC
import nebulosa.constants.SPEED_OF_LIGHT
import nebulosa.coordinates.SphericalRepresentation
import nebulosa.math.*
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Distance.Companion.au
import nebulosa.math.Distance.Companion.km
import nebulosa.math.Pressure.Companion.mbar
import nebulosa.math.Temperature.Companion.celsius
import nebulosa.math.Velocity.Companion.kms
import nebulosa.nova.frame.Frame
import nebulosa.nova.frame.ITRS
import nebulosa.nova.position.*
import nebulosa.time.InstantOfTime
import nebulosa.time.TimeJD
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * An |xyz| position and velocity oriented to the ICRF axes.
 *
 * The International Coordinate Reference Frame (ICRF) is a permanent
 * reference frame that is the replacement for J2000. Their axes agree
 * to within 0.02 arcseconds. It also supersedes older equinox-based
 * systems like B1900 and B1950.
 *
 * @param position The |xyz| position in AU.
 * @param velocity The |xyz| velocity in AU/day.
 */
open class ICRF protected constructor(
    val position: Vector3D,
    val velocity: Vector3D,
    val time: InstantOfTime,
    val center: Number,
    val target: Number,
) {

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun component1() = position

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun component2() = velocity

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun component3() = time

    /**
     * Distance from [center] to [target] at [time].
     */
    inline val distance get() = position.length.km

    /**
     * Speed at [time].
     */
    inline val speed get() = velocity.length.kms

    /**
     * Length of this vector in days of light travel time.
     */
    inline val lightTime get() = position.length * (1000.0 / (SPEED_OF_LIGHT * DAYSEC))

    /**
     * Computes the equatorial (RA, declination, distance)
     * with respect to the fixed axes of the ICRF.
     */
    fun equatorial() = SphericalRepresentation.of(position)

    /**
     * Computes the equatorial (RA, declination, distance)
     * referenced to the dynamical system defined by
     * the Earth's true equator and equinox at specific [epoch] time.
     */
    fun equatorialAtEpoch(epoch: InstantOfTime) = SphericalRepresentation.of(epoch.m * position)

    /**
     * Computes the equatorial (RA, declination, distance)
     * referenced to the dynamical system defined by
     * the Earth's true equator and equinox of [time].
     */
    fun equatorialAtDate() = equatorialAtEpoch(time)

    /**
     * Computes the equatorial (RA, declination, distance)
     * referenced to the dynamical system defined by
     * the Earth's true equator and equinox of J2000.0.
     */
    fun equatorialJ2000() = equatorialAtEpoch(TimeJD.J2000)

    /**
     * Computes hour angle, declination, and distance.
     *
     * This only works for positions
     * whose center is a geographic location; otherwise, there is no
     * local meridian from which to measure the hour angle.
     *
     * Because this declination is measured from the plane of the
     * Earth’s physical geographic equator, it will be slightly
     * different than the declination returned by [equatorial].
     *
     * The coordinates are not adjusted for atmospheric refraction near
     * the horizon.
     */
    @Suppress("LocalVariableName")
    fun hourAngle(): SphericalRepresentation {
        require(center is GeographicPosition) { "the center must be a GeographicPosition" }

        val R = ITRS.rotationAt(time)
        val r = R * position
        val (sublongitude, dec, distance) = r

        val ha = (center.longitude - sublongitude).normalized

        return SphericalRepresentation(ha, dec.rad, distance.au)
    }

    /**
     * Computes the parallactic angle, which is the deviation between zenith angle and north angle.
     */
    fun parallacticAngle(): Angle {
        val (ha, delta) = hourAngle()
        val phi = (center as GeographicPosition).latitude
        // A rare condition! Object exactly in zenith, avoid undefined result.
        return if (ha == 0.0 && (delta - phi.value) == 0.0) Angle.ZERO
        else atan2(sin(ha), phi.tan * cos(delta) - sin(delta) * cos(ha)).rad
    }

    /**
     * Computes cartesian CIRS coordinates at a given [epoch] |xyz|.
     */
    fun cirs(epoch: InstantOfTime = time) = epoch.c * position

    /**
     * Gets spherical CIRS coordinates at a given [epoch] (ra, dec, distance).
     */
    fun sphericalCIRS(epoch: InstantOfTime = time) = SphericalRepresentation(cirs(epoch))

    /**
     * Returns the [position] as an |xyz| position and velocity vector in a reference [frame].
     */
    @Suppress("LocalVariableName")
    fun frame(frame: Frame): Pair<Vector3D, Vector3D> {
        val R = frame.rotationAt(time)
        val r = R * position
        var v = R * velocity

        frame.dRdtTimesRtAt(time)?.also { v += it * r }

        return r to v
    }

    /**
     * Returns the longitude, latitude and distance in the given [frame].
     */
    fun latLon(frame: Frame): SphericalRepresentation {
        return SphericalRepresentation(frame.rotationAt(time) * position)
    }

    /**
     * Computes the angle between this [position] and [another].
     */
    fun separationFrom(another: ICRF) = position.angle(another.position)

    /**
     *  Returns the orientation of this observer.
     */
    val horizontalRotation by lazy {
        require(target is GeographicPosition || target is PlanetograhicPosition) {
            "to compute an altazimuth position, you must observe from " +
                "a specific Earth location or from a position on another body loaded from a set " +
                "of planetary constants"
        }

        (target as Frame).rotationAt(time)
    }

    operator fun minus(other: ICRF) = of(position - other.position, velocity - other.velocity, time, other.target, target)

    operator fun unaryMinus() = of(-position, -velocity, time, target, center, javaClass)

    companion object {

        @JvmStatic
        fun of(
            position: Vector3D,
            velocity: Vector3D,
            time: InstantOfTime,
            center: Number = Int.MIN_VALUE,
            target: Number = Int.MIN_VALUE,
            type: Class<out ICRF>? = null,
        ) = when {
            type == Barycentric::class.java || center.toInt() == 0 -> Barycentric(position, velocity, time, center, target)
            type == Geocentric::class.java || center.toInt() == 399 -> Geocentric(position, velocity, time, center, target)
            type == Geometric::class.java || center is GeographicPosition -> Geometric(position, velocity, time, center, target)
            else -> ICRF(position, velocity, time, center, target)
        }

        internal fun horizontal(
            position: ICRF,
            temperature: Temperature = 10.0.celsius,
            pressure: Pressure = 1013.0.mbar,
        ): SphericalRepresentation {
            val r = when {
                position is Astrometric -> position.barycenter.horizontalRotation
                position is Apparent -> position.barycenter.horizontalRotation
                position.center is Frame -> position.center.rotationAt(position.time)
                else -> throw IllegalArgumentException(
                    "to compute an altazimuth position, you must observe from " +
                        "a specific Earth location or from a position on another body loaded from a set " +
                        "of planetary constants"
                )
            }

            val h = r * position.position

            // TODO: return if (position.center is GeographicPosition) {
            //    SphericalRepresentation(position.center.refract(h.a2.rad, temperature, pressure), h.a1.rad, h.a3.au)
            //} else {
            return SphericalRepresentation(h)
            //}
        }
    }
}
