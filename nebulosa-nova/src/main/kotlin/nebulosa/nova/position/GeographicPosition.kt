package nebulosa.nova.position

import nebulosa.constants.ANGULAR_VELOCITY
import nebulosa.constants.DAYSEC
import nebulosa.constants.PIOVERTWO
import nebulosa.erfa.eraRefco
import nebulosa.erfa.eraSp00
import nebulosa.math.*
import nebulosa.nova.frame.Frame
import nebulosa.nova.frame.ITRS
import nebulosa.time.IERS
import nebulosa.time.InstantOfTime
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.tan

class GeographicPosition(
    override val longitude: Angle,
    override val latitude: Angle,
    override val elevation: Distance,
    itrs: Vector3D,
    val model: Geoid,
) : ITRSPosition(itrs), GeographicCoordinate, Frame {

    private val rLat by lazy { Matrix3D.rotY(-latitude).flipX() }
    private val rLatLon by lazy { rLat * Matrix3D.rotZ(longitude) }

    override val center = 399

    override val target = this

    /**
     * Returns this position’s Local Sidereal Time at the [time].
     */
    fun lstAt(time: InstantOfTime, tio: Boolean = false): Angle {
        return if (tio) {
            val (sprime, xp, yp) = IERS.pmAngles(time)
            val r = Matrix3D.rotZ(longitude).rotateX(-yp).rotateY(-xp).rotateZ(time.gast + sprime)
            atan2(r[0, 1], r[0, 0])
        } else {
            val sprime = eraSp00(time.tt.whole, time.tt.fraction)
            (time.gast + longitude + sprime).normalized
        }
    }

    /**
     * Predicts how the atmosphere will refract a position.
     *
     * Given a body that is standing [altitude] above the
     * true horizon, return an [Angle] predicting its apparent
     * altitude given the supplied [temperature] and [pressure].
     */
    fun refract(
        altitude: Angle,
        temperature: Temperature = 15.0.celsius,
        pressure: Pressure = elevation.pressure(temperature),
        relativeHumidity: Double = 0.0,
        waveLength: Double = 0.54,
    ) = computeRefractedAltitude(altitude, temperature, pressure, relativeHumidity, waveLength)

    /**
     * Computes rotation from GCRS to this location’s altazimuth system.
     */
    override fun rotationAt(time: InstantOfTime) = rLatLon * ITRS.rotationAt(time)

    override fun dRdtTimesRtAt(time: InstantOfTime): Matrix3D {
        // TODO: taking the derivative of the instantaneous angular velocity would provide a more accurate transform.
        val (x, y, z) = rLat * EARTH_ANGULAR_VELOCITY_VECTOR
        return Matrix3D(0.0, -z, y, z, 0.0, -x, -y, x, 0.0)
    }

    override fun toByte() = center.toByte()

    @Suppress("OVERRIDE_DEPRECATION")
    override fun toChar() = center.toChar()

    override fun toDouble() = center.toDouble()

    override fun toFloat() = center.toFloat()

    override fun toInt() = center

    override fun toLong() = center.toLong()

    override fun toShort() = center.toShort()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GeographicPosition

        if (longitude != other.longitude) return false
        if (latitude != other.latitude) return false
        if (elevation != other.elevation) return false
        return model == other.model
    }

    override fun hashCode(): Int {
        var result = longitude.hashCode()
        result = 31 * result + latitude.hashCode()
        result = 31 * result + elevation.hashCode()
        result = 31 * result + model.hashCode()
        return result
    }

    override fun toString() = "GeographicPosition(longitude=${longitude.toDegrees}, " +
            "latitude=${latitude.toDegrees}, elevation=${elevation.toMeters}, model=$model)"

    companion object {

        @JvmStatic val EARTH_ANGULAR_VELOCITY_VECTOR = Vector3D(z = DAYSEC * ANGULAR_VELOCITY)

        @JvmStatic
        fun computeRefractedAltitude(
            altitude: Angle,
            temperature: Temperature = 15.0.celsius,
            pressure: Pressure = ONE_ATM,
            relativeHumidity: Double = 0.5,
            waveLength: Double = 0.55,
            iterationIncrement: Angle = 1.0.arcsec,
        ): Double {
            if (altitude < 0.0) {
                return altitude
            }

            val z = PIOVERTWO - altitude

            val (refa, refb) = eraRefco(pressure, temperature, relativeHumidity, waveLength)

            var roller = iterationIncrement
            var iterations = 0

            while (iterations++ < 10) {
                val refractedZenithDistanceRadian = z - roller

                // dZ = A tan Z + B tan^3 Z.
                val dZ2 = refa * tan(refractedZenithDistanceRadian) + refb * tan(refractedZenithDistanceRadian).cubic

                if (dZ2.isNaN()) {
                    return altitude
                }

                val originalZenithDistanceRadian = refractedZenithDistanceRadian + dZ2

                if (abs(originalZenithDistanceRadian - z) < iterationIncrement) {
                    return PIOVERTWO - originalZenithDistanceRadian
                }

                roller += iterationIncrement
            }

            return altitude
        }
    }
}
