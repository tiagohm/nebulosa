package nebulosa.imaging.algorithms.star.detection

import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.*
import nebulosa.imaging.algorithms.star.hfd.FindResult
import nebulosa.imaging.algorithms.star.hfd.HalfFluxDiameter
import nebulosa.imaging.algorithms.star.hfd.ImageStar
import nebulosa.log.loggerFor
import kotlin.math.hypot

class HFDStarDetector(private val sigma: Double = 1.5) : StarDetector {

    override fun detectStars(image: Image): Collection<DetectedStar> {
        val transformedImage = if (image.mono) image.mono() else image.transform(GRAYSCALE)

        lateinit var kernel: StarFinderKernel

        val convolvedImage = transformedImage
            .transform(AutoScreenTransformFunction)
            .let { Statistics(noSumOfSquares = true).compute(it) to it }
            .also { LOG.info("${it.first}") }
            .also { kernel = StarFinderKernel(1.0 / it.first.median, sigma = sigma, normalize = false) }
            .also { it.second.transform(Convolution(kernel)) }
            .also { it.second.transform(Binarize(it.first.maximum - it.first.stdDev - it.first.variance)) }
            // .also { it.second.transform(Binarize(it.first.median + it.first.stdDev + MedianDeviation(it.first.median).compute(it.second))) }
            .second

        val imageStars = HashSet<ImageStar>()
        val offsetX = kernel.xSize / 2
        val offsetY = kernel.ySize / 2

        for (y in offsetY until convolvedImage.height - offsetY) {
            for (x in offsetX until convolvedImage.width - offsetX) {
                val pixel = convolvedImage.readRed(x, y)

                if (pixel == 1f) {
                    if (imageStars.any { hypot(it.x - x, it.y - y) <= 15.0 }) continue
                    val star = HalfFluxDiameter(x.toDouble(), y.toDouble()).compute(image)
                    if (imageStars.any { hypot(it.x - star.x, it.y - star.y) <= 5.0 }) continue
                    star.result == FindResult.OK && star.peak > 0.0 && star.hfd > 0.0 && imageStars.add(star)
                }
            }
        }

        LOG.info("detected {} stars", imageStars.size)

        return imageStars
    }

    companion object {

        private const val GRAYSCALE_FACTOR = 1f / 3

        @JvmStatic private val GRAYSCALE = Grayscale(GRAYSCALE_FACTOR, GRAYSCALE_FACTOR, GRAYSCALE_FACTOR)
        @JvmStatic private val LOG = loggerFor<HFDStarDetector>()
    }
}
