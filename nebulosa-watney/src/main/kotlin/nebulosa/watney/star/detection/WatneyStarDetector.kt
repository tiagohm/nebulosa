package nebulosa.watney.star.detection

import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.Statistics
import nebulosa.imaging.hfd.HalfFluxDiameter
import nebulosa.star.detection.StarDetector
import kotlin.math.roundToInt

data class WatneyStarDetector(
    private val detectionFilter: StarDetectionFilter = DefaultStarDetectionFilter,
    private val starDetectionBgOffset: Float = 3f,
    private val minHFD: Float = 1.5f,
    private val minStarSize: Float = 4f,
) : StarDetector<Image> {

    override fun detect(input: Image): List<DetectedStar> {
        val starBins = ArrayList<StarPixelBin>()

        val stats = Statistics().compute(input)

        // Too dark or broken image.
        if (stats.mean == 0f) return emptyList()

        val flatValue = stats.mean + (stats.stdDev * starDetectionBgOffset)
        var previousLineBins: MutableList<StarPixelBin> = ArrayList(0)

        repeat(input.height) {
            previousLineBins = starBins
                .binStarPixelsFromScanline(input, it, flatValue, previousLineBins)
        }

        starBins.forEach(StarPixelBin::recalculateBounds)
        detectionFilter.filter(starBins, input)

        return starBins.mapNotNull {
            val star = it.computeCenterPixelPosAndRelativeBrightness()
            if (star.size < minStarSize) return@mapNotNull null
            val computedStar = HalfFluxDiameter.compute(input, star.x, star.y, (star.size / 2).roundToInt())
            if (computedStar.hfd < minHFD) null
            else DetectedStar(computedStar.x, computedStar.y, star.size, computedStar.hfd, computedStar.snr, computedStar.flux)
        }
    }

    /**
     * Read whole line into star pixel bins (contiguous pixels over background value on X axis).
     * Then look up one row (x-1 and x+1) for previous line bins.
     * Combine the current bin to that/them (check from left to right,
     * combine self with topleftmost, and potentially the topright with topleftmost too)
     */
    private fun MutableList<StarPixelBin>.binStarPixelsFromScanline(
        image: Image,
        y: Int,
        flatValue: Float,
        previousLineBins: MutableList<StarPixelBin>,
    ): MutableList<StarPixelBin> {
        val scanLineBins = ArrayList<StarPixelBin>()
        var currentBin: StarPixelBin? = null

        var lineIdx = image.indexAt(0, y)

        repeat(image.width) {
            val pixel = image.readGrayBT709(lineIdx++)

            if (pixel >= flatValue) {
                if (currentBin == null) {
                    currentBin = StarPixelBin()
                    currentBin!!.add(it, y, pixel)
                    scanLineBins.add(currentBin!!)
                } else {
                    currentBin!!.add(it, y, pixel)
                }
            } else {
                currentBin = null
            }
        }

        // No combination to above star pixel bins required if none were present.
        if (previousLineBins.isEmpty()) {
            addAll(scanLineBins)
            return scanLineBins
        }

        // Merge into previous line's star pixel bins if they happen to be adjacent.

        // Find the ones above (+-1 px l/r)
        // Take first
        // Add our pixels to that one
        // Add the others' pixels to that one
        val rowOutputBins = ArrayList<StarPixelBin>(scanLineBins.size)

        for (starBin in scanLineBins) {
            val left = starBin.left
            val right = starBin.right
            var merged = false

            val connectedPreviousLinePixelBins = ArrayList<StarPixelBin>(previousLineBins.size)

            for (prevLineBin in previousLineBins) {
                val prevLineBinPixels = prevLineBin.pixelRows[y - 1]!!

                for (p in prevLineBinPixels.indices) {
                    if (prevLineBinPixels[p].x >= left - 1 && prevLineBinPixels[p].x <= right + 1) {
                        connectedPreviousLinePixelBins.add(prevLineBin)
                        break
                    }
                }
            }

            if (connectedPreviousLinePixelBins.isNotEmpty()) {
                val mergeTarget = connectedPreviousLinePixelBins[0]

                merged = true

                if (y !in mergeTarget.pixelRows)
                    mergeTarget.pixelRows[y] = ArrayList(starBin.pixelRows[y]!!)
                else
                    mergeTarget.pixelRows[y]!!.addAll(starBin.pixelRows[y]!!)

                if (mergeTarget !in rowOutputBins) {
                    rowOutputBins.add(mergeTarget)
                }

                for (n in 1 until connectedPreviousLinePixelBins.size) {
                    val mergeable = connectedPreviousLinePixelBins[n]

                    for ((k, value) in mergeable.pixelRows) {
                        if (k !in mergeTarget.pixelRows)
                            mergeTarget.pixelRows[k] = ArrayList(value)
                        else
                            mergeTarget.pixelRows[k]!!.addAll(value)
                    }

                    // Remove, since this is now merged with another one.
                    remove(mergeable)
                    previousLineBins.remove(mergeable)
                }
            }

            if (!merged) {
                add(starBin)
                rowOutputBins.add(starBin)
            }
        }

        return rowOutputBins
    }
}
