package nebulosa.nova.astrometry

import nebulosa.constants.MU_KM3_S2_TO_AU3_D2
import nebulosa.constants.TAU
import nebulosa.math.*
import nebulosa.nova.frame.InertialFrame
import nebulosa.nova.position.ICRF
import nebulosa.time.InstantOfTime
import nebulosa.time.TDB
import kotlin.math.*

/**
 * Osculating orbital elements.
 */
data class OsculatingElements(
    val position: Vector3D,
    val velocity: Vector3D,
    val time: InstantOfTime,
    val mu: Double,
) {

    /**
     * Creates a [OsculatingElements] from [position].
     */
    constructor(position: ICRF) : this(
        position.position,
        position.velocity,
        position.time,
        ((GM_TABLE[position.center.toInt()] ?: 0.0) + (GM_TABLE[position.target.toInt()] ?: 0.0)) * MU_KM3_S2_TO_AU3_D2,
    )

    /**
     * Creates a [OsculatingElements] from [position] and reference [frame].
     */
    constructor(
        position: ICRF,
        frame: Matrix3D,
    ) : this(
        frame * position.position,
        frame * position.velocity,
        position.time,
        ((GM_TABLE[position.center.toInt()] ?: 0.0) + (GM_TABLE[position.target.toInt()] ?: 0.0)) * MU_KM3_S2_TO_AU3_D2,
    )

    /**
     * Creates a [OsculatingElements] from [position] and reference [frame].
     */
    constructor(
        position: ICRF,
        frame: InertialFrame,
    ) : this(position, frame.matrix)

    private val h = position.cross(velocity)
    private val e = eccentricityVector(position, velocity, mu)
    private val n = nodeVector(h)

    val apoapsis
        get() = apoapsisDistance(semiLatusRectum, eccentricity)

    val argumentOfLatitude
        get() = (argumentOfPeriapsis + trueAnomaly).normalized

    val argumentOfPeriapsis
        get() = argumentOfPeriapsis(n, e, position, velocity)

    val eccentricAnomaly
        get() = eccentricAnomaly(trueAnomaly, eccentricity)

    val eccentricity
        get() = e.length

    val inclination
        get() = inclination(h)

    val longitudeOfAscendingNode
        get() = longitudeOfAscendingNode(inclination, h)

    val longitudeOfPeriapsis
        get() = (longitudeOfAscendingNode + argumentOfPeriapsis).normalized

    val meanAnomaly
        get() = meanAnomaly(eccentricAnomaly, eccentricity)

    val meanLongitude
        get() = (longitudeOfAscendingNode + argumentOfPeriapsis + meanAnomaly).normalized

    val meanMotionPerDay
        get() = meanMotion(semiMajorAxis, mu).rad

    val periapsis
        get() = periapsisDistance(semiLatusRectum, eccentricity)

    val periodInDays
        get() = period(semiMajorAxis, mu)

    val semiLatusRectum
        get() = semiLatusRectum(h, mu).au

    val semiMajorAxis
        get() = semiMajorAxis(semiLatusRectum, eccentricity)

    val semiMinorAxis
        get() = semiMinorAxis(semiLatusRectum, eccentricity)

    val trueAnomaly
        get() = trueAnomaly(n, e, position, velocity)

    val trueLongitude
        get() = (longitudeOfAscendingNode + argumentOfPeriapsis + trueAnomaly).normalized

    val timeOfPeriapsis: InstantOfTime
        get() {
            val m = meanAnomaly(eccentricAnomaly, eccentricity, false)
            val tp = timeSincePeriapsis(m, meanMotionPerDay, trueAnomaly, semiLatusRectum, mu)
            return TDB(time.whole - tp, time.fraction)
        }

    companion object {

        @JvmStatic private val INFINITY_DISTANCE = Double.POSITIVE_INFINITY.au

        @JvmStatic
        private fun eccentricityVector(position: Vector3D, velocity: Vector3D, mu: Double) =
            ((position * (velocity.length.squared - mu / position.length)) - (velocity * position.dot(velocity))) / mu

        @JvmStatic
        private fun nodeVector(h: Vector3D) = Vector3D(-h[1], h[0], 0.0).normalized

        @JvmStatic
        private fun meanMotion(a: Distance, mu: Double) = sqrt(mu / abs(a).cubic)

        @JvmStatic
        private fun inclination(h: Vector3D) = h.angle(Vector3D.Z)

        @JvmStatic
        private fun longitudeOfAscendingNode(i: Angle, h: Vector3D) = if (i == 0.0) i else atan2(h[0], -h[1]).rad.normalized

        @JvmStatic
        private fun semiLatusRectum(h: Vector3D, mu: Double) = h.length.squared / mu

        @JvmStatic
        private fun semiMajorAxis(p: Distance, e: Double) = if (e == 1.0) INFINITY_DISTANCE else p / (1.0 - e * e)

        @JvmStatic
        private fun semiMinorAxis(p: Distance, e: Double) = when {
            e < 1.0 -> p / sqrt(1 - e * e)
            e > 1.0 -> p * sqrt(e * e - 1) / (1 - e * e)
            else -> INFINITY_DISTANCE
        }

        @JvmStatic
        private fun period(a: Distance, mu: Double) = TAU * sqrt(a.cubic / mu)

        @JvmStatic
        private fun periapsisDistance(p: Distance, e: Double) = if (e == 1.0) p / 2.0 else p * (1.0 - e) / (1.0 - e * e)

        @JvmStatic
        private fun apoapsisDistance(p: Distance, e: Double) = if (e >= 1.0) INFINITY_DISTANCE else p * (1.0 + e) / (1.0 - e * e)

        @JvmStatic
        private fun argumentOfPeriapsis(
            n: Vector3D,
            e: Vector3D,
            position: Vector3D,
            velocity: Vector3D,
        ): Angle {
            // Circular.
            val v = if (e.length < 1E-15) {
                0.0
            }
            // Equatorial and not circular.
            else if (n.length < 1E-15) {
                val angle = e.longitude
                if (position.cross(velocity)[2] >= 0.0) angle else (-angle).normalized
            }
            // Not circular and not equatorial.
            else {
                val angle = n.angle(e)
                if (e[2] >= 0.0) angle else (-angle).normalized
            }

            return if (e.length <= 1.0) v else v.normalized
        }

        @JvmStatic
        private fun trueAnomaly(
            n: Vector3D,
            e: Vector3D,
            position: Vector3D,
            velocity: Vector3D,
        ): Angle {
            // Not Circular.
            return if (e.length > 1E-15) {
                val angle = e.angle(position)
                if (position.dot(velocity) > 0.0) angle else (-angle).normalized
            }
            // Equatorial and circular.
            else if (n.length < 1E-15) {
                val angle = acos(position[0] / position.length).rad
                if (velocity[0] < 0) angle else (-angle).normalized
            }
            // Circular and not equatorial.
            else {
                val angle = n.angle(position)
                if (position[2] >= 0.0) angle else (-angle).normalized
            }
        }

        @JvmStatic
        private fun eccentricAnomaly(
            v: Angle,
            e: Double,
        ): Double {
            return when {
                e < 1.0 -> 2.0 * atan(sqrt((1.0 - e) / (1.0 + e)) * tan(v / 2.0))
                e > 1.0 -> 2.0 * atanh(tan(v / 2.0) / sqrt((e + 1.0) / (e - 1.0)))
                else -> 0.0
            }
        }

        @JvmStatic
        @Suppress("LocalVariableName")
        private fun meanAnomaly(
            E: Double,
            e: Double,
            shift: Boolean = true,
        ): Angle {
            return when {
                e < 1.0 -> (E - e * sin(E)).rad.normalized
                e > 1.0 -> (e * sinh(E) - E).rad.let { if (shift) it.normalized else it }
                else -> 0.0
            }
        }

        /**
         * Computes the time of periapsis in days.
         */
        @JvmStatic
        @Suppress("LocalVariableName")
        private fun timeSincePeriapsis(
            M: Double,
            n: Double,
            v: Double,
            p: Double,
            mu: Double,
        ): Double {
            // Non-parabolic.
            return if (n > 1E-19) {
                M / n
            } else {
                val D = tan(v / 2)
                sqrt(2 * (p / 2).cubic / mu) * (D + D.cubic / 3)
            }
        }

        // All values are in km³/s²
        // Source: ftp://ssd.jpl.nasa.gov/pub/xfr/gm_Horizons.pck
        @Suppress("FloatingPointLiteralPrecision")
        @JvmStatic private val GM_TABLE = mapOf(
            1 to 2.2031780000000021E+04,
            2 to 3.2485859200000006E+05,
            3 to 4.0350323550225981E+05,
            4 to 4.2828375214000022E+04,
            5 to 1.2671276480000021E+08,
            6 to 3.7940585200000003E+07,
            7 to 5.7945486000000080E+06,
            8 to 6.8365271005800236E+06,
            9 to 9.7700000000000068E+02,
            10 to 1.3271244004193938E+11,

            199 to 2.2031780000000021E+04,
            299 to 3.2485859200000006E+05,
            399 to 3.9860043543609598E+05,
            499 to 4.282837362069909E+04,
            599 to 1.266865349115908E+08,
            699 to 3.793120723493890E+07,
            799 to 5.793951322279009E+06,
            899 to 6.835099502439672E+06,
            999 to 8.693390780381926E+02,

            301 to 4.9028000661637961E+03,

            401 to 7.087546066894452E-04,
            402 to 9.615569648120313E-05,

            501 to 5.959924010272514E+03,
            502 to 3.202739815114734E+03,
            503 to 9.887819980080976E+03,
            504 to 7.179304867611079E+03,
            505 to 1.487604677404272E-01,
            506 to 1.380080696078966E-01,

            601 to 2.503458199931431E+00,
            602 to 7.211185066509890E+00,
            603 to 4.120856508658532E+01,
            604 to 7.311574218947423E+01,
            605 to 1.539419035933117E+02,
            606 to 8.978137030983542E+03,
            607 to 3.712085754472412E-01,
            608 to 1.205095752388872E+02,
            609 to 5.532371285376407E-01,
            610 to 1.265765099012197E-01,
            611 to 3.512333288208074E-02,
            612 to 3.424829447502984E-04,
            615 to 3.718871247516475E-04,
            616 to 1.075208001007610E-02,
            617 to 9.290325122028795E-03,

            701 to 8.346344431770477E+01,
            702 to 8.509338094489388E+01,
            703 to 2.269437003741248E+02,
            704 to 2.053234302535623E+02,
            705 to 4.319516899232100E+00,

            801 to 1.427598140725034E+03,

            901 to 1.062509269522026E+02,
            902 to 2.150552267969335E-03,
            903 to 3.663917107480563E-03,
            904 to 4.540734312735987E-04,
            905 to 2.000000000000000E-20,

            2000001 to 6.2809393000000000E+01,
            2000002 to 1.3923011000000001E+01,
            2000003 to 1.6224149999999999E+00,
            2000004 to 1.7288008999999999E+01,
            2000010 to 5.5423920000000004E+00,
            2000015 to 2.0981550000000002E+00,
            2000016 to 1.5300480000000001E+00,
            2000031 to 2.8448720000000001E+00,
            2000048 to 1.1351590000000000E+00,
            2000052 to 1.1108039999999999E+00,
            2000065 to 1.4264810000000001E+00,
            2000087 to 9.8635300000000004E-01,
            2000088 to 1.1557990000000000E+00,
            2000433 to 4.463E-4,
            2000451 to 1.0295259999999999E+00,
            2000511 to 2.3312860000000000E+00,
            2000704 to 2.3573170000000001E+00,
        )
    }
}
