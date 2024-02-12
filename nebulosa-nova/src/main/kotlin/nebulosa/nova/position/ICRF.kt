package nebulosa.nova.position

import nebulosa.constants.DAYSEC
import nebulosa.constants.SPEED_OF_LIGHT
import nebulosa.erfa.CartesianCoordinate
import nebulosa.erfa.PositionAndVelocity
import nebulosa.erfa.SphericalCoordinate
import nebulosa.math.*
import nebulosa.nova.astrometry.Body
import nebulosa.nova.frame.Frame
import nebulosa.nova.frame.ITRS
import nebulosa.time.CurrentTime
import nebulosa.time.InstantOfTime
import kotlin.math.atan2

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
@Suppress("LeakingThis")
open class ICRF protected constructor(
    val position: Vector3D,
    val velocity: Vector3D,
    val time: InstantOfTime,
    val center: Number,
    val target: Number,
) {

    internal var centerBarycentric: ICRF? = null

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun component1() = position

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun component2() = velocity

    @Suppress("NOTHING_TO_INLINE")
    inline operator fun component3() = time

    init {
        if (center.toInt() == 0) {
            centerBarycentric = this
        }
    }

    /**
     * Distance from [center] to [target] at [time].
     */
    inline val distance
        get() = position.length.km

    /**
     * Speed at [time].
     */
    inline val speed
        get() = velocity.length.kms

    /**
     * Length of this vector in days of light travel time.
     */
    inline val lightTime
        get() = position.length * (1000.0 / (SPEED_OF_LIGHT * DAYSEC))

    /**
     * Computes the equatorial (RA, declination, distance)
     * with respect to the fixed axes of the ICRF.
     */
    fun equatorial() = SphericalCoordinate.of(position[0].au, position[1].au, position[2].au)

    /**
     * Computes the equatorial (RA, declination, distance)
     * referenced to the dynamical system defined by
     * the Earth's true equator and equinox at specific time
     * represented by its rotation [matrix].
     */
    fun equatorialAtEpoch(matrix: Matrix3D) = (matrix * position).let { SphericalCoordinate.of(it[0].au, it[1].au, it[2].au) }

    /**
     * Computes the equatorial (RA, declination, distance)
     * referenced to the dynamical system defined by
     * the Earth's true equator and equinox at specific [epoch] time.
     */
    fun equatorialAtEpoch(epoch: InstantOfTime) = equatorialAtEpoch(epoch.m)

    /**
     * Computes the equatorial (RA, declination, distance)
     * referenced to the dynamical system defined by
     * the Earth's true equator and equinox of [time].
     */
    fun equatorialAtDate() = equatorialAtEpoch(time)

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
    fun hourAngle(): SphericalCoordinate {
        require(center is GeographicPosition) { "the center must be a GeographicPosition" }

        val r = ITRS.rotationAt(time)
        val (sublongitude, dec, distance) = r * position

        val ha = (center.longitude - sublongitude).normalized

        return SphericalCoordinate(ha, dec.rad, distance.au)
    }

    /**
     * Computes the altitude, azimuth and distance relative to the observer's horizon.
     */
    fun horizontal(
        temperature: Temperature = 15.0.celsius,
        pressure: Pressure = ONE_ATM,
    ): SphericalCoordinate {
        // TODO: Uncomment when implement apparent method.
        // require(this !is Astrometric) {
        //     "it is not useful to call horizontal() on an astrometric position; " +
        //             "try calling apparent() first to get an apparent position"
        // }

        return horizontal(this, temperature, pressure)
    }

    /**
     * Computes the parallactic angle, which is the deviation
     * between zenith angle and north angle.
     */
    fun parallacticAngle(): Angle {
        val (ha, delta) = hourAngle()
        val phi = (center as GeographicPosition).latitude
        // A rare condition! Object exactly in zenith, avoid undefined result.
        return if (ha == 0.0 && (delta - phi) == 0.0) 0.0
        else atan2(ha.sin, phi.tan * delta.cos - delta.sin * ha.cos).rad
    }

    /**
     * Computes cartesian CIRS coordinates at a given [epoch] |xyz|.
     */
    fun cirs(epoch: InstantOfTime = time) = epoch.c * position

    /**
     * Gets spherical CIRS coordinates at a given [epoch] (ra, dec, distance).
     */
    fun sphericalCIRS(epoch: InstantOfTime = time) = cirs(epoch).let { SphericalCoordinate.of(it[0].au, it[1].au, it[2].au) }

    /**
     * Returns the [position] as an |xyz| position and velocity vector in a reference [frame].
     */
    fun frame(frame: Frame): PositionAndVelocity {
        val r = frame.rotationAt(time)
        val p = r * position
        var v = r * velocity

        frame.dRdtTimesRtAt(time)?.also { v += it * p }

        return PositionAndVelocity(p, v)
    }

    /**
     * Returns the longitude, latitude and distance in the given [frame].
     */
    fun latLon(frame: Frame): SphericalCoordinate {
        return (frame.rotationAt(time) * position).let { SphericalCoordinate.of(it[0].au, it[1].au, it[2].au) }
    }

    /**
     * Computes the angle between this [position] and [another].
     */
    fun separationFrom(another: ICRF) = position.angle(another.position)

    /**
     * Given a [sun] object, returns the [Angle] from the
     * body's point of view between light arriving from the Sun and the
     * light departing toward the observer. This angle is 0° if the
     * observer is in the same direction as the Sun and sees the body
     * as fully illuminated, and 180° if the observer is behind the
     * body and sees only its dark side.
     */
    fun phaseAngle(sun: Body): Angle {
        val s = sun.at<ICRF>(time)
        var v = position - s.position
        if (centerBarycentric != null) v += centerBarycentric!!.position
        return position.angle(v)
    }

    /**
     * Given a [sun] object computes what fraction from 0.0
     * to 1.0 of this target’s disc is illuminated, under the
     * assumption that the target is a sphere.
     */
    fun illuminated(sun: Body): Double {
        return 0.5 * (1.0 + phaseAngle(sun).cos)
    }

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
            type === Barycentric::class.java || center.toInt() == 0 -> Barycentric(position, velocity, time, center, target)
            type === Geometric::class.java || center is GeographicPosition -> Geometric(position, velocity, time, center, target)
            type === Geocentric::class.java || center.toInt() == 399 -> Geocentric(position, velocity, time, center, target)
            else -> ICRF(position, velocity, time, center, target)
        }

        @JvmStatic
        internal fun horizontal(
            position: ICRF,
            temperature: Temperature = 15.0.celsius,
            pressure: Pressure = ONE_ATM,
        ): SphericalCoordinate {
            val r = position.centerBarycentric?.horizontalRotation
                ?: (position.center as? Frame)?.rotationAt(position.time)
                ?: throw IllegalArgumentException(
                    "to compute an altazimuth position, you must observe from " +
                        "a specific Earth location or from a position on another body loaded from a set " +
                        "of planetary constants"
                )

            val coordinate = SphericalCoordinate.of(r * position.position)

            return if (position.center is GeographicPosition) {
                val refracted = position.center.refract(coordinate.latitude, temperature, pressure)
                coordinate.copy(phi = refracted)
            } else {
                coordinate
            }
        }

        /**
         * Builds a position from two vectors in a reference [frame] at the [time].
         */
        @JvmStatic
        fun frame(
            time: InstantOfTime,
            frame: Frame,
            distance: Vector3D,
            velocity: Vector3D,
            type: Class<out ICRF>? = null,
        ): ICRF {
            var r = distance
            var v = velocity

            frame.dRdtTimesRtAt(time)?.also {
                v -= it * r // Subtract instead of transposing.
            }

            frame.rotationAt(time).transposed.also {
                r = it * r
                v = it * r
            }

            return of(r, v, time, Int.MIN_VALUE, Int.MIN_VALUE, type)
        }

        /**
         * Builds a position object from a right ascension and declination.
         *
         * If a specific [distance] is not provided, It returns a
         * position vector a gigaparsec in length. This puts the position at a
         * great enough distance that it will stand at the same right ascension
         * and declination from any viewing position in the Solar System, to
         * very high precision (within a few hundredths of a microarcsecond).
         *
         * If an [epoch] is specified, the input coordinates are understood
         * to be in the dynamical system of that particular date. Otherwise,
         * they will be assumed to be ICRS (the modern replacement for J2000).
         */
        @JvmStatic
        fun equatorial(
            rightAscension: Angle, declination: Angle,
            distance: Distance = ONE_GIGAPARSEC,
            time: InstantOfTime = CurrentTime,
            epoch: InstantOfTime? = null,
            center: Number = Int.MIN_VALUE,
            target: Number = Int.MIN_VALUE,
        ): ICRF {
            val position = CartesianCoordinate.of(rightAscension, declination, distance)
            return of(if (epoch != null) epoch.m.transposed * position else position, Vector3D.EMPTY, time, center, target)
        }
    }
}
