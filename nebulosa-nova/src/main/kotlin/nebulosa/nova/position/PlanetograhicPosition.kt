package nebulosa.nova.position

import nebulosa.constants.TAU
import nebulosa.erfa.PositionAndVelocity
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Distance
import nebulosa.math.Matrix3D
import nebulosa.math.Vector3D
import nebulosa.nova.astrometry.Body
import nebulosa.nova.frame.Frame
import nebulosa.nova.frame.PlanetaryFrame
import nebulosa.time.InstantOfTime

data class PlanetograhicPosition(
    val frame: PlanetaryFrame,
    val position: Vector3D,
    val latitude: Angle,
    val longitude: Angle,
) : Body, Frame, Number() {

    override val center = frame.center

    override val target = this

    constructor(
        frame: PlanetaryFrame,
        latitude: Angle, longitude: Angle, distance: Distance,
    ) : this(
        frame,
        Matrix3D.rotateZ(longitude).rotateY(-latitude) * Vector3D(distance.value),
        latitude, longitude,
    )

    override fun compute(time: InstantOfTime): PositionAndVelocity {
        // Since position has zero velocity in this reference
        // frame, velocity includes a "dRdt" term but not an "r" term.
        val (r, dRdt) = frame.rotationAndRateAt(time)
        return PositionAndVelocity(r.transposed * position, dRdt.transposed * position)
    }

    /**
     * Computes the altazimuth rotation matrix for this location’s sky.
     */
    override fun rotationAt(time: InstantOfTime): Matrix3D {
        // TODO: Figure out how to produce this rotation directly
        // from position, to support situations where we were not
        // given a latitude and longitude.  If that is not feasible,
        // then at least cache the product of these first two matrices.
        val m = Matrix3D.rotateY((TAU / 4.0 - latitude.value).rad)
            .rotateZ((TAU / 2.0 - longitude.value).rad) *
                frame.rotationAt(time)

        // Turn clockwise into counterclockwise.
        // Flip the sign of y so that azimuth reads north-east rather than the other direction.
        return Matrix3D(
            m.a11, m.a12, m.a13,
            -m.a21, -m.a22, -m.a23,
            m.a31, m.a32, m.a33,
        )
    }

    override fun toByte() = center.toByte()

    @Suppress("OVERRIDE_DEPRECATION")
    override fun toChar() = center.toInt().toChar()

    override fun toDouble() = center.toDouble()

    override fun toFloat() = center.toFloat()

    override fun toInt() = center.toInt()

    override fun toLong() = center.toLong()

    override fun toShort() = center.toShort()
}
