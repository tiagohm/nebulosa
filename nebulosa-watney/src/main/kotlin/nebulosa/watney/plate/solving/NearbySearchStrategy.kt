package nebulosa.watney.plate.solving

import nebulosa.erfa.SphericalCoordinate
import nebulosa.math.Angle
import nebulosa.math.deg
import nebulosa.math.toDegrees
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min

data class NearbySearchStrategy(
    private val centerRA: Angle,
    private val centerDEC: Angle,
    private val options: NearbySearchStrategyOptions = NearbySearchStrategyOptions.DEFAULT,
) : SearchStrategy {

    private val densityOffsets = IntArray(options.maxNegativeDensityOffset + options.maxPositiveDensityOffset + 1)

    init {
        require(options.maxFieldRadius > 0.0) { "maxFieldRadius must be > 0" }

        ((-options.maxNegativeDensityOffset)..options.maxPositiveDensityOffset)
            .forEachIndexed { n, i -> densityOffsets[n] = i }
    }

    override fun searchQueue(): List<SearchRun> {
        var radiiToTry = if (options.minFieldRadius <= 0.0) doubleArrayOf(options.maxFieldRadius.toDegrees)
        else if (options.maxFieldRadius == options.minFieldRadius) doubleArrayOf(options.minFieldRadius.toDegrees)
        else doubleArrayOf(options.maxFieldRadius.toDegrees, options.minFieldRadius.toDegrees)

        if (options.intermediateFieldRadiusSteps < 0 && radiiToTry.size > 1) {
            var currentRadius = options.maxFieldRadius.toDegrees
            val minRadius = options.minFieldRadius.toDegrees
            val radii = ArrayList<Double>(8)

            while (currentRadius > minRadius) {
                radii.add(currentRadius)
                currentRadius *= 0.5
            }

            radii.add(minRadius)
            radiiToTry = radii.toDoubleArray()
        } else if (options.intermediateFieldRadiusSteps > 0 && radiiToTry.size > 1) {
            val delta = (options.maxFieldRadius - options.minFieldRadius).toDegrees
            val stepSize = delta / (options.intermediateFieldRadiusSteps + 1)
            var currentRadius = options.maxFieldRadius.toDegrees
            val minRadius = options.minFieldRadius.toDegrees
            val radii = ArrayList<Double>(8)

            while (currentRadius > minRadius) {
                radii.add(currentRadius)
                currentRadius -= stepSize
            }

            radii.add(minRadius)
            radiiToTry = radii.toDoubleArray()
        }

        var n = 0
        val maxDEC = min(centerDEC.toDegrees + options.searchAreaRadius.toDegrees, 90.0)
        val minDEC = max(centerDEC.toDegrees - options.searchAreaRadius.toDegrees, -90.0)

        // All search runs, will be from largest radius to smallest, grouped by radius
        // and ordered in group by distance to our search center coordinate.
        val allRuns = ArrayList<SearchRun>()

        for (scopeFieldRadius in radiiToTry) {
            val runs = ArrayList<Pair<Double, SearchRun>>()
            runs.add(0.0 to SearchRun(scopeFieldRadius.deg, centerRA, centerDEC, densityOffsets))

            // Lazy approach: see what Dec range is within search radius,
            // populate it with semi-overlapping search circles and then measure distance
            // to each circle center and include it if it's in range.
            // Sort circles by distance.
            var dec = minDEC

            while (dec <= maxDEC) {
                val angularDistToCover = cos(dec.deg) * 360.0
                val numberOfSearchCircles = ceil(angularDistToCover / (2 * scopeFieldRadius)).toInt() + 1
                val raStep = 360.0 / numberOfSearchCircles
                val raOffset = n % 2 * 0.5 * raStep

                repeat(numberOfSearchCircles) {
                    val ra = raOffset + it * raStep
                    val distToOriginalSearchCenter = SphericalCoordinate.angularDistance(ra.deg, dec.deg, centerRA, centerDEC)

                    if (distToOriginalSearchCenter < options.searchAreaRadius) {
                        runs.add(distToOriginalSearchCenter to SearchRun(scopeFieldRadius.deg, ra.deg, dec.deg, densityOffsets))
                    }
                }

                dec += scopeFieldRadius
                n++
            }

            runs.sortBy { it.first }
            runs.forEach { allRuns.add(it.second) }
        }

        return allRuns
    }
}
