import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.image.Image.Companion.asImage
import nebulosa.image.algorithms.transformation.*
import nebulosa.image.algorithms.transformation.convolution.*
import nebulosa.image.format.ImageChannel
import nebulosa.test.DEBAYER_FITS
import nebulosa.test.M82_COLOR_32_XISF
import nebulosa.test.M82_MONO_8_XISF
import nebulosa.test.save
import nebulosa.xisf.xisf
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class XisfTransformAlgorithmTest {

    @Test
    fun monoRaw() {
        val mImage = M82_MONO_8_XISF.xisf().asImage()
        mImage.save("xisf-mono-raw").second shouldBe "0dca7efedef5b3525f8037f401518b0b"
    }

    @Test
    fun monoVerticalFlip() {
        val mImage = M82_MONO_8_XISF.xisf().asImage()
        mImage.transform(VerticalFlip)
        mImage.save("xisf-mono-vertical-flip").second shouldBe "d8250e5ac45d9a6ec04bed2a583a70b2"
    }

    @Test
    fun monoHorizontalFlip() {
        val mImage = M82_MONO_8_XISF.xisf().asImage()
        mImage.transform(HorizontalFlip)
        mImage.save("xisf-mono-horizontal-flip").second shouldBe "9e2a05c331a5daeff20671a365e8b74c"
    }

    @Test
    fun monoVerticalAndHorizontalFlip() {
        val mImage = M82_MONO_8_XISF.xisf().asImage()
        mImage.transform(VerticalFlip, HorizontalFlip)
        mImage.save("xisf-mono-vertical-horizontal-flip").second shouldBe "c7d55e870850c10619f389e3f9ec1243"
    }

    @Test
    fun monoSubframe() {
        val mImage = M82_MONO_8_XISF.xisf().asImage()
        val nImage = mImage.transform(SubFrame(45, 70, 16, 16))
        nImage.width shouldBeExactly 16
        nImage.height shouldBeExactly 16
        nImage.mono.shouldBeTrue()
        nImage.save("xisf-mono-subframe").second shouldBe "dc0ca19c5d40f90e4daac87fbab8f449"
    }

    @Test
    fun monoSharpen() {
        val mImage = M82_MONO_8_XISF.xisf().asImage()
        mImage.transform(Sharpen)
        mImage.save("xisf-mono-sharpen").second shouldBe "08421f6afef422cdaa9f464a860808ce"
    }

    @Test
    fun monoMean() {
        val mImage = M82_MONO_8_XISF.xisf().asImage()
        mImage.transform(Mean)
        mImage.save("xisf-mono-mean").second shouldBe "4127027d467ce9537049dc5f25f23ede"
    }

    @Test
    fun monoInvert() {
        val mImage = M82_MONO_8_XISF.xisf().asImage()
        mImage.transform(Invert)
        mImage.save("xisf-mono-invert").second shouldBe "a6ec4a55225cb18f004e159a60137fe9"
    }

    @Test
    fun monoEmboss() {
        val mImage = M82_MONO_8_XISF.xisf().asImage()
        mImage.transform(Emboss)
        mImage.save("xisf-mono-emboss").second shouldBe "2f07b3964b52f6b141b8cc45639b2287"
    }

    @Test
    fun monoEdges() {
        val mImage = M82_MONO_8_XISF.xisf().asImage()
        mImage.transform(Edges)
        mImage.save("xisf-mono-edges").second shouldBe "897af72d536bca57cce77f09e396adb7"
    }

    @Test
    fun monoBlur() {
        val mImage = M82_MONO_8_XISF.xisf().asImage()
        mImage.transform(Blur)
        mImage.save("xisf-mono-blur").second shouldBe "5a850ef20c0ebd461e676523823ff6ea"
    }

    @Test
    fun monoGaussianBlur() {
        val mImage = M82_MONO_8_XISF.xisf().asImage()
        mImage.transform(GaussianBlur(sigma = 5.0, size = 9))
        mImage.save("xisf-mono-gaussian-blur").second shouldBe "cb7489cb1948e802ea4d92bba24e0739"
    }

    @Test
    fun monoStfMidTone01Shadow00Highlight10() {
        val mImage = M82_MONO_8_XISF.xisf().asImage()
        mImage.transform(ScreenTransformFunction(0.1f))
        mImage.save("xisf-mono-stf-01-00-10").second shouldBe "91621dba5a58081347d156f2152bd8c6"
    }

    @Test
    fun monoStfMidTone09Shadow00Highlight10() {
        val mImage = M82_MONO_8_XISF.xisf().asImage()
        mImage.transform(ScreenTransformFunction(0.9f))
        mImage.save("xisf-mono-stf-09-00-10").second shouldBe "9a67728cfc4f871ea8ab6557f381949e"
    }

    @Test
    fun monoStfMidTone01Shadow05Highlight10() {
        val mImage = M82_MONO_8_XISF.xisf().asImage()
        mImage.transform(ScreenTransformFunction(0.1f, shadow = 0.5f))
        mImage.save("xisf-mono-stf-01-05-10").second shouldBe "c70b9caaaf88bba9c64891a2b3ba8033"
    }

    @Test
    fun monoStfMidTone09Shadow05Highlight10() {
        val mImage = M82_MONO_8_XISF.xisf().asImage()
        mImage.transform(ScreenTransformFunction(0.9f, shadow = 0.5f))
        mImage.save("xisf-mono-stf-09-05-10").second shouldBe "4f4c49b96bd9aa417f22c59b8224ea90"
    }

    @Test
    fun monoStfMidTone01Shadow00Highlight05() {
        val mImage = M82_MONO_8_XISF.xisf().asImage()
        mImage.transform(ScreenTransformFunction(0.1f, highlight = 0.5f))
        mImage.save("xisf-mono-stf-01-00-05").second shouldBe "b0c9598e3003d72a5fba617c30dc1821"
    }

    @Test
    fun monoStfMidTone09Shadow00Highlight05() {
        val mImage = M82_MONO_8_XISF.xisf().asImage()
        mImage.transform(ScreenTransformFunction(0.9f, highlight = 0.5f))
        mImage.save("xisf-mono-stf-09-00-05").second shouldBe "5824ac8c2d44c2c8ff8121e0904bb77b"
    }

    @Test
    fun monoStfMidTone01Shadow04Highlight06() {
        val mImage = M82_MONO_8_XISF.xisf().asImage()
        mImage.transform(ScreenTransformFunction(0.1f, 0.4f, 0.6f))
        mImage.save("xisf-mono-stf-01-04-06").second shouldBe "20417dfc43fc4c5cb425875614d8066c"
    }

    @Test
    fun monoStfMidTone09Shadow04Highlight06() {
        val mImage = M82_MONO_8_XISF.xisf().asImage()
        mImage.transform(ScreenTransformFunction(0.9f, 0.4f, 0.6f))
        mImage.save("xisf-mono-stf-09-04-06").second shouldBe "13d13ad164a952a29f7ad76e1e51e7d0"
    }

    @Test
    fun monoAutoStf() {
        val mImage = M82_MONO_8_XISF.xisf().asImage()
        mImage.transform(AutoScreenTransformFunction)
        mImage.save("xisf-mono-auto-stf").second shouldBe "9204a71df3770e8fe5ca49e3420eed72"
    }

    @Test
    fun colorRaw() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.save("xisf-color-raw").second shouldBe "764e326cc5260d81f3761112ad6a1969"
    }

    @Test
    fun colorVerticalFlip() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(VerticalFlip)
        mImage.save("xisf-color-vertical-flip").second shouldBe "595d8d3e46e91d7adf7591e591a7a98d"
    }

    @Test
    fun colorHorizontalFlip() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(HorizontalFlip)
        mImage.save("xisf-color-horizontal-flip").second shouldBe "b3d9609ef88db9b9215b4e21fdd4992a"
    }

    @Test
    fun colorVerticalAndHorizontalFlip() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(VerticalFlip, HorizontalFlip)
        mImage.save("xisf-color-vertical-horizontal-flip").second shouldBe "1c9be1a7e5a66a989f6d0715c7ff30e4"
    }

    @Test
    fun colorSubframe() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        val nImage = mImage.transform(SubFrame(45, 70, 16, 16))
        nImage.width shouldBeExactly 16
        nImage.height shouldBeExactly 16
        nImage.mono.shouldBeFalse()
        nImage.save("xisf-color-subframe").second shouldBe "dcf5bc8908ea6a32a5e98f14152f47b9"
    }

    @Test
    fun colorSharpen() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(Sharpen)
        mImage.save("xisf-color-sharpen").second shouldBe "1017a88ff2caeb83a650d94d96c59347"
    }

    @Test
    fun colorMean() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(Mean)
        mImage.save("xisf-color-mean").second shouldBe "eb948cab18442d008381f392bf43542a"
    }

    @Test
    fun colorInvert() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(Invert)
        mImage.save("xisf-color-invert").second shouldBe "bbc97cdcde240873439bc29ff1adf169"
    }

    @Test
    fun colorEmboss() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(Emboss)
        mImage.save("xisf-color-emboss").second shouldBe "fba6827f204df93f4aaf2f99b113a8f7"
    }

    @Test
    fun colorEdges() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(Edges)
        mImage.save("xisf-color-edges").second shouldBe "71d6b5c936ab8165c7c1b63514d2da7b"
    }

    @Test
    fun colorBlur() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(Blur)
        mImage.save("xisf-color-blur").second shouldBe "5c9f738a309fc86956d124f0f109eea2"
    }

    @Test
    fun colorGaussianBlur() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(GaussianBlur(sigma = 5.0, size = 9))
        mImage.save("xisf-color-gaussian-blur").second shouldBe "648beabcea0b486efc3f0c4ded632a06"
    }

    @Test
    fun colorStfMidTone01Shadow00Highlight10() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(ScreenTransformFunction(0.1f))
        mImage.save("xisf-color-stf-01-00-10").second shouldBe "d84577785e44179f7af7c00807be5260"
    }

    @Test
    fun colorStfMidTone09Shadow00Highlight10() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(ScreenTransformFunction(0.9f))
        mImage.save("xisf-color-stf-09-00-10").second shouldBe "11771bfe0e180e1efea4568902109013"
    }

    @Test
    fun colorStfMidTone01Shadow05Highlight10() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(ScreenTransformFunction(0.1f, shadow = 0.5f))
        mImage.save("xisf-color-stf-01-05-10").second shouldBe "4e3b204d8a7ed06dfd05b7d010ef267b"
    }

    @Test
    fun colorStfMidTone09Shadow05Highlight10() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(ScreenTransformFunction(0.9f, shadow = 0.5f))
        mImage.save("xisf-color-stf-09-05-10").second shouldBe "95c5b801978abdbb51afc9d0de12c218"
    }

    @Test
    fun colorStfMidTone01Shadow00Highlight05() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(ScreenTransformFunction(0.1f, highlight = 0.5f))
        mImage.save("xisf-color-stf-01-00-05").second shouldBe "08322042c9e57102fec77a06b28c263d"
    }

    @Test
    fun colorStfMidTone09Shadow00Highlight05() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(ScreenTransformFunction(0.9f, highlight = 0.5f))
        mImage.save("xisf-color-stf-09-00-05").second shouldBe "c99990d3a4bc6fc3aa7a8ac90d452419"
    }

    @Test
    fun colorStfMidTone01Shadow04Highlight06() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(ScreenTransformFunction(0.1f, 0.4f, 0.6f))
        mImage.save("xisf-color-stf-01-04-06").second shouldBe "33bce2d7f7cde67d0ee618439f438ee7"
    }

    @Test
    fun colorStfMidTone09Shadow04Highlight06() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(ScreenTransformFunction(0.9f, 0.4f, 0.6f))
        mImage.save("xisf-color-stf-09-04-06").second shouldBe "923e56840bcea4462160e36f2e801f5e"
    }

    @Test
    fun colorAutoStf() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(AutoScreenTransformFunction)
        mImage.save("xisf-color-auto-stf").second shouldBe "afe2fd8e21b042389ff5eb1d1abffb08"
    }

    @Test
    fun colorScnrMaximumMask() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(SubtractiveChromaticNoiseReduction(ImageChannel.RED, 1f, ProtectionMethod.MAXIMUM_MASK))
        mImage.save("xisf-color-scnr-maximum-mask").second shouldBe "e465a2fc0814055e089a3180c0235c8b"
    }

    @Test
    fun colorScnrAdditiveMask() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(SubtractiveChromaticNoiseReduction(ImageChannel.RED, 1f, ProtectionMethod.ADDITIVE_MASK))
        mImage.save("xisf-color-scnr-additive-mask").second shouldBe "1016ec07253f869097d2d9c67194b3e3"
    }

    @Test
    fun colorScnrAverageNeutral() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(SubtractiveChromaticNoiseReduction(ImageChannel.RED, 1f, ProtectionMethod.AVERAGE_NEUTRAL))
        mImage.save("xisf-color-scnr-average-neutral").second shouldBe "2f2583bfb2ae4a9e4441dc9b827196f7"
    }

    @Test
    fun colorScnrMaximumNeutral() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(SubtractiveChromaticNoiseReduction(ImageChannel.RED, 1f, ProtectionMethod.MAXIMUM_NEUTRAL))
        mImage.save("xisf-color-scnr-maximum-neutral").second shouldBe "dabd995db4fba052617b148b3700378d"
    }

    @Test
    fun colorScnrMinimumNeutral() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        mImage.transform(SubtractiveChromaticNoiseReduction(ImageChannel.RED, 1f, ProtectionMethod.MINIMUM_NEUTRAL))
        mImage.save("xisf-color-scnr-minimum-neutral").second shouldBe "b92e5130ec2a44c0afd1ca3782261e47"
    }

    @Test
    fun colorGrayscaleBt709() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        val nImage = mImage.transform(Grayscale.BT709)
        nImage.save("xisf-color-grayscale-bt709").second shouldBe "bf78482866a0b3a216fa5262f28b0e5e"
    }

    @Test
    fun colorGrayscaleRmy() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        val nImage = mImage.transform(Grayscale.RMY)
        nImage.save("xisf-color-grayscale-rmy").second shouldBe "e75085eea073811aef41624aab318b48"
    }

    @Test
    fun colorGrayscaleY() {
        val mImage = M82_COLOR_32_XISF.xisf().asImage()
        val nImage = mImage.transform(Grayscale.Y)
        nImage.save("xisf-color-grayscale-y").second shouldBe "7a2bef966d460742533a1c8c3a74f1c5"
    }

    @Test
    @Disabled
    fun colorDebayer() {
        val mImage = DEBAYER_FITS.xisf().asImage()
        val nImage = mImage.transform(AutoScreenTransformFunction)
        nImage.save("xisf-color-debayer").second shouldBe "86b5bdd67dfd6bbf5495afae4bf2bc04"
    }

    @Test
    @Disabled
    fun colorNoDebayer() {
        val mImage = DEBAYER_FITS.xisf().asImage(false)
        val nImage = mImage.transform(AutoScreenTransformFunction)
        nImage.save("xisf-color-no-debayer").second shouldBe "958ccea020deec1f0c075042a9ba37c3"
    }
}
