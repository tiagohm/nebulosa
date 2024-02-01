package nebulosa.alignment.polar.point.three

import nebulosa.constants.PIOVERTWO
import nebulosa.erfa.CartesianCoordinate
import nebulosa.erfa.eraAtco13
import nebulosa.erfa.eraAtic13
import nebulosa.erfa.eraEe06a
import nebulosa.math.Angle
import nebulosa.math.Vector3D
import nebulosa.math.normalized
import nebulosa.time.IERS
import nebulosa.time.UTC
import kotlin.math.acos

internal class Position {

    @JvmField val topocentric: Topocentric
    @JvmField val vector: Vector3D

    constructor(
        rightAscension: Angle, declination: Angle,
        longitude: Angle, latitude: Angle,
    ) {
        val time = UTC.now()
        val ee = eraEe06a(time.tt.whole, time.tt.fraction)
        val (ri, di) = eraAtic13((rightAscension + ee).normalized, declination, time.tdb.whole, time.tdb.fraction)
        val dut1 = IERS.delta(time)
        val (xp, yp) = IERS.pmAngles(time)
        val (b) = eraAtco13(ri, di, 0.0, 0.0, 0.0, 0.0, time.whole, time.fraction, dut1, longitude, latitude, 0.0, xp, yp, 1013.25, 15.0, 0.0, 0.55)
        val az = b[0] // aob
        val alt = PIOVERTWO - b[1] // zob
        topocentric = Topocentric(az, alt, longitude, latitude, time)
        vector = CartesianCoordinate.of(-topocentric.azimuth, PIOVERTWO - topocentric.altitude, 1.0)
    }

    constructor(topocentric: Topocentric, vector: Vector3D) {
        this.topocentric = topocentric
        this.vector = vector
    }

    constructor(vector: Vector3D, longitude: Angle, latitude: Angle) {
        this.topocentric = if (vector[0] == 0.0 && vector[1] == 0.0) {
            Topocentric(0.0, PIOVERTWO, longitude, latitude, UTC.now())
        } else {
            Topocentric(-vector.longitude, PIOVERTWO - acos(vector[2]), longitude, latitude, UTC.now())
        }

        this.vector = vector
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Position) return false

        if (topocentric != other.topocentric) return false
        if (vector != other.vector) return false

        return true
    }

    override fun hashCode(): Int {
        var result = topocentric.hashCode()
        result = 31 * result + vector.hashCode()
        return result
    }

    override fun toString() = "Position(topocentric=$topocentric, vector=$vector)"
}
