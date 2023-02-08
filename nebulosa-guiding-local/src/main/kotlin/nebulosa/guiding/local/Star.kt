package nebulosa.guiding.local

import nebulosa.imaging.Image
import org.slf4j.LoggerFactory
import kotlin.math.*

/**
 * Represents a star.
 */
open class Star : Point {

    enum class FindResult {
        OK,
        SATURATED,
        LOWSNR,
        LOWMASS,
        LOWHFD,
        TOO_NEAR_EDGE,
        MASSCHANGE,
        ERROR;

        inline val wasFound
            get() = this == OK || this == SATURATED
    }

    var mass = 0f
        private set

    var snr = 0f
        private set

    var hfd = 0f
        private set

    var peak = 0f
        private set

    var lastFindResult = FindResult.ERROR
        private set

    constructor(x: Float, y: Float) : super(x, y)

    constructor(point: Point) : super(point)

    fun find(
        image: Image, searchRegion: Float = 15f,
        baseX: Float = x, baseY: Float = y,
        mode: FindMode = FindMode.CENTROID, minHFD: Float = 1.5f,
    ): Boolean {
        var result = FindResult.OK

        var newX = baseX
        var newY = baseY

        run {
            val minX = 0
            val minY = 0
            val maxX = image.width - 1
            val maxY = image.height - 1

            var startX = max((x - searchRegion).toInt(), minX)
            var endX = min((x + searchRegion).toInt(), maxX)
            var startY = max((y - searchRegion).toInt(), minY)
            var endY = min((y + searchRegion).toInt(), maxY)

            // println("Star::Find startx=%d starty=%d endx=%d endy=%d".format(startX, startY, endX, endY))

            require(endX > startX && endY > startY) { "coordinates are invalid" }

            var peakX = 0
            var peakY = 0
            var peak = 0f
            val max3 = FloatArray(3)

            if (mode == FindMode.PEAK) {
                for (y in startY..endY) {
                    for (x in startX..endY) {
                        val p = image.readPixel(x, y) * 65535f

                        if (p > peak) {
                            peak = p
                            peakX = x
                            peakY = y
                        }
                    }
                }

                this.peak = peak
            } else {
                // Find the peak value within the search region
                // using a smoothing function also check for saturation.
                for (y in startY + 1 until endY) {
                    for (x in startX + 1 until endX) {
                        var p = image.readPixel(x, y) * 65535f

                        // TODO: Optimize this using stride instead of call readPixel
                        // and remove 65535 multiplication.
                        val pv = 4 * p + image.readPixel(x - 1, y - 1) * 65535f +
                                image.readPixel(x + 1, y - 1) * 65535f +
                                image.readPixel(x - 1, y + 1) * 65535f +
                                image.readPixel(x + 1, y + 1) * 65535f +
                                2 * image.readPixel(x + 0, y - 1) * 65535f +
                                2 * image.readPixel(x - 1, y + 0) * 65535f +
                                2 * image.readPixel(x + 1, y + 0) * 65535f +
                                2 * image.readPixel(x + 0, y + 1) * 65535f

                        // println("Star::Find p=%d val=%d".format(p.toInt(), pv.toInt()))

                        if (pv > peak) {
                            peak = pv
                            peakX = x
                            peakY = y
                            // println("Star::Find peak_val=%d peak_x=%d peak_y=%d".format(peak.toInt(), peakX, peakY))
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

                this.peak = max3[0]   // Raw peak value.
                peak /= 16 // Smoothed peak value.
                // println("Star::Find PeakVal=%d peak_val/16=%d".format(this.peak.toInt(), peak.toInt()))
            }

            // Center window around peak value.
            startX = max(peakX - B, minX)
            endX = min(peakX + B, maxX)
            startY = max(peakY - B, minY)
            endY = min(peakY + B, maxY)

            // println("Star::Find startx=%d starty=%d endx=%d endy=%d".format(startX, startY, endX, endY))

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

                    for (x in startX..endX) {
                        val dx = x - peakX
                        val r2 = dx * dx + dy2

                        // Exclude points not in annulus.
                        if (r2 <= A2 || r2 > B2) continue

                        val p = image.readPixel(x, y) * 65535f
                        // println("Star::Find val=%.4f x=%d y=%d".format(p, x, y))

                        if (i > 0 && (p < meanBg - 2f * sigmaBg || p > meanBg + 2f * sigmaBg)) continue

                        sum += p
                        nbg++

                        val a0 = a
                        a += (p - a) / nbg
                        q += (p - a0) * (p - a)
                        // println("Star::Find sum=%.4f a=%.4f q=%.4f nbg=%d".format(sum, a, q, nbg))
                    }
                }

                if (nbg < 10 && LOG.isDebugEnabled) {
                    LOG.debug("too few background points! nbg={} mean={} sigma={}", nbg, meanBg, sigmaBg)
                    break
                }

                prevMeanBg = meanBg
                meanBg = sum / nbg
                sigma2Bg = q / (nbg - 1)
                sigmaBg = sqrt(sigma2Bg)

                // val msg = "Star::Find iter=%d prev_mean_bg=%.4f mean_bg=%.4f sigma2_bg=%.4f sigma_bg=%.4f"
                // println(msg.format(i, prevMeanBg, meanBg, sigma2Bg, sigmaBg))

                if (i > 0 && abs(meanBg - prevMeanBg) < 0.5) break
            }

            val thresh: Float

            var cx = 0f
            var cy = 0f
            var mass = 0f
            var n: Int

            val hfrvec = ArrayList<R2M>()

            if (mode == FindMode.PEAK) {
                mass = peak
                n = 1
                thresh = 0f
            } else {
                thresh = truncate(meanBg + 3f * sigmaBg + 0.5f)

                // println("Star::Find thresh=%.4f".format(thresh))

                // Find pixels over threshold within aperture; compute mass and centroid.
                startX = max(peakX - A, minX)
                endX = min(peakX + A, maxX)
                startY = max(peakY - A, minY)
                endY = min(peakY + A, maxY)

                // println("Star::Find startx=%d starty=%d endx=%d endy=%d".format(startX, startY, endX, endY))

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
                        val p = image.readPixel(x, y) * 65535f

                        if (p < thresh) continue

                        // println("Star::Find val=%f x=%d y=%d".format(p, x, y))

                        val d = p - meanBg

                        cx += dx * d
                        cy += dy * d
                        mass += d
                        n++

                        // println("Star::Find cx=%.4f cy=%.4f mass=%.4f n=%d".format(cx, cy, mass, n))

                        hfrvec.add(R2M(x.toFloat(), y.toFloat(), d))
                    }
                }
            }

            this.mass = mass

            // SNR estimate from: Measuring the Signal-to-Noise Ratio S/N of the CCD Image of a Star or Nebula, J.H.Simonetti, 2004 January 8
            // http://www.phys.vt.edu/~jhs/phys3154/snr20040108.pdf
            snr = if (n > 0) mass / sqrt(mass / 0.5f + sigma2Bg * n * (1f + 1f / nbg)) else 0f

            // println("Star::SNR=%.4f".format(snr))

            // A few scattered pixels over threshold can give a false positive
            // avoid this by requiring the smoothed peak value to be above the threshold.
            if (peak <= thresh && snr >= LOW_SNR) {
                LOG.debug("false star n={} nbg={} bg={} sigma={} thresh={} peak={}", n, nbg, meanBg, sigmaBg, thresh, peak)
                snr = LOW_SNR - 0.1f
            }

            if (mass < 10f) {
                hfd = 0f
                result = FindResult.LOWMASS
                return@run
            }

            if (snr < LOW_SNR) {
                hfd = 0f
                result = FindResult.LOWSNR
                return@run
            }

            newX = peakX + cx / mass
            newY = peakY + cy / mass

            hfd = 2f * HalfFluxRadius.compute(newX, newY, mass, hfrvec)

            if (hfd < minHFD && mode != FindMode.PEAK) {
                result = FindResult.LOWHFD
                return@run
            }

            val mx = max3[0]

            // check for saturation.
            if (mx >= 65535f) {
                result = FindResult.SATURATED
                return@run
            }

            // maxADU not known, use the "flat-top" hueristic
            // even at saturation, the max values may vary a bit due to noise
            // Call it saturated if the the top three values are within 32 parts per 65535 of max for 16-bit cameras,
            // or within 1 part per 191 for 8-bit cameras
            // val d = max3[0] - max3[2]

            // if (d * 65535f < 32f * mx) {
            //     result = FindResult.SATURATED
            //     return@run
            // }
        }

        x = newX
        y = newY
        lastFindResult = result

        if (LOG.isDebugEnabled) {
            LOG.debug("Find returns {} ({}), X={}, Y={}, Mass={}, SNR={}, Peak={} HFD={}", wasFound, result, x, y, mass, snr, peak, hfd)
        }

        return wasFound
    }

    override fun invalidate() {
        mass = 0f
        snr = 0f
        hfd = 0f
        lastFindResult = FindResult.ERROR
        super.invalidate()
    }

    inline val wasFound
        get() = isValid && lastFindResult.wasFound

    override fun toString(): String {
        return "Star(x=$x, y=$y," +
                " mass=$mass, snr=$snr, hfd=$hfd, peak=$peak," +
                " lastFindResult=$lastFindResult)"
    }

    companion object {

        private const val A = 7   // Inner radius.
        private const val B = 12  // outer radius.
        private const val A2 = A * A
        private const val B2 = B * B

        private const val LOW_SNR = 3f

        @JvmStatic private val LOG = LoggerFactory.getLogger(Star::class.java)
    }
}
