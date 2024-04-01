import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nebulosa.image.Image
import nebulosa.image.algorithms.transformation.*
import nebulosa.image.algorithms.transformation.convolution.*
import nebulosa.image.format.ImageChannel
import nebulosa.test.AbstractFitsAndXisfTest
import nebulosa.xisf.xisf

class XisfTransformAlgorithmTest : AbstractFitsAndXisfTest() {

    init {
        "mono:raw" {
            val mImage = Image.open(M82_MONO_8_XISF.xisf())
            mImage.save("xisf-mono-raw").second shouldBe "0dca7efedef5b3525f8037f401518b0b"
        }
        "mono:vertical flip" {
            val mImage = Image.open(M82_MONO_8_XISF.xisf())
            mImage.transform(VerticalFlip)
            mImage.save("xisf-mono-vertical-flip").second shouldBe "d8250e5ac45d9a6ec04bed2a583a70b2"
        }
        "mono:horizontal flip" {
            val mImage = Image.open(M82_MONO_8_XISF.xisf())
            mImage.transform(HorizontalFlip)
            mImage.save("xisf-mono-horizontal-flip").second shouldBe "9e2a05c331a5daeff20671a365e8b74c"
        }
        "mono:vertical & horizontal flip" {
            val mImage = Image.open(M82_MONO_8_XISF.xisf())
            mImage.transform(VerticalFlip, HorizontalFlip)
            mImage.save("xisf-mono-vertical-horizontal-flip").second shouldBe "c7d55e870850c10619f389e3f9ec1243"
        }
        "mono:subframe" {
            val mImage = Image.open(M82_MONO_8_XISF.xisf())
            val nImage = mImage.transform(SubFrame(45, 70, 16, 16))
            nImage.width shouldBeExactly 16
            nImage.height shouldBeExactly 16
            nImage.mono.shouldBeTrue()
            nImage.save("xisf-mono-subframe").second shouldBe "dc0ca19c5d40f90e4daac87fbab8f449"
        }
        "mono:sharpen" {
            val mImage = Image.open(M82_MONO_8_XISF.xisf())
            mImage.transform(Sharpen)
            mImage.save("xisf-mono-sharpen").second shouldBe "08421f6afef422cdaa9f464a860808ce"
        }
        "mono:mean" {
            val mImage = Image.open(M82_MONO_8_XISF.xisf())
            mImage.transform(Mean)
            mImage.save("xisf-mono-mean").second shouldBe "4127027d467ce9537049dc5f25f23ede"
        }
        "mono:invert" {
            val mImage = Image.open(M82_MONO_8_XISF.xisf())
            mImage.transform(Invert)
            mImage.save("xisf-mono-invert").second shouldBe "a6ec4a55225cb18f004e159a60137fe9"
        }
        "mono:emboss" {
            val mImage = Image.open(M82_MONO_8_XISF.xisf())
            mImage.transform(Emboss)
            mImage.save("xisf-mono-emboss").second shouldBe "2f07b3964b52f6b141b8cc45639b2287"
        }
        "mono:edges" {
            val mImage = Image.open(M82_MONO_8_XISF.xisf())
            mImage.transform(Edges)
            mImage.save("xisf-mono-edges").second shouldBe "897af72d536bca57cce77f09e396adb7"
        }
        "mono:blur" {
            val mImage = Image.open(M82_MONO_8_XISF.xisf())
            mImage.transform(Blur)
            mImage.save("xisf-mono-blur").second shouldBe "5a850ef20c0ebd461e676523823ff6ea"
        }
        "mono:gaussian blur" {
            val mImage = Image.open(M82_MONO_8_XISF.xisf())
            mImage.transform(GaussianBlur(sigma = 5.0, size = 9))
            mImage.save("xisf-mono-gaussian-blur").second shouldBe "cb7489cb1948e802ea4d92bba24e0739"
        }
        "mono:STF:midtone = 0.1, shadow = 0.0, highlight = 1.0" {
            val mImage = Image.open(M82_MONO_8_XISF.xisf())
            mImage.transform(ScreenTransformFunction(0.1f))
            mImage.save("xisf-mono-stf-01-00-10").second shouldBe "91621dba5a58081347d156f2152bd8c6"
        }
        "mono:STF:midtone = 0.9, shadow = 0.0, highlight = 1.0" {
            val mImage = Image.open(M82_MONO_8_XISF.xisf())
            mImage.transform(ScreenTransformFunction(0.9f))
            mImage.save("xisf-mono-stf-09-00-10").second shouldBe "9a67728cfc4f871ea8ab6557f381949e"
        }
        "mono:STF:midtone = 0.1, shadow = 0.5, highlight = 1.0" {
            val mImage = Image.open(M82_MONO_8_XISF.xisf())
            mImage.transform(ScreenTransformFunction(0.1f, shadow = 0.5f))
            mImage.save("xisf-mono-stf-01-05-10").second shouldBe "c70b9caaaf88bba9c64891a2b3ba8033"
        }
        "mono:STF:midtone = 0.9, shadow = 0.5, highlight = 1.0" {
            val mImage = Image.open(M82_MONO_8_XISF.xisf())
            mImage.transform(ScreenTransformFunction(0.9f, shadow = 0.5f))
            mImage.save("xisf-mono-stf-09-05-10").second shouldBe "4f4c49b96bd9aa417f22c59b8224ea90"
        }
        "mono:STF:midtone = 0.1, shadow = 0.0, highlight = 0.5" {
            val mImage = Image.open(M82_MONO_8_XISF.xisf())
            mImage.transform(ScreenTransformFunction(0.1f, highlight = 0.5f))
            mImage.save("xisf-mono-stf-01-00-05").second shouldBe "b0c9598e3003d72a5fba617c30dc1821"
        }
        "mono:STF:midtone = 0.9, shadow = 0.0, highlight = 0.5" {
            val mImage = Image.open(M82_MONO_8_XISF.xisf())
            mImage.transform(ScreenTransformFunction(0.9f, highlight = 0.5f))
            mImage.save("xisf-mono-stf-09-00-05").second shouldBe "5824ac8c2d44c2c8ff8121e0904bb77b"
        }
        "mono:STF:midtone = 0.1, shadow = 0.4, highlight = 0.6" {
            val mImage = Image.open(M82_MONO_8_XISF.xisf())
            mImage.transform(ScreenTransformFunction(0.1f, 0.4f, 0.6f))
            mImage.save("xisf-mono-stf-01-04-06").second shouldBe "20417dfc43fc4c5cb425875614d8066c"
        }
        "mono:STF:midtone = 0.9, shadow = 0.4, highlight = 0.6" {
            val mImage = Image.open(M82_MONO_8_XISF.xisf())
            mImage.transform(ScreenTransformFunction(0.9f, 0.4f, 0.6f))
            mImage.save("xisf-mono-stf-09-04-06").second shouldBe "13d13ad164a952a29f7ad76e1e51e7d0"
        }
        "mono:auto STF" {
            val mImage = Image.open(M82_MONO_8_XISF.xisf())
            mImage.transform(AutoScreenTransformFunction)
            mImage.save("xisf-mono-auto-stf").second shouldBe "9204a71df3770e8fe5ca49e3420eed72"
        }
        "!mono:reload" {
            val mImage0 = Image.open(M82_MONO_8_XISF.xisf())

            val mImage1 = Image.open(M82_MONO_8_XISF.xisf())
            mImage1.transform(Invert)

            mImage0.load(mImage1.hdu)
            mImage0.save("xisf-mono-reload").second shouldBe "6e94463bb5b9561de1f0ee0a154db53e"
        }
        "color:raw" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.save("xisf-color-raw").second shouldBe "764e326cc5260d81f3761112ad6a1969"
        }
        "color:vertical flip" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(VerticalFlip)
            mImage.save("xisf-color-vertical-flip").second shouldBe "595d8d3e46e91d7adf7591e591a7a98d"
        }
        "color:horizontal flip" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(HorizontalFlip)
            mImage.save("xisf-color-horizontal-flip").second shouldBe "b3d9609ef88db9b9215b4e21fdd4992a"
        }
        "color:vertical & horizontal flip" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(VerticalFlip, HorizontalFlip)
            mImage.save("xisf-color-vertical-horizontal-flip").second shouldBe "1c9be1a7e5a66a989f6d0715c7ff30e4"
        }
        "color:subframe" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            val nImage = mImage.transform(SubFrame(45, 70, 16, 16))
            nImage.width shouldBeExactly 16
            nImage.height shouldBeExactly 16
            nImage.mono.shouldBeFalse()
            nImage.save("xisf-color-subframe").second shouldBe "dcf5bc8908ea6a32a5e98f14152f47b9"
        }
        "color:sharpen" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(Sharpen)
            mImage.save("xisf-color-sharpen").second shouldBe "1017a88ff2caeb83a650d94d96c59347"
        }
        "color:mean" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(Mean)
            mImage.save("xisf-color-mean").second shouldBe "eb948cab18442d008381f392bf43542a"
        }
        "color:invert" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(Invert)
            mImage.save("xisf-color-invert").second shouldBe "bbc97cdcde240873439bc29ff1adf169"
        }
        "color:emboss" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(Emboss)
            mImage.save("xisf-color-emboss").second shouldBe "fba6827f204df93f4aaf2f99b113a8f7"
        }
        "color:edges" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(Edges)
            mImage.save("xisf-color-edges").second shouldBe "71d6b5c936ab8165c7c1b63514d2da7b"
        }
        "color:blur" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(Blur)
            mImage.save("xisf-color-blur").second shouldBe "5c9f738a309fc86956d124f0f109eea2"
        }
        "color:gaussian blur" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(GaussianBlur(sigma = 5.0, size = 9))
            mImage.save("xisf-color-gaussian-blur").second shouldBe "648beabcea0b486efc3f0c4ded632a06"
        }
        "color:STF:midtone = 0.1, shadow = 0.0, highlight = 1.0" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(ScreenTransformFunction(0.1f))
            mImage.save("xisf-color-stf-01-00-10").second shouldBe "d84577785e44179f7af7c00807be5260"
        }
        "color:STF:midtone = 0.9, shadow = 0.0, highlight = 1.0" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(ScreenTransformFunction(0.9f))
            mImage.save("xisf-color-stf-09-00-10").second shouldBe "11771bfe0e180e1efea4568902109013"
        }
        "color:STF:midtone = 0.1, shadow = 0.5, highlight = 1.0" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(ScreenTransformFunction(0.1f, shadow = 0.5f))
            mImage.save("xisf-color-stf-01-05-10").second shouldBe "4e3b204d8a7ed06dfd05b7d010ef267b"
        }
        "color:STF:midtone = 0.9, shadow = 0.5, highlight = 1.0" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(ScreenTransformFunction(0.9f, shadow = 0.5f))
            mImage.save("xisf-color-stf-09-05-10").second shouldBe "95c5b801978abdbb51afc9d0de12c218"
        }
        "color:STF:midtone = 0.1, shadow = 0.0, highlight = 0.5" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(ScreenTransformFunction(0.1f, highlight = 0.5f))
            mImage.save("xisf-color-stf-01-00-05").second shouldBe "08322042c9e57102fec77a06b28c263d"
        }
        "color:STF:midtone = 0.9, shadow = 0.0, highlight = 0.5" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(ScreenTransformFunction(0.9f, highlight = 0.5f))
            mImage.save("xisf-color-stf-09-00-05").second shouldBe "c99990d3a4bc6fc3aa7a8ac90d452419"
        }
        "color:STF:midtone = 0.1, shadow = 0.4, highlight = 0.6" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(ScreenTransformFunction(0.1f, 0.4f, 0.6f))
            mImage.save("xisf-color-stf-01-04-06").second shouldBe "33bce2d7f7cde67d0ee618439f438ee7"
        }
        "color:STF:midtone = 0.9, shadow = 0.4, highlight = 0.6" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(ScreenTransformFunction(0.9f, 0.4f, 0.6f))
            mImage.save("xisf-color-stf-09-04-06").second shouldBe "923e56840bcea4462160e36f2e801f5e"
        }
        "color:auto STF" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(AutoScreenTransformFunction)
            mImage.save("xisf-color-auto-stf").second shouldBe "b1460451ad0f0580802d3d6d3a6750ba"
        }
        "color:SCNR Maximum Mask" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(SubtractiveChromaticNoiseReduction(ImageChannel.RED, 1f, ProtectionMethod.MAXIMUM_MASK))
            mImage.save("xisf-color-scnr-maximum-mask").second shouldBe "e465a2fc0814055e089a3180c0235c8b"
        }
        "color:SCNR Additive Mask" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(SubtractiveChromaticNoiseReduction(ImageChannel.RED, 1f, ProtectionMethod.ADDITIVE_MASK))
            mImage.save("xisf-color-scnr-additive-mask").second shouldBe "1016ec07253f869097d2d9c67194b3e3"
        }
        "color:SCNR Average Neutral" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(SubtractiveChromaticNoiseReduction(ImageChannel.RED, 1f, ProtectionMethod.AVERAGE_NEUTRAL))
            mImage.save("xisf-color-scnr-average-neutral").second shouldBe "2f2583bfb2ae4a9e4441dc9b827196f7"
        }
        "color:SCNR Maximum Neutral" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(SubtractiveChromaticNoiseReduction(ImageChannel.RED, 1f, ProtectionMethod.MAXIMUM_NEUTRAL))
            mImage.save("xisf-color-scnr-maximum-neutral").second shouldBe "dabd995db4fba052617b148b3700378d"
        }
        "color:SCNR Minimum Neutral" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            mImage.transform(SubtractiveChromaticNoiseReduction(ImageChannel.RED, 1f, ProtectionMethod.MINIMUM_NEUTRAL))
            mImage.save("xisf-color-scnr-minimum-neutral").second shouldBe "b92e5130ec2a44c0afd1ca3782261e47"
        }
        "color:grayscale BT-709" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            val nImage = mImage.transform(Grayscale.BT709)
            nImage.save("xisf-color-grayscale-bt709").second shouldBe "bf78482866a0b3a216fa5262f28b0e5e"
        }
        "color:grayscale RMY" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            val nImage = mImage.transform(Grayscale.RMY)
            nImage.save("xisf-color-grayscale-rmy").second shouldBe "e75085eea073811aef41624aab318b48"
        }
        "color:grayscale Y" {
            val mImage = Image.open(M82_COLOR_32_XISF.xisf())
            val nImage = mImage.transform(Grayscale.Y)
            nImage.save("xisf-color-grayscale-y").second shouldBe "7a2bef966d460742533a1c8c3a74f1c5"
        }
        "!color:debayer" {
            val mImage = Image.open(DEBAYER_FITS.xisf())
            val nImage = mImage.transform(AutoScreenTransformFunction)
            nImage.save("xisf-color-debayer").second shouldBe "86b5bdd67dfd6bbf5495afae4bf2bc04"
        }
        "!color:no-debayer" {
            val mImage = Image.open(DEBAYER_FITS.xisf(), false)
            val nImage = mImage.transform(AutoScreenTransformFunction)
            nImage.save("xisf-color-no-debayer").second shouldBe "958ccea020deec1f0c075042a9ba37c3"
        }
        "!color:reload" {
            val mImage0 = Image.open(M82_COLOR_32_XISF.xisf())
            var mImage1 = Image.open(DEBAYER_FITS.xisf())

            mImage1.load(mImage0.hdu).shouldNotBeNull()
            mImage1.save("xisf-color-reload").second shouldBe "18fb83e240bc7a4cbafbc1aba2741db6"

            mImage1 = Image.open(DEBAYER_FITS.xisf(), false)

            mImage1.load(mImage0.hdu).shouldBeNull()
            mImage0.load(mImage1.hdu).shouldBeNull()
        }
    }
}
