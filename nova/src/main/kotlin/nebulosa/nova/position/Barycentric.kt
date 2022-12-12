package nebulosa.nova.position

import nebulosa.constants.TAU
import nebulosa.coordinates.CartesianCoordinate
import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.math.Distance.Companion.au
import nebulosa.math.Vector3D
import nebulosa.math.pmod
import nebulosa.nova.astrometry.Body
import nebulosa.nova.astrometry.ICRF
import nebulosa.nova.astrometry.Observable
import nebulosa.nova.frame.Ecliptic
import nebulosa.nova.frame.Frame
import nebulosa.time.InstantOfTime
import nebulosa.time.TimeJD

/**
 * An |xyz| position measured from the Solar System barycenter.
 *
 * The position is measured from the gravitational center of
 * the Solar System whenever you ask a body for
 * its location at a particular [time].
 *
 * The [position] and [velocity] are vectors in the
 * Barycentric Celestial Reference System (BCRS), the modern
 * replacement for J2000 coordinates measured from the Solar System Barycenter.
 */
class Barycentric internal constructor(
    position: Vector3D,
    velocity: Vector3D,
    time: InstantOfTime,
    center: Number,
    target: Number,
) : ICRF(position, velocity, time, center, target) {

    /**
     * Computes the [Astrometric] position of a [body] from this location.
     */
    fun observe(body: Observable): Astrometric {
        val (p, v) = body.observe(this)
        val target = if (body is Body) body.target else if (body is Number) body else Int.MIN_VALUE
        return Astrometric(p, v, time, this.target, target, this)
    }

    /**
     * Returns the elongation at the [time].
     *
     * More precisely, returns the difference between the planetocentric apparent ecliptic
     * longitudes of [target] object and [center], viewed from this position,
     * constrained to the interval 0-1.
     *
     * For Moon as [target] viewed from Earth, it represents the Moon phase,
     * where 0 is New Moon and 0.5 is Full Moon.
     */
    fun elongation(target: Body, center: Body): Double {
        val mLon = observe(target).latLon(Ecliptic).theta
        val sLon = observe(center).latLon(Ecliptic).phi
        val angle = (mLon - sLon).value pmod TAU
        return angle / TAU
    }

    /**
     * Computes the phase angle of [target] body viewed from this position at the [time].
     */
    fun phaseAngle(target: Body, center: Body): Angle {
        val pe = -observe(target) // Rotate 180 degrees to point back at Earth.
        val ps = target.at<Barycentric>(TimeJD(time.tt - pe.lightTime)).observe(center)
        return pe.separationFrom(ps)
    }

    /**
     * Computes the illuminated fraction of [target] body viewed from this position at the [time].
     */
    fun illuminated(target: Body, center: Body) = 0.5 * (1.0 + phaseAngle(target, center).cos)

    /**
     * Generates an [Apparent] position from an [altitude] and [azimuth].
     */
    @Suppress("LocalVariableName")
    fun altAz(
        altitude: Angle,
        azimuth: Angle,
        distance: Distance = 0.1.au,
    ): Apparent {
        val frame = target as? Frame ?: throw IllegalArgumentException(
            "to compute an altazimuth position, you must observe from " +
                "a specific Earth location or from a position on another body loaded from a set " +
                "of planetary constants"
        )

        val R = frame.rotationAt(time)
        val p = R.transpose() * CartesianCoordinate.of(azimuth, altitude, distance)

        return Apparent(p, Vector3D.EMPTY, time, center, target, this)
    }
}
