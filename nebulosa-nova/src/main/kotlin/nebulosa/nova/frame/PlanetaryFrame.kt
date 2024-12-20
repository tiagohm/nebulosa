package nebulosa.nova.frame

import nebulosa.constants.DAYSEC
import nebulosa.math.Angle
import nebulosa.math.Distance
import nebulosa.math.Matrix3D
import nebulosa.math.rad
import nebulosa.nasa.pck.PckSegment
import nebulosa.nova.position.PlanetograhicPosition
import nebulosa.time.InstantOfTime
import kotlin.math.cos
import kotlin.math.sin

/**
 * Planetary constants frame, for building rotation matrices.
 */
data class PlanetaryFrame(
    val center: Number,
    private val matrix: Matrix3D?,
    private val segment: PckSegment,
) : Frame {

    data class RotationAndRate(
        @JvmField val rotation: Matrix3D,
        @JvmField val rate: Matrix3D,
    )

    /**
     * Creates the [PlanetaryFrame] given the |xyz| rotating [angles].
     */
    constructor(
        center: Number,
        angles: DoubleArray,
        segment: PckSegment,
    ) : this(center, Matrix3D.rotX(angles[0]).rotateY(angles[1]).rotateZ(angles[2]), segment)

    /**
     * Returns the rotation matrix for this frame at the [time].
     */
    override fun rotationAt(time: InstantOfTime): Matrix3D {
        val (ra, dec, w) = segment.compute(time.tdb, derivative = false).position
        val r = Matrix3D.rotZ((-w).rad).rotateX((-dec).rad).rotateZ((-ra).rad)
        return if (matrix != null) matrix * r else r
    }

    /**
     * Returns rotation and rate matrices for this frame at the [time].
     *
     * The rate matrix returned is in units of angular motion per day.
     */
    fun rotationAndRateAt(time: InstantOfTime): RotationAndRate {
        val (c, rates) = segment.compute(time, true)
        val (ra, dec, w) = c
        val (radot, decdot, wdot) = rates

        val r = Matrix3D.rotZ((-w).rad)
            .rotateX((-dec).rad)
            .rotateZ((-ra).rad)

        val ca = cos(w)
        val sa = sin(w)
        val u = cos(dec)
        val v = -sin(dec)

        val domega0 = wdot + u * radot
        val domega1 = ca * decdot - sa * v * radot
        val domega2 = sa * decdot + ca * v * radot

        val drdtrt = Matrix3D(
            0.0, domega0, domega2,
            -domega0, 0.0, domega1,
            -domega2, -domega1, 0.0,
        )

        val dRdt = drdtrt * r

        return if (matrix != null) {
            RotationAndRate(matrix * r, matrix * dRdt * DAYSEC)
        } else {
            RotationAndRate(r, dRdt * DAYSEC)
        }
    }

    fun latLon(
        latitude: Angle, longitude: Angle, radius: Distance,
        elevation: Distance = 0.0,
    ) = PlanetograhicPosition(this, latitude, longitude, radius + elevation)
}
