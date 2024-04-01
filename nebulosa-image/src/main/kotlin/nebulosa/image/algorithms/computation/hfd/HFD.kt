package nebulosa.image.algorithms.computation.hfd

import nebulosa.image.Image
import nebulosa.image.algorithms.ComputationAlgorithm
import kotlin.math.*

/**
 * Half Flux Diameter.
 *
 * @see <a href="https://github.com/OpenPHDGuiding/phd2/blob/master/star.cpp">PHD2 Reference</a>
 * @see <a href="https://www.lost-infinity.com/night-sky-image-processing-part-6-measuring-the-half-flux-diameter-hfd-of-a-star-a-simple-c-implementation/">Measuring the Half Flux Diameter (HFD) of a star</a>
 */
data class HFD(
    private val x: Int, private val y: Int,
    private val searchRegion: Int = 15,
    private val mode: Mode = Mode.CENTROID,
) : ComputationAlgorithm<HFD.ComputedStar> {

    enum class Mode {
        CENTROID,
        PEAK,
    }

    data class ComputedStar(
        @JvmField val x: Int, @JvmField val y: Int,
        @JvmField val hfd: Double = 0.0,
        @JvmField val snr: Double = 0.0,
        @JvmField val flux: Double = 0.0,
    )

    override fun compute(source: Image): ComputedStar {
        return compute(source, x, y, searchRegion, mode)
    }

    companion object {

        private const val A = 7   // Inner radius.
        private const val B = 12  // outer radius.
        private const val A2 = A * A
        private const val B2 = B * B

        @JvmStatic
        fun compute(
            source: Image,
            baseX: Int, baseY: Int,
            searchRegion: Int = 15,
            mode: Mode = Mode.CENTROID,
        ): ComputedStar {
            val minX = 0
            val minY = 0
            val maxX = source.width - 1
            val maxY = source.height - 1

            var startX = max((baseX - searchRegion), minX)
            var endX = min((baseX + searchRegion), maxX)
            var startY = max((baseY - searchRegion), minY)
            var endY = min((baseY + searchRegion), maxY)

            if (endX <= startX || endY <= startY) {
                return ComputedStar(baseX, baseY)
            }

            var peakX = 0
            var peakY = 0
            var peak = 0f
            val max3 = FloatArray(3)

            if (mode == Mode.PEAK) {
                for (y in startY..endY) {
                    var index = source.indexAt(startX, y)

                    for (x in startX..endY) {
                        val p = source.readGrayBT709(index++)

                        if (p > peak) {
                            peak = p
                            peakX = x
                            peakY = y
                        }
                    }
                }
            } else {
                // Find the peak value within the search region
                // using a smoothing function also check for saturation.
                for (y in startY + 1 until endY) {
                    var index = source.indexAt(startX + 1, y)

                    for (x in startX + 1 until endX) {
                        var p = source.readGrayBT709(index)

                        val pv = 4 * p + source.readGrayBT709(index - source.stride - 1) +
                                source.readGrayBT709(index - source.stride + 1) +
                                source.readGrayBT709(index + source.stride - 1) +
                                source.readGrayBT709(index + source.stride + 1) +
                                2 * source.readGrayBT709(index - source.stride) +
                                2 * source.readGrayBT709(index - 1) +
                                2 * source.readGrayBT709(index + 1) +
                                2 * source.readGrayBT709(index + source.stride)

                        if (pv > peak) {
                            peak = pv
                            peakX = x
                            peakY = y
                        }

                        if (p > max3[0]) {
                            val k = max3[0]
                            max3[0] = p
                            p = k
                        }

                        if (p > max3[1]) {
                            val k = max3[1]
                            max3[1] = p
                            p = k
                        }

                        if (p > max3[2]) {
                            // val k = max3[2]
                            max3[2] = p
                            // p = k
                        }

                        index++
                    }
                }

                peak /= 16f // Smoothed peak value.
            }

            // Center window around peak value.
            startX = max(peakX - B, minX)
            endX = min(peakX + B, maxX)
            startY = max(peakY - B, minY)
            endY = min(peakY + B, maxY)

            // Find the mean and stdev of the background.
            var nbg = 0
            var meanBg = 0f
            var prevMeanBg: Float
            var sigma2Bg = 0f
            var sigmaBg = 0f

            for (i in 0..8) {
                var sum = 0f
                var a = 0f
                var q = 0f
                nbg = 0

                for (y in startY..endY) {
                    val dy = y - peakY
                    val dy2 = dy * dy

                    var index = source.indexAt(startX, y) - 1

                    for (x in startX..endX) {
                        index++

                        val dx = x - peakX
                        val r2 = dx * dx + dy2

                        // Exclude points not in annulus.
                        if (r2 <= A2 || r2 > B2) continue

                        val p = source.readGrayBT709(index)

                        if (i > 0 && (p < meanBg - 2f * sigmaBg || p > meanBg + 2f * sigmaBg)) {
                            continue
                        }

                        sum += p
                        nbg++

                        val a0 = a
                        a += (p - a) / nbg
                        q += (p - a0) * (p - a)
                    }
                }

                if (nbg < 10) {
                    break
                }

                prevMeanBg = meanBg
                meanBg = sum / nbg
                sigma2Bg = q / (nbg - 1)
                sigmaBg = sqrt(sigma2Bg)

                if (i > 0 && abs(meanBg - prevMeanBg) < 0.5) break
            }

            var cx = 0f
            var cy = 0f
            var mass = 0f
            var n: Int

            val hfrvec = ArrayList<FloatArray>(4 * A2)

            if (mode == Mode.PEAK) {
                mass = peak
                n = 1
            } else {
                // val thresh = meanBg + 3f * sigmaBg

                // Find pixels over threshold within aperture; compute mass and centroid.
                startX = max(peakX - A, minX)
                endX = min(peakX + A, maxX)
                startY = max(peakY - A, minY)
                endY = min(peakY + A, maxY)

                n = 0

                for (y in startY..endY) {
                    val dy = y - peakY
                    val dy2 = dy * dy

                    if (dy2 > A2) continue

                    var index = source.indexAt(startX, y) - 1

                    for (x in startX..endX) {
                        index++

                        val dx = x - peakX

                        // Exclude points outside aperture.
                        if (dx * dx + dy2 > A2) continue

                        // Exclude points below threshold.
                        val p = source.readGrayBT709(index)

                        // if (p < thresh) continue

                        val d = (p - meanBg) * 65535

                        cx += dx * d
                        cy += dy * d
                        mass += d
                        n++

                        hfrvec.add(floatArrayOf(x.toFloat(), y.toFloat(), d))
                    }
                }
            }

            if (mass <= 0f) {
                return ComputedStar(baseX, baseY)
            }

            val newX = peakX + cx / mass
            val newY = peakY + cy / mass

            // SNR estimate from: Measuring the Signal-to-Noise Ratio S/N of the CCD Image of a Star
            // or Nebula, J.H.Simonetti, 2004 January 8
            // http://www.phys.vt.edu/~jhs/phys3154/snr20040108.pdf
            val snr = if (n > 0) mass / sqrt(mass / 0.5 + sigma2Bg * n * (1.0 + 1.0 / nbg))
            else 0.0

            val hfd = 2.0 * HFR.compute(newX, newY, mass, hfrvec)

            return ComputedStar(newX.roundToInt(), newY.roundToInt(), hfd, snr, mass.toDouble())
        }
    }
}
