import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.floats.shouldBeExactly
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.imaging.Image
import nebulosa.imaging.ImageChannel
import nebulosa.imaging.algorithms.Median
import nebulosa.imaging.algorithms.MedianDeviation
import nebulosa.imaging.algorithms.Statistics
import nebulosa.test.FitsStringSpec

class ComputationAlgorithmTest : FitsStringSpec() {

    init {
        "mono:median deviation" {
            val mImage = Image.openFITS(mF64bit)
            val median = mImage.compute(Median())
            mImage.compute(MedianDeviation(median)) shouldBe (0.0f plusOrMinus 1e-6f)
        }
        "mono:statistics" {
            val mImage = Image.openFITS(mF64bit)
            val statistics = mImage.compute(Statistics())

            statistics.count shouldBeExactly 64
            statistics.maxCount shouldBeExactly 39
            statistics.mean shouldBe (0.609375f plusOrMinus 1e-6f)
            statistics.sumOfSquares shouldBe (39f plusOrMinus 1e-6f)
            statistics.median shouldBe (1f plusOrMinus 1e-6f)
            statistics.variance shouldBe (0.241815f plusOrMinus 1e-6f)
            statistics.stdDev shouldBe (0.491747f plusOrMinus 1e-6f)
            statistics.avgDev shouldBe (0.390625f plusOrMinus 1e-6f)
            statistics.minimum shouldBeExactly 0f
            statistics.maximum shouldBeExactly 1f
        }
        "color:median deviation" {
            val cImage = Image.openFITS(cF64bit)
            val median = cImage.compute(Median())
            cImage.compute(MedianDeviation(median)) shouldBe (0.0f plusOrMinus 1e-6f)
        }
        "color:statistics" {
            val cImage = Image.openFITS(cF64bit)

            for (channel in ImageChannel.RGB) {
                val statistics = cImage.compute(Statistics(channel))

                statistics.count shouldBeExactly 64
                statistics.maxCount shouldBeExactly 45
                statistics.mean shouldBe (0.703125f plusOrMinus 1e-6f)
                statistics.sumOfSquares shouldBe (45f plusOrMinus 1e-6f)
                statistics.median shouldBe (1f plusOrMinus 1e-6f)
                statistics.variance shouldBe (0.212053f plusOrMinus 1e-6f)
                statistics.stdDev shouldBe (0.460492f plusOrMinus 1e-6f)
                statistics.avgDev shouldBe (0.296875f plusOrMinus 1e-6f)
                statistics.minimum shouldBeExactly 0f
                statistics.maximum shouldBeExactly 1f
            }
        }
    }
}
