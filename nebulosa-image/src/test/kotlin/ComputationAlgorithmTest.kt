import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.floats.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.fits.fits
import nebulosa.image.Image
import nebulosa.image.algorithms.computation.MedianAbsoluteDeviation
import nebulosa.image.algorithms.computation.Statistics
import nebulosa.image.format.ImageChannel
import nebulosa.test.FitsStringSpec

class ComputationAlgorithmTest : FitsStringSpec() {

    init {
        "mono:median absolute deviation" {
            val mImage = Image.open(NGC3344_MONO_F32_FITS.fits())
            mImage.compute(MedianAbsoluteDeviation()) shouldBe (0.0862f plusOrMinus 1e-4f)
        }
        "mono:statistics" {
            val mImage = Image.open(NGC3344_MONO_F32_FITS.fits())
            val statistics = mImage.compute(Statistics.GRAY)

            statistics.count shouldBeExactly 65536
            statistics.maxCount shouldBeExactly 926
            statistics.mean shouldBe (0.2848f plusOrMinus 1e-4f)
            statistics.sumOfSquares shouldBe (6827.543f plusOrMinus 1e-3f)
            statistics.median shouldBe (0.2470f plusOrMinus 1e-4f)
            statistics.variance shouldBe (0.02302f plusOrMinus 1e-4f)
            statistics.stdDev shouldBe (0.1517f plusOrMinus 1e-4f)
            statistics.avgDev shouldBe (0.11553f plusOrMinus 1e-5f)
            statistics.minimum shouldBeExactly 0.03529412f
            statistics.maximum shouldBeExactly 1f
        }
        "color:median absolute deviation" {
            val cImage = Image.open(NGC3344_COLOR_F32_FITS.fits())
            cImage.compute(MedianAbsoluteDeviation(channel = ImageChannel.RED)) shouldBe (0.0823f plusOrMinus 1e-4f)
            cImage.compute(MedianAbsoluteDeviation(channel = ImageChannel.GREEN)) shouldBe (0.0745f plusOrMinus 1e-4f)
            cImage.compute(MedianAbsoluteDeviation(channel = ImageChannel.BLUE)) shouldBe (0.0705f plusOrMinus 1e-4f)
        }
        "color:statistics" {
            val cImage = Image.open(NGC3344_COLOR_F32_FITS.fits())

            run {
                val statistics = cImage.compute(Statistics.RED)

                statistics.count shouldBeExactly 65536
                statistics.maxCount shouldBeExactly 1027
                statistics.mean shouldBe (0.2843f plusOrMinus 1e-4f)
                statistics.sumOfSquares shouldBe (6723.677f plusOrMinus 1e-3f)
                statistics.median shouldBe (0.2470f plusOrMinus 1e-4f)
                statistics.variance shouldBe (0.0217f plusOrMinus 1e-4f)
                statistics.stdDev shouldBe (0.1474f plusOrMinus 1e-4f)
                statistics.avgDev shouldBe (0.11034f plusOrMinus 1e-5f)
                statistics.minimum shouldBeExactly 0.047058824f
                statistics.maximum shouldBeExactly 1f
            }

            run {
                val statistics = cImage.compute(Statistics.GREEN)

                statistics.count shouldBeExactly 65536
                statistics.maxCount shouldBeExactly 1181
                statistics.mean shouldBe (0.2635f plusOrMinus 1e-4f)
                statistics.sumOfSquares shouldBe (5845.3496f plusOrMinus 1e-3f)
                statistics.median shouldBe (0.2235f plusOrMinus 1e-4f)
                statistics.variance shouldBe (0.0197f plusOrMinus 1e-4f)
                statistics.stdDev shouldBe (0.1404f plusOrMinus 1e-4f)
                statistics.avgDev shouldBe (0.10392f plusOrMinus 1e-5f)
                statistics.minimum shouldBeExactly 0.050980393f
                statistics.maximum shouldBeExactly 1f
            }

            run {
                val statistics = cImage.compute(Statistics.BLUE)

                statistics.count shouldBeExactly 65536
                statistics.maxCount shouldBeExactly 1234
                statistics.mean shouldBe (0.2619f plusOrMinus 1e-4f)
                statistics.sumOfSquares shouldBe (5615.849f plusOrMinus 1e-3f)
                statistics.median shouldBe (0.2235f plusOrMinus 1e-4f)
                statistics.variance shouldBe (0.0170f plusOrMinus 1e-4f)
                statistics.stdDev shouldBe (0.1306f plusOrMinus 1e-4f)
                statistics.avgDev shouldBe (0.09864f plusOrMinus 1e-5f)
                statistics.minimum shouldBeExactly 0.050980393f
                statistics.maximum shouldBeExactly 1f
            }
        }
    }
}
