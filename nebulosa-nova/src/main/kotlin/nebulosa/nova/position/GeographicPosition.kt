package nebulosa.nova.position

import nebulosa.constants.ANGULAR_VELOCITY
import nebulosa.constants.DAYSEC
import nebulosa.erfa.eraSp00
import nebulosa.math.*
import nebulosa.math.Pressure.Companion.pressure
import nebulosa.nova.frame.Frame
import nebulosa.nova.frame.ITRS
import nebulosa.time.InstantOfTime

class GeographicPosition(
    val longitude: Angle,
    val latitude: Angle,
    val elevation: Distance,
    itrs: Vector3D,
    val model: Geoid,
) : ITRSPosition(itrs), Frame {

    private val rLat by lazy { Matrix3D.rotateY(-latitude).flipX() }
    private val rLatLon by lazy { rLat * Matrix3D.rotateZ(longitude) }

    override val center = 399

    override val target
        get() = this

    /**
     * Returns this position’s Local Sidereal Time at the [time].
     */
    fun lstAt(time: InstantOfTime): Angle {
        val sprime = eraSp00(time.tt.whole, time.tt.fraction)
        return (time.gast + longitude + sprime).normalized
    }

    /**
     * Predicts how the atmosphere will refract a position.
     *
     * Given a body that is standing [altitude] above the
     * true horizon, return an [Angle] predicting its apparent
     * altitude given the supplied [temperature] and [pressure].
     */
//    fun refract(
//        altitude: Angle,
//        temperature: Temperature = 10.0.celsius,
//        pressure: Pressure = elevation.pressure(temperature),
//        relativeHumidity: Double = 0.5,
//    ): Angle {
//        eraRefco(pressure.value, temperature.value, relativeHumidity, 0.4) // TODO default wavelength
//    }

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
        if (model != other.model) return false

        return true
    }

    override fun hashCode(): Int {
        var result = longitude.hashCode()
        result = 31 * result + latitude.hashCode()
        result = 31 * result + elevation.hashCode()
        result = 31 * result + model.hashCode()
        return result
    }

    override fun toString(): String {
        return "GeographicPosition(longitude=${longitude.degrees}, latitude=${latitude.degrees}, elevation=${elevation.meters}, model=$model)"
    }

    companion object {

        @JvmStatic val EARTH_ANGULAR_VELOCITY_VECTOR = Vector3D(0.0, 0.0, DAYSEC * ANGULAR_VELOCITY)
    }
}
