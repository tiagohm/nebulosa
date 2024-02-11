package nebulosa.alignment.polar.point.three

import nebulosa.constants.PIOVERTWO
import nebulosa.erfa.eraAtco13
import nebulosa.math.Angle
import nebulosa.math.ONE_ATM
import nebulosa.math.Vector3D
import nebulosa.time.CurrentTime
import nebulosa.time.IERS
import nebulosa.time.InstantOfTime
import kotlin.math.cos
import kotlin.math.sin

internal data class Position(
    @JvmField val topocentric: Topocentric,
    @JvmField val vector: Vector3D,
) {

    companion object {

        operator fun invoke(
            rightAscension: Angle, declination: Angle,
            longitude: Angle, latitude: Angle,
            time: InstantOfTime = CurrentTime,
            refract: Boolean = false,
        ): Position {
            // SOFA.CelestialToTopocentric.
            val dut1 = IERS.delta(time)
            val (xp, yp) = IERS.pmAngles(time)
            val pressure = if (refract) ONE_ATM else 0.0
            // @formatter:off
            val (b) = eraAtco13(rightAscension, declination, 0.0, 0.0, 0.0, 0.0, time.utc.whole, time.utc.fraction, dut1, longitude, latitude, 0.0, xp, yp, pressure, 15.0, 0.5, 0.55)
            // @formatter:on
            val topocentric = Topocentric(b[0], PIOVERTWO - b[1], longitude, latitude)
            // val vector = CartesianCoordinate.of(-b[0], b[1], 1.0)
            val theta = -b[0]
            val phi = b[1]
            val x = cos(theta) * sin(phi)
            val y = sin(theta) * sin(phi)
            val z = cos(phi)
            return Position(topocentric, Vector3D(x, y, z))
        }

        operator fun invoke(
            vector: Vector3D, longitude: Angle, latitude: Angle,
        ): Position {
            val topocentric = if (vector.x == 0.0 && vector.y == 0.0) {
                Topocentric(0.0, PIOVERTWO, longitude, latitude)
            } else {
                Topocentric(-vector.longitude, PIOVERTWO - vector.latitude, longitude, latitude)
            }

            return Position(topocentric, vector)
        }
    }
}
