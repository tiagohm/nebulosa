package nebulosa.watney.plate.solving

import nebulosa.common.concurrency.cancel.CancellationToken
import nebulosa.erfa.SphericalCoordinate
import nebulosa.fits.Header
import nebulosa.fits.NOAOExt
import nebulosa.fits.Standard
import nebulosa.fits.fits
import nebulosa.imaging.Image
import nebulosa.log.debug
import nebulosa.log.loggerFor
import nebulosa.math.Angle
import nebulosa.math.deg
import nebulosa.math.toDegrees
import nebulosa.plate.solving.Parity
import nebulosa.plate.solving.PlateSolution
import nebulosa.plate.solving.PlateSolver
import nebulosa.star.detection.ImageStar
import nebulosa.star.detection.StarDetector
import nebulosa.watney.plate.solving.math.equatorialToStandardCoordinates
import nebulosa.watney.plate.solving.math.lerp
import nebulosa.watney.plate.solving.math.solveLeastSquares
import nebulosa.watney.plate.solving.math.standardToEquatorialCoordinates
import nebulosa.watney.plate.solving.quad.ImageStarQuad
import nebulosa.watney.plate.solving.quad.QuadDatabase
import nebulosa.watney.plate.solving.quad.StarQuad
import nebulosa.watney.plate.solving.quad.StarQuadMatch
import nebulosa.watney.star.detection.WatneyStarDetector
import org.apache.commons.collections4.bag.HashBag
import java.nio.file.Path
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.IntStream
import kotlin.math.*

data class WatneyPlateSolver(
    private val quadDatabase: QuadDatabase,
    private val starDetector: StarDetector<Image>? = null,
    private val numSubSets: Int = 4,
) : PlateSolver {

    @Suppress("NAME_SHADOWING")
    override fun solve(
        path: Path?, image: Image?,
        centerRA: Angle, centerDEC: Angle, radius: Angle,
        downsampleFactor: Int, timeout: Duration?,
        cancellationToken: CancellationToken,
    ): PlateSolution {
        val image = image ?: path!!.fits().let(Image::open)
        val stars = (starDetector ?: DEFAULT_STAR_DETECTOR).detect(image)

        LOG.debug { "detected ${stars.size} stars from the image" }

        fun makeSuccessSolution(solution: ComputedPlateSolution): PlateSolution {
            return PlateSolution(
                true, solution.orientation, solution.pixelScale,
                solution.centerRA, solution.centerDEC, solution.width, solution.height,
                solution.parity, solution.radius,
            )
        }

        if (stars.isEmpty()) return PlateSolution.NO_SOLUTION

        val (imageStarQuads, countInFirstPass) = formImageStarQuads(stars)
        LOG.debug { "formed ${imageStarQuads.size} quads from the chosen stars" }

        val strategy = if (radius.toDegrees >= 0.1) {
            val options = NearbySearchStrategyOptions(maxFieldRadius = radius, maxNegativeDensityOffset = 2, maxPositiveDensityOffset = 2)
            NearbySearchStrategy(centerRA, centerDEC, options)
        } else {
            val options = BlindSearchStrategyOptions(maxNegativeDensityOffset = 2, maxPositiveDensityOffset = 2)
            BlindSearchStrategy(options)
        }

        LOG.debug { "strategy: $strategy" }

        val searchQueue = strategy.searchQueue()

        val groupedSearchQueue = TreeMap<Double, MutableList<SearchRun>>(Collections.reverseOrder())
        val runsByRadius = searchQueue.groupByTo(groupedSearchQueue) { it.radius.toDegrees }.toList()

        val serialSearches = ArrayList<SolveResult>()
        val iteration = AtomicInteger(0)

        repeat(numSubSets) {
            for (rg in runsByRadius.indices) {
                for (searchRun in runsByRadius[rg].second) {
                    val solveResult = trySolve(image, searchRun, countInFirstPass, quadDatabase, numSubSets, it, imageStarQuads, iteration)

                    serialSearches.add(solveResult)

                    if (solveResult.success) {
                        return makeSuccessSolution(solveResult.solution!!)
                    }
                }

                // If only one subset aka no sampling, no need to check potential matches since they aren't potential as
                // we're already using all database quads in our search.
                if (numSubSets == 1)
                    continue

                val (matchedResult) = getMatchedAndUnmatchedSearchRuns(serialSearches)
                serialSearches.clear()

                @Suppress("NestedLambdaShadowedImplicitParameter")
                val potentialMatchQueue = matchedResult.map { it.searchRun!! }

                // Remove all potentials so that we don't search them again in the next subset.
                // They aren't a significant number, but every little bit helps.
                for (m in potentialMatchQueue.indices) {
                    runsByRadius[rg].second.remove(potentialMatchQueue[m])
                }

                LOG.debug { "continue searching, potential matches to try: ${potentialMatchQueue.size}" }

                for (searchRun in potentialMatchQueue) {
                    val solveResult = trySolve(image, searchRun, countInFirstPass, quadDatabase, 1, 0, imageStarQuads, iteration)

                    if (solveResult.success) {
                        LOG.info("a successful result was found!")
                        return makeSuccessSolution(solveResult.solution!!)
                    }
                }
            }
        }

        return PlateSolution.NO_SOLUTION
    }

    companion object {

        private const val MIN_MATCHES = 5

        @JvmStatic private val LOG = loggerFor<WatneyPlateSolver>()
        @JvmStatic private val DEFAULT_STAR_DETECTOR = WatneyStarDetector()

        @JvmStatic
        internal fun formImageStarQuads(starsFound: List<ImageStar>): Pair<List<ImageStarQuad>, Int> {
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

        @JvmStatic
        private fun trySolve(
            image: Image, searchRun: SearchRun, countInFirstPass: Int,
            quadDatabase: QuadDatabase, numSubSets: Int, subSetIndex: Int,
            imageStarQuads: List<ImageStarQuad>,
            iteration: AtomicInteger,
        ): SolveResult {
            val solveResult = SolveResult(searchRun = searchRun)

            // Quads per degree.
            val searchFieldSize = searchRun.radius.toDegrees * 2
            val a = atan(image.height.toDouble() / image.width)
            val s1 = searchFieldSize * sin(a)
            val s2 = searchFieldSize * cos(a)
            val area = s1 * s2
            val quadsPerSqDeg = (countInFirstPass / area).toInt()

            val imageDiameterInPixels = hypot(image.width.toDouble(), image.height.toDouble())
            var pixelAngularSearchFieldSizeRatio = imageDiameterInPixels / searchFieldSize

            val iterationCount = iteration.getAndIncrement()

            var databaseQuads = quadDatabase.quads(
                searchRun.centerRA, searchRun.centerDEC, searchRun.radius, quadsPerSqDeg,
                searchRun.densityOffsets, numSubSets, subSetIndex, imageStarQuads
            )

            solveResult.numPotentialMatches = databaseQuads.size

            if (databaseQuads.isNotEmpty()) {
                LOG.debug { "iteration $iterationCount [${searchRun.centerRA.toDegrees}, ${searchRun.centerDEC.toDegrees}] (${searchRun.radius.toDegrees}): ${databaseQuads.size} potential database matches" }
            }

            if (databaseQuads.size < MIN_MATCHES) {
                return solveResult
            }

            // Found enough matches; a likely hit. If this was a sampled run, spend the time to retrieve the full quad set without sampling
            // as we're going to try for a solution.
            if (numSubSets > 1) {
                databaseQuads = quadDatabase.quads(
                    searchRun.centerRA, searchRun.centerDEC, searchRun.radius, quadsPerSqDeg, searchRun.densityOffsets, 1, 0,
                    imageStarQuads
                )
            }

            val matchingQuads = findMatches(pixelAngularSearchFieldSizeRatio, imageStarQuads, databaseQuads.toMutableList(), 0.011, MIN_MATCHES)

            if (matchingQuads.size >= MIN_MATCHES) {
                val preliminarySolution = calculateSolution(image, matchingQuads, searchRun.centerRA, searchRun.centerDEC)

                if (!isValidSolution(preliminarySolution.first)) {
                    return solveResult
                }

                // Probably off really badly, so don't accept it.
                if (preliminarySolution.first.radius > 2 * searchRun.radius) {
                    return solveResult
                }

                pixelAngularSearchFieldSizeRatio = imageDiameterInPixels / preliminarySolution.first.radius * 2

                val improvedSolution = performAccuracyImprovementForSolution(
                    image, preliminarySolution.first,
                    pixelAngularSearchFieldSizeRatio, quadDatabase, imageStarQuads,
                    quadsPerSqDeg, MIN_MATCHES, searchRun.densityOffsets
                )

                if (!isValidSolution(improvedSolution?.first)) {
                    return solveResult
                }

                solveResult.success = true
                solveResult.matchedQuads = improvedSolution!!.second
                solveResult.solution = improvedSolution.first
            }

            return solveResult
        }

        @JvmStatic
        private fun isValidSolution(solution: ComputedPlateSolution?): Boolean {
            return solution != null && solution.centerRA.isFinite() && solution.centerDEC.isFinite()
                && solution.orientation.isFinite() && solution.plateConstants.isValid
        }

        @JvmStatic
        private fun performAccuracyImprovementForSolution(
            image: Image, solution: ComputedPlateSolution,
            pixelAngularSearchFieldSizeRatio: Double, quadDatabase: QuadDatabase,
            imageStarQuads: List<ImageStarQuad>, quadsPerSqDeg: Int,
            minMatches: Int, densityOffsets: IntArray,
        ): Pair<ComputedPlateSolution, List<StarQuadMatch>>? {
            // Include many, to improve the odds and to maximize match chances.
            // var fullDensityOffsets = new[] {-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5};
            // Actually, use given offsets. The result is likely going to be more accurate on large fields due to smaller chance of mismatches.
            val databaseQuads = quadDatabase
                .quads(solution.centerRA, solution.centerDEC, solution.radius, quadsPerSqDeg, densityOffsets, 1, 0, imageStarQuads)
            val matchingQuads = findMatches(pixelAngularSearchFieldSizeRatio, imageStarQuads, databaseQuads.toMutableList(), 0.011, minMatches)
            return if (matchingQuads.size >= minMatches) calculateSolution(image, matchingQuads, solution.centerRA, solution.centerDEC)
            else null
        }

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
        private fun calculateSolution(
            image: Image,
            matches: List<StarQuadMatch>,
            scopeCoordsRA: Angle,
            scopeCoordsDEC: Angle
        ): Pair<ComputedPlateSolution, List<StarQuadMatch>> {
            var pc = solvePlateConstants(matches, scopeCoordsRA, scopeCoordsDEC)

            // Filter out the matches further - there may still be mismatches that are distorting the solution.
            val acceptedMatches = reduceToBestMatches(matches, pc, scopeCoordsRA, scopeCoordsDEC)
            pc = solvePlateConstants(acceptedMatches, scopeCoordsRA, scopeCoordsDEC)

            val pixelsPerDeg = calculatePixelsPerDegree(acceptedMatches)
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
            var dRa = rightEquatorial[0] - imageCenterEquatorial[0]
            val cd11 = (dRa * cos(imageCenterEquatorial[1])).toDegrees
            val cd21 = (rightEquatorial[1] - imageCenterEquatorial[1]).toDegrees

            dRa = upEquatorial[0] - imageCenterEquatorial[0]
            val cd12 = (dRa * cos(imageCenterEquatorial[1])).toDegrees
            val cd22 = (upEquatorial[1] - imageCenterEquatorial[1]).toDegrees

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

            val header = Header()
            header.add(Standard.CDELT1, cdelt1)
            header.add(Standard.CDELT2, cdelt2)
            header.add(Standard.CROTA1, crota1.toDegrees)
            header.add(Standard.CROTA2, crota2.toDegrees)
            header.add(Standard.CRVAL1, scopeCoordsRA.toDegrees)
            header.add(Standard.CRVAL2, scopeCoordsDEC.toDegrees)
            header.add(Standard.CRPIX1, scopePxX)
            header.add(Standard.CRPIX2, scopePxY)
            header.add(NOAOExt.CD1_1, cd11)
            header.add(NOAOExt.CD1_2, cd12)
            header.add(NOAOExt.CD1_1, cd11)
            header.add(NOAOExt.CD2_2, cd22)

            return ComputedPlateSolution(
                header, crota1, pixScale, scopeCoordsRA, scopeCoordsDEC,
                fieldWidthDeg.deg, fieldHeightDeg.deg, fieldPixelRadius.deg,
                parity, pc,
            ) to acceptedMatches
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

        @JvmStatic
        private fun getMatchedAndUnmatchedSearchRuns(results: List<SolveResult>): Pair<List<SolveResult>, List<SolveResult>> {
            val withMatches = ArrayList<SolveResult>(results.size)
            val withoutMatches = ArrayList<SolveResult>(results.size)

            for (result in results) {
                if (result.numPotentialMatches > 0) withMatches.add(result)
                else withoutMatches.add(result)
            }

            withMatches.sortBy { it.numPotentialMatches }

            return withMatches to withoutMatches
        }

        @JvmStatic
        private fun findMatches(
            pixelToAngleRatio: Double, imageQuads: List<ImageStarQuad>,
            dbQuads: MutableList<StarQuad>,
            threshold: Double, minMatches: Int,
        ): List<StarQuadMatch> {
            val matches = HashBag<StarQuadMatch>()

            val batchSize = 5
            val imageQuadBatches = ArrayList<Array<ImageStarQuad?>>(imageQuads.size / batchSize)
            var batch = arrayOfNulls<ImageStarQuad>(batchSize)

            for (i in imageQuads.indices) {
                batch[i % batchSize] = imageQuads[i]

                if ((i + 1) % batchSize == 0 || i == imageQuads.size - 1) {
                    imageQuadBatches.add(batch)
                    batch = arrayOfNulls(batchSize)
                }
            }

            for (b in imageQuadBatches.indices) {
                val dbQuadsFound = HashBag<StarQuad>()

                IntStream.range(0, 5).parallel().forEach {
                    val imageQuad = imageQuadBatches[b][it] ?: return@forEach

                    for (j in dbQuads.indices) {
                        val d1 = abs(imageQuad.ratios[0] / dbQuads[j].ratios[0] - 1.0)
                        if (d1 > threshold) continue

                        val d2 = abs(imageQuad.ratios[1] / dbQuads[j].ratios[1] - 1.0)
                        if (d2 > threshold) continue

                        val d3 = abs(imageQuad.ratios[2] / dbQuads[j].ratios[2] - 1.0)
                        if (d3 > threshold) continue

                        val d4 = abs(imageQuad.ratios[3] / dbQuads[j].ratios[3] - 1.0)
                        if (d4 > threshold) continue

                        val d5 = abs(imageQuad.ratios[4] / dbQuads[j].ratios[4] - 1.0)
                        if (d5 > threshold) continue

                        synchronized(matches) {
                            matches.add(StarQuadMatch(dbQuads[j], imageQuad))
                            dbQuadsFound.add(dbQuads[j])
                        }

                        // This is a must; we have observed remote possibility of duplicates.
                        // And one pixel coordinate should match exactly one quad.
                        break
                    }
                }

                dbQuads.removeAll(dbQuadsFound)
            }

            if (matches.size < minMatches) return emptyList()

            // Ratios' median absolute deviance shouldn't be off more than this,
            // if it is, then we're probably having a wild set of mismatches.
            val acceptedAbsoluteDev = pixelToAngleRatio * 0.01

            val matchList = matches.toTypedArray()
            matchList.sortBy { it.scaleRatio }
            val midIndex = matchList.size / 2
            val medianScaleRatio = if (matchList.size % 2 != 0) matchList[midIndex].scaleRatio
            else (matchList[midIndex].scaleRatio + matchList[midIndex - 1].scaleRatio) / 2

            val scaleRatioAbsoluteDeviances = DoubleArray(matchList.size) { abs(matchList[it].scaleRatio - medianScaleRatio) }
            scaleRatioAbsoluteDeviances.sort()
            val medianAbsoluteDevianceScaleRatio = if (scaleRatioAbsoluteDeviances.size % 2 != 0) scaleRatioAbsoluteDeviances[midIndex]
            else (scaleRatioAbsoluteDeviances[midIndex] + scaleRatioAbsoluteDeviances[midIndex - 1]) / 2

            // If the scale ratios are wildly random, this can't be a match.
            if (medianAbsoluteDevianceScaleRatio > acceptedAbsoluteDev) return emptyList()

            // Form bins of the matches, and calculate a weighted average using the bins.
            // This is to make sure the mismatches (wild scale ratios) do not affect the
            // average in an unreasonable manner.
            val minScaleRatio = matchList.first().scaleRatio
            val maxScaleRatio = matchList.last().scaleRatio
            val numBins = 10
            val binWidth = (maxScaleRatio - minScaleRatio) / numBins + 1
            val weights = IntArray(numBins)
            val indexWeights = IntArray(matchList.size)

            for (i in matchList.indices) {
                val w = ((matchList[i].scaleRatio - minScaleRatio) / binWidth).toInt()
                weights[w]++
                indexWeights[i] = w
            }

            var divider = 0.0
            var total = 0.0

            for (i in matchList.indices) {
                val weight = weights[indexWeights[i]]
                total += weight * matchList[i].scaleRatio
                divider += weight
            }

            val weightedMean = total / divider

            val differenceSquared = matches.sumOf { (it.scaleRatio - weightedMean).pow(2) }
            val stdDev = sqrt(differenceSquared / matches.size)

            return matches
                .filter { abs(it.scaleRatio - weightedMean) < stdDev }
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
