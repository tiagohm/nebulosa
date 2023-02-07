package nebulosa.nova.astrometry

import nebulosa.constants.GM_SUN_PITJEVA_2005
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Distance
import nebulosa.math.Distance.Companion.au
import nebulosa.math.Matrix3D
import nebulosa.nova.frame.EclipticJ2000
import nebulosa.time.InstantOfTime
import nebulosa.time.TT
import nebulosa.time.TimeYMDHMS

class Asteroid(
    semiMajorAxis: Distance,
    eccentricity: Double,
    inclination: Angle,
    longitudeOfAscendingNode: Angle,
    argumentOfPerihelion: Angle,
    meanAnomaly: Angle,
    epoch: InstantOfTime,
    target: Int = Int.MIN_VALUE,
    mu: Double = GM_SUN_PITJEVA_2005,
    rotation: Matrix3D? = EclipticJ2000.transposed.matrix,
) : Body by KeplerOrbit.meanAnomaly(
    semiMajorAxis * (1.0 - eccentricity * eccentricity),
    eccentricity,
    inclination, longitudeOfAscendingNode, argumentOfPerihelion, meanAnomaly,
    epoch, mu, 10, target,
    rotation,
) {

    companion object {

        @JvmStatic private val MPC_COLUMNS = mapOf(
            // "designation_packed" to (0..6),
            // "magnitude_H" to (8..12),
            // "magnitude_G" to (14..18),
            "epoch_packed" to (20..24),
            "mean_anomaly" to (26..34), // degrees
            "argument_of_perihelion" to (37..45), // degrees
            "longitude_of_ascending_node" to (48..56), // degrees
            "inclination" to (59..67), // degrees
            "eccentricity" to (70..78),
            // "mean_daily_motion" to (80..90), // degrees
            "semimajor_axis" to (92..102), // AU
            // "uncertainty" to (105..105),
            // "reference" to (107..115),
            // "observations" to (117..121),
            // "oppositions" to (123..125),
            // "observation_period" to (127..135),
            // "rms_residual" to (137..140), // arcseconds
            // "coarse_perturbers" to (142..144),
            // "precise_perturbers" to (146..148),
            // "computer_name" to (150..159),
            // "hex_flags" to (161..164),
            // "designation" to (166..193),
            // "last_observation_date" to (194..201),
        )

        @JvmStatic
        fun parse(line: String): Asteroid {
            val parsedLine = MPC_COLUMNS.mapValues { line.substring(it.value) }

            val a = parsedLine["semimajor_axis"]!!.toDouble().au
            val e = parsedLine["eccentricity"]!!.toDouble()

            fun n(c: Char) = c.code - if (c.isDigit()) 48 else 55

            val epochPacked = parsedLine["epoch_packed"]!!
            val year = 100 * n(epochPacked[0]) + epochPacked.substring(1..2).toInt()
            val epoch = TT(TimeYMDHMS(year, n(epochPacked[3]), n(epochPacked[4])))

            return Asteroid(
                a, e,
                parsedLine["inclination"]!!.toDouble().deg,
                parsedLine["longitude_of_ascending_node"]!!.toDouble().deg,
                parsedLine["argument_of_perihelion"]!!.toDouble().deg,
                parsedLine["mean_anomaly"]!!.toDouble().deg,
                epoch,
            )
        }
    }
}
