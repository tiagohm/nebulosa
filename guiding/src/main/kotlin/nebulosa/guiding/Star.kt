package nebulosa.guiding

import nebulosa.imaging.Image
import org.slf4j.LoggerFactory
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

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

    init {
        invalidate()

        // Star is a bit quirky in that we use X and Y after the star is invalidated.
        x = 0f
        y = 0f
    }

    fun find(
        image: Image, searchRegion: Int,
        mode: FindMode, minHfd: Double,
    ) {

    }

    fun find(
        image: Image, searchRegion: Int,
        baseX: Int, baseY: Int,
        mode: FindMode, minHfd: Double,
    ): Boolean {
        var result = FindResult.OK
        var newX = baseX.toFloat()
        var newY = baseY.toFloat()

        val res = runCatching {
            val minX = 0
            val minY = 0
            val maxX = image.width - 1
            val maxY = image.height - 1

            var startX = max(baseX - searchRegion, minX)
            var endX = min(baseX + searchRegion, maxX)
            var startY = max(baseY - searchRegion, minY)
            var endY = min(baseY + searchRegion, maxY)

            require(endX > startX && endY > startY) { "coordinates are invalid" }

            var peakX = 0
            var peakY = 0
            var peak = 0f
            val max3 = FloatArray(3)

            if (mode == FindMode.PEAK) {
                for (y in startY..endY) {
                    for (x in startX..endY) {
                        val p = image.readPixel(x, y)

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
                    for (x in startX + 1 until endY) {
                        var p = image.readPixel(x, y)

                        // TODO: Optimize this using stride instead of call readPixel.
                        val pv = 4 * p + image.readPixel(y - 1, x - 1) +
                                image.readPixel(y - 1, x + 1) +
                                image.readPixel(y + 1, x - 1) +
                                image.readPixel(y + 1, x + 1) +
                                2 * image.readPixel(y - 1, x + 0) +
                                2 * image.readPixel(y + 0, x - 1) +
                                2 * image.readPixel(y + 0, x + 1) +
                                2 * image.readPixel(y + 1, x + 0)

                        if (pv > peak) {
                            peak = p
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

                this.peak = max3[0]   // Raw peak value.
                peak /= 16 // Smoothed peak value.
            }

            // Center window around peak value.
            startX = max(peakX - B, minX)
            endX = min(peakX + B, maxX)
            startY = max(peakY - B, minY)
            endY = min(peakY + B, maxY)

            // Find the mean and stdev of the background.
            var nbg = 0
            var meanBg = 0f
            var prevMeanBg = 0f
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

                        val p = image.readPixel(x, y)

                        if (i > 0 && (p < meanBg - 2.0 * sigmaBg || p > meanBg + 2.0 * sigmaBg)) continue

                        sum += p
                        nbg++

                        val a0 = a
                        a += (p - a) / nbg
                        q += (p - a0) * (p - a)
                    }
                }

                if (nbg < 10 && LOG.isDebugEnabled) {
                    // Only possible after the first iteration.
                    LOG.debug("too few background points! nbg={} mean={} sigma={}", nbg, meanBg, sigmaBg)
                    break
                }

                prevMeanBg = meanBg
                meanBg = sum / nbg
                sigma2Bg = q / (nbg - 1)
                sigmaBg = sqrt(sigma2Bg)

                if (i > 0 && abs(meanBg - prevMeanBg) < 0.5) break
            }

            var thresh = 0

            var cx = 0f
            var cy = 0f
            var mass = 0f
            var n = 0

            val hfrvec = ArrayList<R2M>()

            if (mode == FindMode.PEAK) {
                mass = peak
                n = 1
                thresh = 0
            } else {
                thresh = (meanBg + 3.0 * sigmaBg + 0.5).toInt()

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
                        if (dx * dx + dy2 > A2) continue;

                        // Exclude points below threshold.
                        val p = image.readPixel(x, y)

                        if (p < thresh) continue

                        val d = p - meanBg

                        cx += dx * d
                        cy += dy * d
                        mass += d
                        n++

                        hfrvec.add(R2M(x, y, d))
                    }
                }
            }

            this.mass = mass

            // SNR estimate from: Measuring the Signal-to-Noise Ratio S/N of the CCD Image of a Star or Nebula, J.H.Simonetti, 2004 January 8
            // http://www.phys.vt.edu/~jhs/phys3154/snr20040108.pdf
            snr = if (n > 0) mass / sqrt(mass / 0.5f + sigma2Bg * n * (1f + 1f / nbg)) else 0f

            // A few scattered pixels over threshold can give a false positive
            // avoid this by requiring the smoothed peak value to be above the threshold.
            if (peak <= thresh && snr >= LOW_SNR) {
                LOG.debug("false star n={} nbg={} bg={} sigma={} thresh={} peak={}", n, nbg, meanBg, sigmaBg, thresh, peak)
                snr = LOW_SNR - 0.1f
            }

            if (mass < 10f) {
                hfd = 0f
                result = FindResult.LOWMASS
                return@runCatching
            }

            if (snr < LOW_SNR) {
                hfd = 0f
                result = FindResult.LOWSNR
                return@runCatching
            }

            newX = peakX + cx / mass
            newY = peakY + cy / mass

            hfd = 2f * HalfFluxRadius.compute(newX, newY, mass, hfrvec)

            if (hfd < minHfd && mode != FindMode.PEAK) {
                result = FindResult.LOWHFD
                return@runCatching
            }

            // TODO: Check for saturation.
        }

        if (res.isFailure && result == FindResult.OK) {
            result = FindResult.ERROR
        }

        x = newX
        y = newY
        valid = true
        lastFindResult = result

        val wasFound = result == FindResult.OK || result == FindResult.SATURATED

        if (!valid || result == FindResult.ERROR) {
            mass = 0f
            snr = 0f
            hfd = 0f
        }

        if (LOG.isDebugEnabled) {
            LOG.debug("Find returns {} ({}), X={}, Y={}, Mass={}, SNR={}, Peak={} HFD={}", wasFound, Result, newX, newY, mass, snr, peak, hfd)
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

    companion object {

        private const val A = 7   // Inner radius.
        private const val B = 12  // outer radius.
        private const val A2 = A * A
        private const val B2 = B * B

        private const val LOW_SNR = 3f

        @JvmStatic private val LOG = LoggerFactory.getLogger(Star::class.java)
    }
}
