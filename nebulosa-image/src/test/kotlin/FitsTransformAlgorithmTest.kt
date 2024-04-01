import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nebulosa.fits.fits
import nebulosa.image.Image
import nebulosa.image.algorithms.transformation.*
import nebulosa.image.algorithms.transformation.convolution.*
import nebulosa.image.format.ImageChannel
import nebulosa.test.FitsStringSpec

class FitsTransformAlgorithmTest : FitsStringSpec() {

    init {
        "mono:raw" {
            val mImage = Image.open(NGC3344_MONO_8_FITS.fits())
            mImage.save("fits-mono-raw").second shouldBe "e17cfc29c3b343409cd8617b6913330e"
        }
        "mono:vertical flip" {
            val mImage = Image.open(NGC3344_MONO_8_FITS.fits())
            mImage.transform(VerticalFlip)
            mImage.save("fits-mono-vertical-flip").second shouldBe "262260dfe719726c0e7829a088279a21"
        }
        "mono:horizontal flip" {
            val mImage = Image.open(NGC3344_MONO_8_FITS.fits())
            mImage.transform(HorizontalFlip)
            mImage.save("fits-mono-horizontal-flip").second shouldBe "daf0f05db5de3750962f338527564b27"
        }
        "mono:vertical & horizontal flip" {
            val mImage = Image.open(NGC3344_MONO_8_FITS.fits())
            mImage.transform(VerticalFlip, HorizontalFlip)
            mImage.save("fits-mono-vertical-horizontal-flip").second shouldBe "3bc81f579a0e34ce9312c3b242209166"
        }
        "mono:subframe" {
            val mImage = Image.open(NGC3344_MONO_8_FITS.fits())
            val nImage = mImage.transform(SubFrame(45, 70, 16, 16))
            nImage.width shouldBeExactly 16
            nImage.height shouldBeExactly 16
            nImage.mono.shouldBeTrue()
            nImage.save("fits-mono-subframe").second shouldBe "4d9984e778f82dde10b9aeeee7a29fe0"
        }
        "mono:sharpen" {
            val mImage = Image.open(NGC3344_MONO_8_FITS.fits())
            mImage.transform(Sharpen)
            mImage.save("fits-mono-sharpen").second shouldBe "0b162242a4e673f6480b5206cf49ca50"
        }
        "mono:mean" {
            val mImage = Image.open(NGC3344_MONO_8_FITS.fits())
            mImage.transform(Mean)
            mImage.save("fits-mono-mean").second shouldBe "cf866292f657c379ae3965931dd8eeea"
        }
        "mono:invert" {
            val mImage = Image.open(NGC3344_MONO_8_FITS.fits())
            mImage.transform(Invert)
            mImage.save("fits-mono-invert").second shouldBe "6e94463bb5b9561de1f0ee0a154db53e"
        }
        "mono:emboss" {
            val mImage = Image.open(NGC3344_MONO_8_FITS.fits())
            mImage.transform(Emboss)
            mImage.save("fits-mono-emboss").second shouldBe "94a8ef5e4573e392d087cf10c905ba12"
        }
        "mono:edges" {
            val mImage = Image.open(NGC3344_MONO_8_FITS.fits())
            mImage.transform(Edges)
            mImage.save("fits-mono-edges").second shouldBe "27ccd5f5e6098d0cae27e7495e18dd72"
        }
        "mono:blur" {
            val mImage = Image.open(NGC3344_MONO_8_FITS.fits())
            mImage.transform(Blur)
            mImage.save("fits-mono-blur").second shouldBe "f2c5466dccf71b5c4bee86c5fbbb95fc"
        }
        "mono:gaussian blur" {
            val mImage = Image.open(NGC3344_MONO_8_FITS.fits())
            mImage.transform(GaussianBlur(sigma = 5.0, size = 9))
            mImage.save("fits-mono-gaussian-blur").second shouldBe "69057b0c4461fb0d55b779da9e72fd69"
        }
        "mono:STF:midtone = 0.1, shadow = 0.0, highlight = 1.0" {
            val mImage = Image.open(NGC3344_MONO_8_FITS.fits())
            mImage.transform(ScreenTransformFunction(0.1f))
            mImage.save("fits-mono-stf-01-00-10").second shouldBe "22c0bd985e70a01330722d912869d6ee"
        }
        "mono:STF:midtone = 0.9, shadow = 0.0, highlight = 1.0" {
            val mImage = Image.open(NGC3344_MONO_8_FITS.fits())
            mImage.transform(ScreenTransformFunction(0.9f))
            mImage.save("fits-mono-stf-09-00-10").second shouldBe "553ccb7546dce3a8f742d5e8f7c58a3f"
        }
        "mono:STF:midtone = 0.1, shadow = 0.5, highlight = 1.0" {
            val mImage = Image.open(NGC3344_MONO_8_FITS.fits())
            mImage.transform(ScreenTransformFunction(0.1f, shadow = 0.5f))
            mImage.save("fits-mono-stf-01-05-10").second shouldBe "f31db854fab72033dce2f8c572ec6783"
        }
        "mono:STF:midtone = 0.9, shadow = 0.5, highlight = 1.0" {
            val mImage = Image.open(NGC3344_MONO_8_FITS.fits())
            mImage.transform(ScreenTransformFunction(0.9f, shadow = 0.5f))
            mImage.save("fits-mono-stf-09-05-10").second shouldBe "633b49c4a1dbb5ad8e6a9d74f330636d"
        }
        "mono:STF:midtone = 0.1, shadow = 0.0, highlight = 0.5" {
            val mImage = Image.open(NGC3344_MONO_8_FITS.fits())
            mImage.transform(ScreenTransformFunction(0.1f, highlight = 0.5f))
            mImage.save("fits-mono-stf-01-00-05").second shouldBe "26036937eb3e5f99cd6129f709ce4b31"
        }
        "mono:STF:midtone = 0.9, shadow = 0.0, highlight = 0.5" {
            val mImage = Image.open(NGC3344_MONO_8_FITS.fits())
            mImage.transform(ScreenTransformFunction(0.9f, highlight = 0.5f))
            mImage.save("fits-mono-stf-09-00-05").second shouldBe "e8f694dae666ac15ce2f8a169eb84024"
        }
        "mono:STF:midtone = 0.1, shadow = 0.4, highlight = 0.6" {
            val mImage = Image.open(NGC3344_MONO_8_FITS.fits())
            mImage.transform(ScreenTransformFunction(0.1f, 0.4f, 0.6f))
            mImage.save("fits-mono-stf-01-04-06").second shouldBe "5226aba21669a24f985703b3e7220568"
        }
        "mono:STF:midtone = 0.9, shadow = 0.4, highlight = 0.6" {
            val mImage = Image.open(NGC3344_MONO_8_FITS.fits())
            mImage.transform(ScreenTransformFunction(0.9f, 0.4f, 0.6f))
            mImage.save("fits-mono-stf-09-04-06").second shouldBe "c2acb25ef7be92a51f63e673ec9a850f"
        }
        "mono:auto STF" {
            val mImage = Image.open(NGC3344_MONO_8_FITS.fits())
            mImage.transform(AutoScreenTransformFunction)
            mImage.save("fits-mono-auto-stf").second shouldBe "e17cfc29c3b343409cd8617b6913330e"
        }
        "!mono:reload" {
            val mImage0 = Image.open(NGC3344_MONO_8_FITS.fits())

            val mImage1 = Image.open(NGC3344_MONO_8_FITS.fits())
            mImage1.transform(Invert)

            mImage0.load(mImage1.hdu)
            mImage0.save("fits-mono-reload").second shouldBe "6e94463bb5b9561de1f0ee0a154db53e"
        }
        "color:raw" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.save("fits-color-raw").second shouldBe "18fb83e240bc7a4cbafbc1aba2741db6"
        }
        "color:vertical flip" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(VerticalFlip)
            mImage.save("fits-color-vertical-flip").second shouldBe "b717ecda5c5bba50cfa06304ef2bca88"
        }
        "color:horizontal flip" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(HorizontalFlip)
            mImage.save("fits-color-horizontal-flip").second shouldBe "f70228600c77551473008ed4b9986439"
        }
        "color:vertical & horizontal flip" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(VerticalFlip, HorizontalFlip)
            mImage.save("fits-color-vertical-horizontal-flip").second shouldBe "1237314044f20307b76203148af855e3"
        }
        "color:subframe" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            val nImage = mImage.transform(SubFrame(45, 70, 16, 16))
            nImage.width shouldBeExactly 16
            nImage.height shouldBeExactly 16
            nImage.mono.shouldBeFalse()
            nImage.save("fits-color-subframe").second shouldBe "282fc4fdf9142fcb4b18e1df1eef4caa"
        }
        "color:sharpen" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(Sharpen)
            mImage.save("fits-color-sharpen").second shouldBe "e562282bdafdeba6ce88981bb9c3ba61"
        }
        "color:mean" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(Mean)
            mImage.save("fits-color-mean").second shouldBe "a8380d928aaa756e202ba43bd3a2f207"
        }
        "color:invert" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(Invert)
            mImage.save("fits-color-invert").second shouldBe "decad269ec26450aebeaf7546867b5f8"
        }
        "color:emboss" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(Emboss)
            mImage.save("fits-color-emboss").second shouldBe "58d69250f1233055aa33f9ec7ca40af1"
        }
        "color:edges" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(Edges)
            mImage.save("fits-color-edges").second shouldBe "091f2955740a8edcd2401dc416d19d51"
        }
        "color:blur" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(Blur)
            mImage.save("fits-color-blur").second shouldBe "0fca440b763de5380fa29de736f3c792"
        }
        "color:gaussian blur" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(GaussianBlur(sigma = 5.0, size = 9))
            mImage.save("fits-color-gaussian-blur").second shouldBe "394d1a4f136f15c802dd73004c421d64"
        }
        "color:STF:midtone = 0.1, shadow = 0.0, highlight = 1.0" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(ScreenTransformFunction(0.1f))
            mImage.save("fits-color-stf-01-00-10").second shouldBe "e952bd263df6fd275b9a80aca554cb4b"
        }
        "color:STF:midtone = 0.9, shadow = 0.0, highlight = 1.0" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(ScreenTransformFunction(0.9f))
            mImage.save("fits-color-stf-09-00-10").second shouldBe "038809d7612018e2e5c19d5e1f551abd"
        }
        "color:STF:midtone = 0.1, shadow = 0.5, highlight = 1.0" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(ScreenTransformFunction(0.1f, shadow = 0.5f))
            mImage.save("fits-color-stf-01-05-10").second shouldBe "70e812260f56f8621002327575611f31"
        }
        "color:STF:midtone = 0.9, shadow = 0.5, highlight = 1.0" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(ScreenTransformFunction(0.9f, shadow = 0.5f))
            mImage.save("fits-color-stf-09-05-10").second shouldBe "6ca400f617f466a9eb02a3a6f2985d99"
        }
        "color:STF:midtone = 0.1, shadow = 0.0, highlight = 0.5" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(ScreenTransformFunction(0.1f, highlight = 0.5f))
            mImage.save("fits-color-stf-01-00-05").second shouldBe "3cd98ee9a8949d5100295acccd77010b"
        }
        "color:STF:midtone = 0.9, shadow = 0.0, highlight = 0.5" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(ScreenTransformFunction(0.9f, highlight = 0.5f))
            mImage.save("fits-color-stf-09-00-05").second shouldBe "2cfeffc88c893cc5883d8a2221f29b91"
        }
        "color:STF:midtone = 0.1, shadow = 0.4, highlight = 0.6" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(ScreenTransformFunction(0.1f, 0.4f, 0.6f))
            mImage.save("fits-color-stf-01-04-06").second shouldBe "532a07a1a166eb007c2e40651aec2097"
        }
        "color:STF:midtone = 0.9, shadow = 0.4, highlight = 0.6" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(ScreenTransformFunction(0.9f, 0.4f, 0.6f))
            mImage.save("fits-color-stf-09-04-06").second shouldBe "eb3d940d9fd2c8814e930715e89897c4"
        }
        "color:auto STF" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(AutoScreenTransformFunction)
            mImage.save("fits-color-auto-stf").second shouldBe "a9c3657d8597b927607eb438e666d3a0"
        }
        "color:SCNR Maximum Mask" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(SubtractiveChromaticNoiseReduction(ImageChannel.RED, 1f, ProtectionMethod.MAXIMUM_MASK))
            mImage.save("fits-color-scnr-maximum-mask").second shouldBe "e7d2155e18ff1e3172f4e849ae983145"
        }
        "color:SCNR Additive Mask" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(SubtractiveChromaticNoiseReduction(ImageChannel.RED, 1f, ProtectionMethod.ADDITIVE_MASK))
            mImage.save("fits-color-scnr-additive-mask").second shouldBe "a458c44cedcda704de16d80053fd87eb"
        }
        "color:SCNR Average Neutral" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(SubtractiveChromaticNoiseReduction(ImageChannel.RED, 1f, ProtectionMethod.AVERAGE_NEUTRAL))
            mImage.save("fits-color-scnr-average-neutral").second shouldBe "e07345ffc4982a62301c95c76d3efb35"
        }
        "color:SCNR Maximum Neutral" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(SubtractiveChromaticNoiseReduction(ImageChannel.RED, 1f, ProtectionMethod.MAXIMUM_NEUTRAL))
            mImage.save("fits-color-scnr-maximum-neutral").second shouldBe "a1d4b04f57b001ba4a996bab0407fd7e"
        }
        "color:SCNR Minimum Neutral" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            mImage.transform(SubtractiveChromaticNoiseReduction(ImageChannel.RED, 1f, ProtectionMethod.MINIMUM_NEUTRAL))
            mImage.save("fits-color-scnr-minimum-neutral").second shouldBe "8b7be57ff38da9c97b35d7888047c0f9"
        }
        "color:grayscale BT-709" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            val nImage = mImage.transform(Grayscale.BT709)
            nImage.save("fits-color-grayscale-bt709").second shouldBe "cab675aa35390a2d58cd48555d91054f"
        }
        "color:grayscale RMY" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            val nImage = mImage.transform(Grayscale.RMY)
            nImage.save("fits-color-grayscale-rmy").second shouldBe "e113627002a4178d1010a2f6246e325f"
        }
        "color:grayscale Y" {
            val mImage = Image.open(NGC3344_COLOR_32_FITS.fits())
            val nImage = mImage.transform(Grayscale.Y)
            nImage.save("fits-color-grayscale-y").second shouldBe "24dd4a7e0fa9e4be34c53c924a78a940"
        }
        "color:debayer" {
            val mImage = Image.open(DEBAYER_FITS_PATH.fits())
            val nImage = mImage.transform(AutoScreenTransformFunction)
            nImage.save("fits-color-debayer").second shouldBe "86b5bdd67dfd6bbf5495afae4bf2bc04"
        }
        "color:no-debayer" {
            val mImage = Image.open(DEBAYER_FITS_PATH.fits(), false)
            val nImage = mImage.transform(AutoScreenTransformFunction)
            nImage.save("fits-color-no-debayer").second shouldBe "958ccea020deec1f0c075042a9ba37c3"
        }
        "color:reload" {
            val mImage0 = Image.open(NGC3344_COLOR_32_FITS.fits())
            var mImage1 = Image.open(DEBAYER_FITS_PATH.fits())

            mImage1.load(mImage0.hdu).shouldNotBeNull()
            mImage1.save("fits-color-reload").second shouldBe "18fb83e240bc7a4cbafbc1aba2741db6"

            mImage1 = Image.open(DEBAYER_FITS_PATH.fits(), false)

            mImage1.load(mImage0.hdu).shouldBeNull()
            mImage0.load(mImage1.hdu).shouldBeNull()
        }
    }
}
