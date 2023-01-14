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
    override val itrs: Vector3D,
    val model: Geoid,
) : ITRSPosition, Frame, Number() {

    private val rLat by lazy { Matrix3D.IDENTITY.rotateY(latitude).flipX() }

    private val rLatLon by lazy { rLat * Matrix3D.IDENTITY.rotateZ(-longitude) }

    override val center = 399

    override val target get() = this

    override val velocity by lazy { super.velocity }

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

    companion object {

        @JvmStatic val EARTH_ANGULAR_VELOCITY_VECTOR = Vector3D(0.0, 0.0, DAYSEC * ANGULAR_VELOCITY)
    }
}
