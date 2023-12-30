import io.kotest.matchers.shouldBe
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.transformation.AutoScreenTransformFunction
import nebulosa.imaging.algorithms.transformation.correction.BiasSubtraction
import nebulosa.imaging.algorithms.transformation.correction.DarkSubtraction
import nebulosa.imaging.algorithms.transformation.correction.FlatCorrection
import nebulosa.test.FitsStringSpec

class CalibrationTest : FitsStringSpec() {

    init {
        unzip("UNCALIBRATED")

        val dark = Image.open(DARK)
        val flat = Image.open(FLAT)
        val bias = Image.open(BIAS)

        "flat correction" {
            val calibrated = Image.open(UNCALIBRATED).transform(FlatCorrection(flat))
            calibrated.transform(AutoScreenTransformFunction).save("FLAT_CALIBRATED").second shouldBe "3b1b12688a403c2f6bc75da1ae1effb1"
        }
        "bias subtraction" {
            val calibrated = Image.open(UNCALIBRATED).transform(BiasSubtraction(bias))
            calibrated.transform(AutoScreenTransformFunction).save("BIAS_CALIBRATED").second shouldBe "9aed2fe876e23fcd3ab696ca6775ce43"
        }
        "dark subtraction" {
            val calibrated = Image.open(UNCALIBRATED).transform(DarkSubtraction(dark))
            calibrated.transform(AutoScreenTransformFunction).save("DARK_CALIBRATED").second shouldBe "56d8016e719b34a3f43d78f7491e5b41"
        }
        "dark subtraction + flat correction" {
            val calibrated = Image.open(UNCALIBRATED).transform(DarkSubtraction(dark), FlatCorrection(flat))
            calibrated.transform(AutoScreenTransformFunction).save("DARK_FLAT_CALIBRATED").second shouldBe "2a8c5318f0528f48bbff3e6dad07a40d"
        }
        "dark subtraction + bias correction" {
            val calibrated = Image.open(UNCALIBRATED).transform(DarkSubtraction(dark), BiasSubtraction(bias))
            calibrated.transform(AutoScreenTransformFunction).save("DARK_BIAS_CALIBRATED").second shouldBe "e77f79aab0e6d40b227c2fbb31ed9f0c"
        }
        "bias subtraction + flat correction" {
            val calibrated = Image.open(UNCALIBRATED).transform(BiasSubtraction(bias), FlatCorrection(flat))
            calibrated.transform(AutoScreenTransformFunction).save("BIAS_FLAT_CALIBRATED").second shouldBe "ac06be67b562706139bd0ae7b6952e42"
        }
        "bias subtraction + dark subtraction + flat correction" {
            val calibrated = Image.open(UNCALIBRATED).transform(BiasSubtraction(bias), DarkSubtraction(dark), FlatCorrection(flat))
            calibrated.transform(AutoScreenTransformFunction).save("BIAS_DARK_FLAT_CALIBRATED").second shouldBe "20ae0816e32de59ddc94d5a54211d8af"
        }
    }
}
