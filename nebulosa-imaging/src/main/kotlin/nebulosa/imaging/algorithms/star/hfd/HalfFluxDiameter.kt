package nebulosa.imaging.algorithms.star.hfd

import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.ComputationAlgorithm
import kotlin.math.*

data class HalfFluxDiameter(
    val x: Double, val y: Double,
    val searchRegion: Double = 15.0,
    val mode: FindMode = FindMode.CENTROID,
    val minHFD: Double = 1.5,
) : ComputationAlgorithm<ImageStar> {

    override fun compute(source: Image): ImageStar {
        val minX = 0
        val minY = 0
        val maxX = source.width - 1
        val maxY = source.height - 1

        var startX = max((x - searchRegion).toInt(), minX)
        var endX = min((x + searchRegion).toInt(), maxX)
        var startY = max((y - searchRegion).toInt(), minY)
        var endY = min((y + searchRegion).toInt(), maxY)

        if (endX <= startX || endY <= startY) {
            return ImageStar(x, y)
        }

        var peakX = 0
        var peakY = 0
        var peak = 0.0
        val rawPeak: Double
        val max3 = DoubleArray(3)

        if (mode == FindMode.PEAK) {
            for (y in startY..endY) {
                for (x in startX..endY) {
                    val p = source.readGray(x, y) * 65535.0

                    if (p > peak) {
                        peak = p
                        peakX = x
                        peakY = y
                    }
                }
            }

            rawPeak = peak
        } else {
            // Find the peak value within the search region
            // using a smoothing function also check for saturation.
            for (y in startY + 1 until endY) {
                for (x in startX + 1 until endX) {
                    var p = source.readGray(x, y) * 65535.0

                    // TODO: Optimize this using stride instead of call readGray and remove 65535 multiplication.
                    val pv = 4 * p + source.readGray(x - 1, y - 1) * 65535.0 +
                            source.readGray(x + 1, y - 1) * 65535.0 +
                            source.readGray(x - 1, y + 1) * 65535.0 +
                            source.readGray(x + 1, y + 1) * 65535.0 +
                            2 * source.readGray(x + 0, y - 1) * 65535.0 +
                            2 * source.readGray(x - 1, y + 0) * 65535.0 +
                            2 * source.readGray(x + 1, y + 0) * 65535.0 +
                            2 * source.readGray(x + 0, y + 1) * 65535.0

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
                }
            }

            rawPeak = max3[0]   // Raw peak value.
            peak /= 16 // Smoothed peak value.
        }

        // Center window around peak value.
        startX = max(peakX - B, minX)
        endX = min(peakX + B, maxX)
        startY = max(peakY - B, minY)
        endY = min(peakY + B, maxY)

        // Find the mean and stdev of the background.
        var nbg = 0
        var meanBg = 0.0
        var prevMeanBg: Double
        var sigma2Bg = 0.0
        var sigmaBg = 0.0

        for (i in 0..8) {
            var sum = 0.0
            var a = 0.0
            var q = 0.0
            nbg = 0

            for (y in startY..endY) {
                val dy = y - peakY
                val dy2 = dy * dy

                for (x in startX..endX) {
                    val dx = x - peakX
                    val r2 = dx * dx + dy2

                    // Exclude points not in annulus.
                    if (r2 <= A2 || r2 > B2) continue

                    val p = source.readGray(x, y) * 65535f

                    if (i > 0 && (p < meanBg - 2f * sigmaBg || p > meanBg + 2f * sigmaBg)) continue

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

            // val msg = "Star::Find iter=%d prev_mean_bg=%.4f mean_bg=%.4f sigma2_bg=%.4f sigma_bg=%.4f"

            if (i > 0 && abs(meanBg - prevMeanBg) < 0.5) break
        }

        val thresh: Double

        var cx = 0.0
        var cy = 0.0
        var mass = 0.0
        var n: Int

        val hfrvec = ArrayList<R2M>(searchRegion.toInt() * searchRegion.toInt())

        if (mode == FindMode.PEAK) {
            mass = peak
            n = 1
            thresh = 0.0
        } else {
            thresh = truncate(meanBg + 3f * sigmaBg + 0.5f)

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

                for (x in startX..endX) {
                    val dx = x - peakX

                    // Exclude points outside aperture.
                    if (dx * dx + dy2 > A2) continue

                    // Exclude points below threshold.
                    val p = source.readGray(x, y) * 65535f

                    if (p < thresh) continue

                    val d = p - meanBg

                    cx += dx * d
                    cy += dy * d
                    mass += d
                    n++

                    hfrvec.add(R2M(x.toDouble(), y.toDouble(), d))
                }
            }
        }

        // SNR estimate from: Measuring the Signal-to-Noise Ratio S/N of the CCD Image of a Star
        // or Nebula, J.H.Simonetti, 2004 January 8
        // http://www.phys.vt.edu/~jhs/phys3154/snr20040108.pdf
        var snr = if (n > 0) mass / sqrt(mass / 0.5f + sigma2Bg * n * (1f + 1f / nbg)) else 0.0

        // A few scattered pixels over threshold can give a false positive
        // avoid this by requiring the smoothed peak value to be above the threshold.
        if (peak <= thresh && snr >= LOW_SNR) {
            snr = LOW_SNR - 0.1
        }

        if (mass < 10f) {
            return ImageStar(x, y, mass, snr, 0.0, rawPeak, FindResult.LOWMASS)
        }

        val newX = peakX + cx / mass
        val newY = peakY + cy / mass

        if (snr < LOW_SNR) {
            return ImageStar(newX, newY, mass, snr, 0.0, rawPeak, FindResult.LOWSNR)
        }

        val hfd = 2f * HalfFluxRadius(newX, newY, mass, hfrvec).compute()

        if (hfd < minHFD && mode != FindMode.PEAK) {
            return ImageStar(newX, newY, mass, snr, hfd, rawPeak, FindResult.LOWHFD)
        }

        val mx = max3[0]

        // check for saturation.
        if (mx >= 65535f) {
            return ImageStar(newX, newY, mass, snr, hfd, rawPeak, FindResult.SATURATED)
        }

        // maxADU not known, use the "flat-top" hueristic
        // even at saturation, the max values may vary a bit due to noise
        // Call it saturated if the top three values are within 32 parts per 65535 of max for 16-bit cameras,
        // or within 1 part per 191 for 8-bit cameras
        // val d = max3[0] - max3[2]

        // if (d * 65535f < 32f * mx) {
        //     result = FindResult.SATURATED
        //     return@run
        // }

        return ImageStar(newX, newY, mass, snr, hfd, rawPeak, FindResult.OK)
    }

    companion object {

        private const val A = 7   // Inner radius.
        private const val B = 12  // outer radius.
        private const val A2 = A * A
        private const val B2 = B * B

        private const val LOW_SNR = 3f
    }
}
