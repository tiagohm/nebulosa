package nebulosa.watney.plate.solving

import nebulosa.erfa.SphericalCoordinate
import nebulosa.imaging.Image
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.toDegrees
import nebulosa.plate.solving.PlateSolution
import nebulosa.plate.solving.PlateSolution.Parity
import nebulosa.plate.solving.PlateSolver
import nebulosa.star.detection.ImageStar
import nebulosa.watney.plate.solving.math.equatorialToStandardCoordinates
import nebulosa.watney.plate.solving.math.lerp
import nebulosa.watney.plate.solving.math.solveLeastSquares
import nebulosa.watney.plate.solving.math.standardToEquatorialCoordinates
import nebulosa.watney.plate.solving.quad.ImageStarQuad
import nebulosa.watney.plate.solving.quad.StarQuad
import nebulosa.watney.plate.solving.quad.StarQuadMatch
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

        @JvmStatic private val LOG = loggerFor<WatneyPlateSolver>()

        @JvmStatic
        private fun formImageStarQuads(starsFound: List<ImageStar>): Pair<List<ImageStarQuad>, Int> {
            val quads = ArrayList<ImageStarQuad>()
            var countInFirstPass = 0

            // Do a few passes. Experimental.
            repeat(4) { p ->
                val starsToUse = (starsFound.size * (1 - p * 0.05)).toInt()
                val stars = starsFound.subList(0, starsToUse)

                val starDistances = Array(stars.size) { DoubleArray(stars.size) }

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
                    val nearestDistances = DoubleArray(nearestIndices.size)

                    for (n in 0..2) {
                        var index = 0
                        var dist = Double.MAX_VALUE

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

                    val ratios = DoubleArray(sixDistances.size - 1) { sixDistances[it] / largestDistance }
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

        /*
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
                    pixelAngularSearchFieldSizeRatio, quadDatabase, imageStarQuads,
                    quadsPerSqDeg, MIN_MATCHES, searchRun.densityOffsets
                )

                if (!isValidSolution(improvedSolution.solution)) {
                    return taskResult
                }
            }
        }
        */

        @JvmStatic
        private fun solvePlateConstants(matches: List<StarQuadMatch>, centerRA: Angle, centerDEC: Angle): PlateConstants {
            val equationInputs = matches.map {
                val (x, y) = equatorialToStandardCoordinates(it.catalogQuad.midPointX, it.catalogQuad.midPointY, centerRA, centerDEC)
                doubleArrayOf(it.imageQuad.midPointX, it.imageQuad.midPointY, 1.0, x, y)
            }

            val (a, b, c) = solveLeastSquares(equationInputs)
            val (d, e, f) = solveLeastSquares(equationInputs.onEach { it[3] = it[4] })

            return PlateConstants(a, b, c, d, e, f)
        }

        @JvmStatic
        private fun calculateSolution(image: Image, matches: List<StarQuadMatch>, scopeCoordsRA: Angle, scopeCoordsDEC: Angle) {
            var pc = solvePlateConstants(matches, scopeCoordsRA, scopeCoordsDEC)

            // Filter out the matches further - there may still be mismatches that are distorting the solution.
            val acceptedMatches = reduceToBestMatches(matches, pc, scopeCoordsRA, scopeCoordsDEC)
            pc = solvePlateConstants(acceptedMatches, scopeCoordsRA, scopeCoordsDEC)
            val newMatches = acceptedMatches

            val pixelsPerDeg = calculatePixelsPerDegree(matches)
            val fieldPixelDiameter = hypot(image.width.toDouble(), image.height.toDouble())
            val fieldPixelRadius = 0.5 * fieldPixelDiameter
            val fieldRadiusDeg = 0.5 * fieldPixelDiameter / pixelsPerDeg
            val fieldWidthDeg = image.width / pixelsPerDeg
            val fieldHeightDeg = image.height / pixelsPerDeg

            // Arcseconds per pixel
            val pixScale = 3600 * fieldRadiusDeg / fieldPixelRadius

            // Calculate the transformation matrix by calculating what one pixel move accounts
            // for in both x and y directions.
            val centerX = image.width / 2
            val centerY = image.height / 2
            var x = pc.a * centerX + pc.b * centerY + pc.c
            var y = pc.d * centerX + pc.e * centerY + pc.f
            val imageCenterEquatorial = standardToEquatorialCoordinates(scopeCoordsRA, scopeCoordsDEC, x, y)

            // One pixel step in the positive Y direction
            x = pc.a * centerX + pc.b * (centerY + 1) + pc.c
            y = pc.d * centerX + pc.e * (centerY + 1) + pc.f
            val upEquatorial = standardToEquatorialCoordinates(scopeCoordsRA, scopeCoordsDEC, x, y)

            // One pixel step in the positive X direction
            x = pc.a * (centerX + 1) + pc.b * centerY + pc.c
            y = pc.d * (centerX + 1) + pc.e * centerY + pc.f
            val rightEquatorial = standardToEquatorialCoordinates(scopeCoordsRA, scopeCoordsDEC, x, y)

            // Calculate the matrix components CD* that can be used to calculate any pixel RA/Dec
            var dRa = rightEquatorial.first - imageCenterEquatorial.first
            val cd11 = (dRa * cos(imageCenterEquatorial.second)).toDegrees
            val cd21 = (rightEquatorial.second - imageCenterEquatorial.second).toDegrees

            dRa = upEquatorial.first - imageCenterEquatorial.first
            val cd12 = (dRa * cos(imageCenterEquatorial.second)).toDegrees
            val cd22 = (upEquatorial.second - imageCenterEquatorial.second).toDegrees

            var crota1 = 0.0
            var crota2 = 0.0
            var cdelt1 = cd11
            var cdelt2 = cd22

            // https://www.virtualastronomy.org/AVM_DRAFTVersion12_rlh02.pdf

            // The determinant, which also tells us if the image is mirrored.
            val sign = if (cd11 * cd22 - cd12 * cd21 < 0) -1 else 1

            if (cd21 != 0.0 && cd12 != 0.0) {
                crota1 = atan2(sign * cd12, cd22)
                crota2 = -atan2(cd21, sign * cd11)
                cdelt1 = sign * sqrt(cd11 * cd11 + cd21 * cd21)
                cdelt2 = sqrt(cd12 * cd12 + cd22 * cd22)
            }

            val scopePxY = (-(-pc.a / pc.d * pc.f) + -pc.c) / (pc.b + -pc.a / pc.d * pc.e)
            val scopePxX = (-pc.b * scopePxY + (-pc.c)) / pc.a
            val parity = if (sign < 0) Parity.NORMAL else Parity.FLIPPED
        }

        @JvmStatic
        private fun calculatePixelsPerDegree(matches: List<StarQuadMatch>): Double {
            val pixelsPerDegree = ArrayList<Double>()

            for (i in matches.indices) {
                for (j in i + 1 until matches.size) {
                    val dx = matches[i].imageQuad.midPointX.toDegrees - matches[j].imageQuad.midPointX.toDegrees
                    val dy = matches[i].imageQuad.midPointY.toDegrees - matches[j].imageQuad.midPointY.toDegrees
                    val pixelDist = sqrt(dx * dx + dy * dy)

                    // TODO: Checks if midpoints is in radians
                    val angularDist = SphericalCoordinate.angularDistance(
                        matches[i].catalogQuad.midPointX, matches[i].catalogQuad.midPointY,
                        matches[j].catalogQuad.midPointX, matches[j].catalogQuad.midPointY,
                    ).toDegrees

                    if (angularDist > 0 && pixelDist > 0) {
                        pixelsPerDegree.add(pixelDist / angularDist)
                    }
                }
            }

            return pixelsPerDegree.average()
        }

        private fun reduceToBestMatches(
            matches: List<StarQuadMatch>,
            initialPlateConstants: PlateConstants,
            scopeCoordsRA: Angle, scopeCoordsDEC: Angle,
        ): List<StarQuadMatch> {
            var squaredSumsA = 0.0
            var squaredSumsB = 0.0
            var squaredSumsC = 0.0
            var squaredSumsD = 0.0
            var squaredSumsE = 0.0
            var squaredSumsF = 0.0
            var squaredSumsSR = 0.0

            val scaleRatios = DoubleArray(matches.size) { matches[it].scaleRatio }
            scaleRatios.sort()
            val midIndex = scaleRatios.size / 2
            val medianScaleRatio = if (scaleRatios.size % 2 != 0) scaleRatios[midIndex]
            else (scaleRatios[midIndex] + scaleRatios[midIndex - 1]) / 2

            val matchDeltas = arrayOfNulls<PlateConstants>(matches.size)
            val matchMedianScaleRatioDeviances = DoubleArray(matches.size)

            for (i in matches.indices) {
                val match = matches[i]
                val matchListCopy = matches.filter { it !== match }
                val pc = solvePlateConstants(matchListCopy, scopeCoordsRA, scopeCoordsDEC)

                val deltas = PlateConstants(
                    pc.a - initialPlateConstants.a, pc.b - initialPlateConstants.b,
                    pc.c - initialPlateConstants.c, pc.d - initialPlateConstants.d,
                    pc.e - initialPlateConstants.e, pc.f - initialPlateConstants.f,
                )

                matchDeltas[i] = deltas
                matchMedianScaleRatioDeviances[i] = abs(medianScaleRatio - match.scaleRatio)

                squaredSumsA += deltas.a * deltas.a
                squaredSumsB += deltas.b * deltas.b
                squaredSumsC += deltas.c * deltas.c
                squaredSumsD += deltas.d * deltas.d
                squaredSumsE += deltas.e * deltas.e
                squaredSumsF += deltas.f * deltas.f
                squaredSumsSR += (medianScaleRatio - match.scaleRatio) * (medianScaleRatio - match.scaleRatio)
            }

            val deviationThresholdA = 2 * sqrt(squaredSumsA / matches.size)
            val deviationThresholdB = 2 * sqrt(squaredSumsB / matches.size)
            val deviationThresholdC = 2 * sqrt(squaredSumsC / matches.size)
            val deviationThresholdD = 2 * sqrt(squaredSumsD / matches.size)
            val deviationThresholdE = 2 * sqrt(squaredSumsE / matches.size)
            val deviationThresholdF = 2 * sqrt(squaredSumsF / matches.size)

            val deviationThresholdSRSigma = sqrt(squaredSumsSR / matches.size)

            // The higher the ScaleRatio, the more leeway we can give (better effective resolution)
            //
            // Experimental values.
            // Minimum tolerated scale ratio deviation (sigma)
            val minSrSigma = 0.66
            // Maximum tolerated scale ratio deviation (sigma)
            val maxSrSigma = 2.0
            // Scale ratio of large fields/low resolution, border value
            val minSrAtValue = 300.0
            // Scale ratio of smaller fields/high resolution, border value
            val maxSrAtValue = 1500.0

            val deviationThresholdScaleRatioSigmaFactor = if (medianScaleRatio <= minSrAtValue) minSrSigma
            else if (medianScaleRatio >= maxSrAtValue) maxSrSigma
            else lerp(minSrSigma, maxSrSigma, (medianScaleRatio - minSrAtValue) / (maxSrAtValue - minSrAtValue))

            val deviationThresholdScaleRatio = deviationThresholdSRSigma * deviationThresholdScaleRatioSigmaFactor
            val filtered = ArrayList<StarQuadMatch>(matchDeltas.size)

            for (i in matchDeltas.indices) {
                val match = matches[i]
                val scaleRatioDeviance = matchMedianScaleRatioDeviances[i]
                val deltas = matchDeltas[i]!!

                if (abs(deltas.a) > deviationThresholdA || abs(deltas.b) > deviationThresholdB ||
                    abs(deltas.c) > deviationThresholdC || abs(deltas.d) > deviationThresholdD ||
                    abs(deltas.e) > deviationThresholdE || abs(deltas.f) > deviationThresholdF ||
                    scaleRatioDeviance > deviationThresholdScaleRatio
                ) {
                    continue
                }

                filtered.add(match)
            }

            return if (filtered.size >= 8) {
                filtered
            } else {
                LOG.info("Not enough matches to perform filtering, with so few matches assuming they're good")
                matches
            }
        }
    }
}
