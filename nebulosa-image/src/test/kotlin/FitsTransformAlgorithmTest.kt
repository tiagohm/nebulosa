import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.fits.fits
import nebulosa.image.Image.Companion.asImage
import nebulosa.image.algorithms.transformation.AutoScreenTransformFunction
import nebulosa.image.algorithms.transformation.Grayscale
import nebulosa.image.algorithms.transformation.HorizontalFlip
import nebulosa.image.algorithms.transformation.Invert
import nebulosa.image.algorithms.transformation.ProtectionMethod
import nebulosa.image.algorithms.transformation.ScreenTransformFunction
import nebulosa.image.algorithms.transformation.SubFrame
import nebulosa.image.algorithms.transformation.SubtractiveChromaticNoiseReduction
import nebulosa.image.algorithms.transformation.VerticalFlip
import nebulosa.image.algorithms.transformation.convolution.Blur
import nebulosa.image.algorithms.transformation.convolution.Edges
import nebulosa.image.algorithms.transformation.convolution.Emboss
import nebulosa.image.algorithms.transformation.convolution.GaussianBlur
import nebulosa.image.algorithms.transformation.convolution.Mean
import nebulosa.image.algorithms.transformation.convolution.Sharpen
import nebulosa.image.format.ImageChannel
import nebulosa.test.DEBAYER_FITS
import nebulosa.test.NGC3344_COLOR_32_FITS
import nebulosa.test.NGC3344_MONO_8_FITS
import nebulosa.test.save
import org.junit.jupiter.api.Test

class FitsTransformAlgorithmTest {

    @Test
    fun monoRaw() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.save("fits-mono-raw").second shouldBe "e17cfc29c3b343409cd8617b6913330e"
    }

    @Test
    fun monoVerticalFlip() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(VerticalFlip)
        mImage.save("fits-mono-vertical-flip").second shouldBe "262260dfe719726c0e7829a088279a21"
    }

    @Test
    fun monoHorizontalFlip() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(HorizontalFlip)
        mImage.save("fits-mono-horizontal-flip").second shouldBe "daf0f05db5de3750962f338527564b27"
    }

    @Test
    fun monoVerticalAndHorizontalFlip() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(VerticalFlip, HorizontalFlip)
        mImage.save("fits-mono-vertical-horizontal-flip").second shouldBe "3bc81f579a0e34ce9312c3b242209166"
    }

    @Test
    fun monoSubframe() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        val nImage = mImage.transform(SubFrame(45, 70, 16, 16))
        nImage.width shouldBeExactly 16
        nImage.height shouldBeExactly 16
        nImage.mono.shouldBeTrue()
        nImage.save("fits-mono-subframe").second shouldBe "4d9984e778f82dde10b9aeeee7a29fe0"
    }

    @Test
    fun monoSharpen() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(Sharpen)
        mImage.save("fits-mono-sharpen").second shouldBe "0b162242a4e673f6480b5206cf49ca50"
    }

    @Test
    fun monoMean() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(Mean)
        mImage.save("fits-mono-mean").second shouldBe "cf866292f657c379ae3965931dd8eeea"
    }

    @Test
    fun monoInvert() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(Invert)
        mImage.save("fits-mono-invert").second shouldBe "6e94463bb5b9561de1f0ee0a154db53e"
    }

    @Test
    fun monoEmboss() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(Emboss)
        mImage.save("fits-mono-emboss").second shouldBe "94a8ef5e4573e392d087cf10c905ba12"
    }

    @Test
    fun monoEdges() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(Edges)
        mImage.save("fits-mono-edges").second shouldBe "27ccd5f5e6098d0cae27e7495e18dd72"
    }

    @Test
    fun monoBlur() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(Blur)
        mImage.save("fits-mono-blur").second shouldBe "f2c5466dccf71b5c4bee86c5fbbb95fc"
    }

    @Test
    fun monoGaussianBlur() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(GaussianBlur(sigma = 5.0, size = 9))
        mImage.save("fits-mono-gaussian-blur").second shouldBe "69057b0c4461fb0d55b779da9e72fd69"
    }

    @Test
    fun monoStfMidtone01Shadow00Highlight10() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(ScreenTransformFunction(0.1f))
        mImage.save("fits-mono-stf-01-00-10").second shouldBe "22c0bd985e70a01330722d912869d6ee"
    }

    @Test
    fun monoStfMidtone09Shadow00Highlight10() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(ScreenTransformFunction(0.9f))
        mImage.save("fits-mono-stf-09-00-10").second shouldBe "553ccb7546dce3a8f742d5e8f7c58a3f"
    }

    @Test
    fun monoStfMidtone01Shadow05Highlight10() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(ScreenTransformFunction(0.1f, shadow = 0.5f))
        mImage.save("fits-mono-stf-01-05-10").second shouldBe "f31db854fab72033dce2f8c572ec6783"
    }

    @Test
    fun monoStfMidtone09Shadow05Highlight10() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(ScreenTransformFunction(0.9f, shadow = 0.5f))
        mImage.save("fits-mono-stf-09-05-10").second shouldBe "633b49c4a1dbb5ad8e6a9d74f330636d"
    }

    @Test
    fun monoStfMidtone01Shadow00Highlight05() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(ScreenTransformFunction(0.1f, highlight = 0.5f))
        mImage.save("fits-mono-stf-01-00-05").second shouldBe "26036937eb3e5f99cd6129f709ce4b31"
    }

    @Test
    fun monoStfMidtone09Shadow00Highlight05() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(ScreenTransformFunction(0.9f, highlight = 0.5f))
        mImage.save("fits-mono-stf-09-00-05").second shouldBe "e8f694dae666ac15ce2f8a169eb84024"
    }

    @Test
    fun monoStfMidtone01Shadow04Highlight06() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(ScreenTransformFunction(0.1f, 0.4f, 0.6f))
        mImage.save("fits-mono-stf-01-04-06").second shouldBe "5226aba21669a24f985703b3e7220568"
    }

    @Test
    fun monoStfMidtone09Shadow04Highlight06() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(ScreenTransformFunction(0.9f, 0.4f, 0.6f))
        mImage.save("fits-mono-stf-09-04-06").second shouldBe "c2acb25ef7be92a51f63e673ec9a850f"
    }

    @Test
    fun monoAutoStf() {
        val mImage = NGC3344_MONO_8_FITS.fits().asImage()
        mImage.transform(AutoScreenTransformFunction)
        mImage.save("fits-mono-auto-stf").second shouldBe "e17cfc29c3b343409cd8617b6913330e"
    }

    @Test
    fun colorRaw() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.save("fits-color-raw").second shouldBe "18fb83e240bc7a4cbafbc1aba2741db6"
    }

    @Test
    fun colorVerticalFlip() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(VerticalFlip)
        mImage.save("fits-color-vertical-flip").second shouldBe "b717ecda5c5bba50cfa06304ef2bca88"
    }

    @Test
    fun colorHorizontalFlip() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(HorizontalFlip)
        mImage.save("fits-color-horizontal-flip").second shouldBe "f70228600c77551473008ed4b9986439"
    }

    @Test
    fun colorVerticalAndHorizontalFlip() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(VerticalFlip, HorizontalFlip)
        mImage.save("fits-color-vertical-horizontal-flip").second shouldBe "1237314044f20307b76203148af855e3"
    }

    @Test
    fun colorSubframe() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        val nImage = mImage.transform(SubFrame(45, 70, 16, 16))
        nImage.width shouldBeExactly 16
        nImage.height shouldBeExactly 16
        nImage.mono.shouldBeFalse()
        nImage.save("fits-color-subframe").second shouldBe "282fc4fdf9142fcb4b18e1df1eef4caa"
    }

    @Test
    fun colorSharpen() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(Sharpen)
        mImage.save("fits-color-sharpen").second shouldBe "e562282bdafdeba6ce88981bb9c3ba61"
    }

    @Test
    fun colorMean() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(Mean)
        mImage.save("fits-color-mean").second shouldBe "a8380d928aaa756e202ba43bd3a2f207"
    }

    @Test
    fun colorInvert() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(Invert)
        mImage.save("fits-color-invert").second shouldBe "decad269ec26450aebeaf7546867b5f8"
    }

    @Test
    fun colorEmboss() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(Emboss)
        mImage.save("fits-color-emboss").second shouldBe "58d69250f1233055aa33f9ec7ca40af1"
    }

    @Test
    fun colorEdges() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(Edges)
        mImage.save("fits-color-edges").second shouldBe "091f2955740a8edcd2401dc416d19d51"
    }

    @Test
    fun colorBlur() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(Blur)
        mImage.save("fits-color-blur").second shouldBe "0fca440b763de5380fa29de736f3c792"
    }

    @Test
    fun colorGaussianBlur() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(GaussianBlur(sigma = 5.0, size = 9))
        mImage.save("fits-color-gaussian-blur").second shouldBe "394d1a4f136f15c802dd73004c421d64"
    }

    @Test
    fun colorStfMidtone01Shadow00Highlight10() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(ScreenTransformFunction(0.1f))
        mImage.save("fits-color-stf-01-00-10").second shouldBe "e952bd263df6fd275b9a80aca554cb4b"
    }

    @Test
    fun colorStfMidtone09Shadow00Highlight10() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(ScreenTransformFunction(0.9f))
        mImage.save("fits-color-stf-09-00-10").second shouldBe "038809d7612018e2e5c19d5e1f551abd"
    }

    @Test
    fun colorStfMidtone01Shadow05Highlight10() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(ScreenTransformFunction(0.1f, shadow = 0.5f))
        mImage.save("fits-color-stf-01-05-10").second shouldBe "70e812260f56f8621002327575611f31"
    }

    @Test
    fun colorStfMidtone09Shadow05Highlight10() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(ScreenTransformFunction(0.9f, shadow = 0.5f))
        mImage.save("fits-color-stf-09-05-10").second shouldBe "6ca400f617f466a9eb02a3a6f2985d99"
    }

    @Test
    fun colorStfMidtone01Shadow00Highlight05() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(ScreenTransformFunction(0.1f, highlight = 0.5f))
        mImage.save("fits-color-stf-01-00-05").second shouldBe "3cd98ee9a8949d5100295acccd77010b"
    }

    @Test
    fun colorStfMidtone09Shadow00Highlight05() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(ScreenTransformFunction(0.9f, highlight = 0.5f))
        mImage.save("fits-color-stf-09-00-05").second shouldBe "2cfeffc88c893cc5883d8a2221f29b91"
    }

    @Test
    fun colorStfMidtone01Shadow04Highlight06() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(ScreenTransformFunction(0.1f, 0.4f, 0.6f))
        mImage.save("fits-color-stf-01-04-06").second shouldBe "532a07a1a166eb007c2e40651aec2097"
    }

    @Test
    fun colorStfMidtone09Shadow04Highlight06() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(ScreenTransformFunction(0.9f, 0.4f, 0.6f))
        mImage.save("fits-color-stf-09-04-06").second shouldBe "eb3d940d9fd2c8814e930715e89897c4"
    }

    @Test
    fun colorAutoStf() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(AutoScreenTransformFunction)
        mImage.save("fits-color-auto-stf").second shouldBe "debc21729a90c0caed3ce43704297d90"
    }

    @Test
    fun colorScnrMaximumMask() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(SubtractiveChromaticNoiseReduction(ImageChannel.RED, 1f, ProtectionMethod.MAXIMUM_MASK))
        mImage.save("fits-color-scnr-maximum-mask").second shouldBe "e7d2155e18ff1e3172f4e849ae983145"
    }

    @Test
    fun colorScnrAdditiveMask() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(SubtractiveChromaticNoiseReduction(ImageChannel.RED, 1f, ProtectionMethod.ADDITIVE_MASK))
        mImage.save("fits-color-scnr-additive-mask").second shouldBe "a458c44cedcda704de16d80053fd87eb"
    }

    @Test
    fun colorScnrAverageNeutral() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(SubtractiveChromaticNoiseReduction(ImageChannel.RED, 1f, ProtectionMethod.AVERAGE_NEUTRAL))
        mImage.save("fits-color-scnr-average-neutral").second shouldBe "e07345ffc4982a62301c95c76d3efb35"
    }

    @Test
    fun colorScnrMaximumNeutral() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(SubtractiveChromaticNoiseReduction(ImageChannel.RED, 1f, ProtectionMethod.MAXIMUM_NEUTRAL))
        mImage.save("fits-color-scnr-maximum-neutral").second shouldBe "a1d4b04f57b001ba4a996bab0407fd7e"
    }

    @Test
    fun colorScnrMinimumNeutral() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        mImage.transform(SubtractiveChromaticNoiseReduction(ImageChannel.RED, 1f, ProtectionMethod.MINIMUM_NEUTRAL))
        mImage.save("fits-color-scnr-minimum-neutral").second shouldBe "8b7be57ff38da9c97b35d7888047c0f9"
    }

    @Test
    fun colorGrayscaleBt709() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        val nImage = mImage.transform(Grayscale.BT709)
        nImage.save("fits-color-grayscale-bt709").second shouldBe "cab675aa35390a2d58cd48555d91054f"
    }

    @Test
    fun colorGrayscaleRmy() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        val nImage = mImage.transform(Grayscale.RMY)
        nImage.save("fits-color-grayscale-rmy").second shouldBe "e113627002a4178d1010a2f6246e325f"
    }

    @Test
    fun colorGrayscaleY() {
        val mImage = NGC3344_COLOR_32_FITS.fits().asImage()
        val nImage = mImage.transform(Grayscale.Y)
        nImage.save("fits-color-grayscale-y").second shouldBe "24dd4a7e0fa9e4be34c53c924a78a940"
    }

    @Test
    fun colorDebayer() {
        val mImage = DEBAYER_FITS.fits().asImage()
        val nImage = mImage.transform(AutoScreenTransformFunction)
        nImage.save("fits-color-debayer").second shouldBe "c89f709774f6714158c4961d59a0acf2"
    }

    @Test
    fun colorNoDebayer() {
        val mImage = DEBAYER_FITS.fits().asImage(false)
        val nImage = mImage.transform(AutoScreenTransformFunction)
        nImage.save("fits-color-no-debayer").second shouldBe "958ccea020deec1f0c075042a9ba37c3"
    }
}
