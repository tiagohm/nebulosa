package nebulosa.imaging.algorithms.star.detection

import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.Convolution
import nebulosa.imaging.algorithms.Grayscale
import nebulosa.imaging.algorithms.SigmaClip
import nebulosa.imaging.algorithms.Statistics
import java.io.File
import javax.imageio.ImageIO

class HFDStarDetector(private val sigma: Double = 3.0) : StarDetector {

    override fun detectStars(image: Image): List<DetectedImage> {
        val grayImage = if (image.mono) image.clone() else image.transform(GRAYSCALE)
        val starFinderKernel = StarFinderKernel(sigma)
        val convolvedImage = grayImage.transform(SigmaClip(sigma = sigma, noStatistics = true), Convolution(starFinderKernel))
        val convolvedStats = Statistics(noDeviation = true, noMedian = true, noSumOfSquares = true).compute(convolvedImage)
        var i = 0

        val outputFile = File("src/test/resources/ZZZ.png")
        ImageIO.write(convolvedImage, "PNG", outputFile)

        for (y in starFinderKernel.ySize / 2 until convolvedImage.height - starFinderKernel.ySize / 2) {
            for (x in starFinderKernel.ySize / 2 until convolvedImage.width - starFinderKernel.ySize / 2) {
                val pixel = convolvedImage.readGray(i)

                if (pixel > 0.5) {
                    println("$x, $y")
                }

                i++
            }
        }

        return emptyList()
    }

    companion object {

        private const val GRAYSCALE_FACTOR = 1f / 3

        @JvmStatic private val GRAYSCALE = Grayscale(GRAYSCALE_FACTOR, GRAYSCALE_FACTOR, GRAYSCALE_FACTOR)
    }
}
