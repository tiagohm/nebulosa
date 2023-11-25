package nebulosa.watney.plate.solving

import nebulosa.imaging.Image
import nebulosa.math.Angle
import nebulosa.math.toDegrees
import nebulosa.plate.solving.PlateSolution
import nebulosa.plate.solving.PlateSolver
import nebulosa.star.detection.ImageStar
import nebulosa.watney.plate.solving.quad.ImageStarQuad
import nebulosa.watney.plate.solving.quad.QuadDatabase
import nebulosa.watney.plate.solving.quad.StarQuad
import nebulosa.watney.star.detection.WatneyStarDetector
import java.time.Duration
import java.util.*
import kotlin.math.*

class WatneyPlateSolver(
    private val maxStars: Int = -1,
    private val sampling: Int = 4,
) : PlateSolver<Image> {

    private val starDetector = WatneyStarDetector()

    override fun solve(
        input: Image, blind: Boolean,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int, timeout: Duration?,
    ): PlateSolution {
        val stars = starDetector.detect(input)

        if (stars.isEmpty()) return PlateSolution.NO_SOLUTION

        val maxStars = if (maxStars > 0) maxStars else min(stars.size / 3, 1000)
        val (imageStarQuads, countInFirstPass) = formImageStarQuads(stars)

        val strategy = if (radius > 0.0) NearbySearchStrategy(centerRA, centerDEC)
        else BlindSearchStrategy()

        val searchQueue = strategy.searchQueue()

        val groupedSearchQueue = TreeMap<Double, MutableList<SearchRun>>(Collections.reverseOrder())
        val runsByRadius = searchQueue.groupByTo(groupedSearchQueue) { it.radius.toDegrees }.toList()

        repeat(sampling) {
            var continueSearching = true

            repeat(groupedSearchQueue.size) { rg ->
                for (searchRun in runsByRadius[rg].second) {

                }
            }
        }

        return PlateSolution.NO_SOLUTION
    }

    companion object {

        private const val MIN_MATCHES = 5

        @JvmStatic
        private fun formImageStarQuads(starsFound: List<ImageStar>): Pair<List<ImageStarQuad>, Int> {
            val quads = ArrayList<ImageStarQuad>()
            var countInFirstPass = 0

            // Do a few passes. Experimental.
            repeat(4) { p ->
                val starsToUse = (starsFound.size * (1 - p * 0.05)).toInt()
                val stars = starsFound.subList(0, starsToUse)

                val starDistances = Array(stars.size) { FloatArray(stars.size) }

                for (i in stars.indices) {
                    for (j in i + 1 until stars.size) {
                        val dist = stars[i].distance(stars[j])
                        starDistances[i][j] = dist
                        starDistances[j][i] = dist
                    }
                }

                for (i in starDistances.indices) {
                    val distancesToOthers = starDistances[i]

                    // Get 3 nearest.
                    val nearestIndices = intArrayOf(-1, -1, -1)
                    val nearestDistances = FloatArray(nearestIndices.size)

                    for (n in 0..2) {
                        var index = 0
                        var dist = Float.MAX_VALUE

                        for (j in distancesToOthers.indices) {
                            if (distancesToOthers[j] < dist && distancesToOthers[j] > 0 && j != nearestIndices[0] && j != nearestIndices[1]) {
                                dist = distancesToOthers[j]
                                index = j
                            }
                        }

                        nearestIndices[n] = index
                        nearestDistances[n] = dist
                    }

                    val (d0a, d0b, d0c) = nearestDistances
                    val (starIndexA, starIndexB, starIndexC) = nearestIndices

                    val dab = starDistances[starIndexA][starIndexB]
                    val dac = starDistances[starIndexA][starIndexC]
                    val dbc = starDistances[starIndexB][starIndexC]

                    val sixDistances = arrayOf(d0a, d0b, d0c, dab, dac, dbc)
                    sixDistances.sort()
                    val largestDistance = sixDistances.max()

                    val ratios = FloatArray(sixDistances.size - 1) { sixDistances[it] / largestDistance }
                    val quadStars = listOf(stars[i], stars[starIndexA], stars[starIndexB], stars[starIndexC])

                    val quad = ImageStarQuad(ratios, largestDistance, quadStars)
                    quads.add(quad)
                }

                if (p == 0) {
                    countInFirstPass = quads.distinctBy { StarQuad.StarBasedEqualityKey(it) }.size
                }
            }

            val quadsArray = quads.distinctBy { ImageStarQuad.StarBasedEqualityKey(it) }
            return quadsArray to countInFirstPass
        }
    }

    @JvmStatic
    private fun trySolve(
        image: Image, searchRun: SearchRun, countInFirstPass: Int,
        quadDatabase: QuadDatabase, numSubSets: Int, subSetIndex: Int,
        imageStarQuads: List<ImageStarQuad>,
    ) {
        // Quads per degree.
        val searchFieldSize = searchRun.radius.toDegrees * 2
        val a = atan(image.height.toDouble() / image.width)
        val s1 = searchFieldSize * sin(a)
        val s2 = searchFieldSize * cos(a)
        val area = s1 * s2
        val quadsPerSqDeg = countInFirstPass / area

        val imageDiameterInPixels = hypot(image.width.toDouble(), image.height.toDouble())
        var pixelAngularSearchFieldSizeRatio = imageDiameterInPixels / searchFieldSize

        var databaseQuads = quadDatabase.quads(
            searchRun.centerRA, searchRun.centerDEC, searchRun.radius, quadsPerSqDeg.toInt(),
            searchRun.densityOffsets, numSubSets, subSetIndex, imageStarQuads
        )

        if (databaseQuads.size < MIN_MATCHES) {
            return taskResult
        }

        // Found enough matches; a likely hit. If this was a sampled run, spend the time to retrieve the full quad set without sampling
        // as we're going to try for a solution.
        if (numSubSets > 1) {
            databaseQuads = quadDatabase.quads(
                searchRun.centerRA, searchRun.centerDEC, searchRun.radius, quadsPerSqDeg.toInt(), searchRun.densityOffsets, 1, 0,
                imageStarQuads
            )
        }

        val matchingQuads = findMatches(pixelAngularSearchFieldSizeRatio, imageStarQuads, databaseQuads, 0.011, MIN_MATCHES)

        if (matchingQuads.size >= MIN_MATCHES) {
            val preliminarySolution = calculateSolution(image, matchingQuads, searchRun.centerRA, searchRun.centerDEC)

            if (!isValidSolution(preliminarySolution)) {
                return taskResult
            }

            // Probably off really badly, so don't accept it.
            if (preliminarySolution.radius > 2 * searchRun.radius) {
                return taskResult
            }

            pixelAngularSearchFieldSizeRatio = imageDiameterInPixels / preliminarySolution.radius * 2

            val improvedSolution = performAccuracyImprovementForSolution(
                image, preliminarySolution,
                pixelAngularSearchFieldSizeRatio, quadDatabase, imageStarQuads, quadsPerSqDeg, MIN_MATCHES, searchRun.densityOffsets
            )

            if (!isValidSolution(improvedSolution.solution)) {
                return taskResult
            }
        }
    }
}
