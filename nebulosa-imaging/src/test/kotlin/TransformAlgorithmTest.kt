import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.*
import nebulosa.test.FitsStringSpec

class TransformAlgorithmTest : FitsStringSpec() {

    init {
        "mono:raw" {
            val mImage = Image.open(NGC3344_MONO_8)
            mImage.save("mono-raw").second shouldBe "e17cfc29c3b343409cd8617b6913330e"
        }
        "mono:vertical flip" {
            val mImage = Image.open(NGC3344_MONO_8)
            mImage.transform(VerticalFlip)
            mImage.save("mono-vertical-flip").second shouldBe "262260dfe719726c0e7829a088279a21"
        }
        "mono:horizontal flip" {
            val mImage = Image.open(NGC3344_MONO_8)
            mImage.transform(HorizontalFlip)
            mImage.save("mono-horizontal-flip").second shouldBe "daf0f05db5de3750962f338527564b27"
        }
        "mono:vertical & horizontal flip" {
            val mImage = Image.open(NGC3344_MONO_8)
            mImage.transform(VerticalFlip, HorizontalFlip)
            mImage.save("mono-vertical-horizontal-flip").second shouldBe "3bc81f579a0e34ce9312c3b242209166"
        }
        "mono:subframe" {
            val mImage = Image.open(NGC3344_MONO_8)
            val nImage = mImage.transform(SubFrame(45, 70, 16, 16))
            nImage.width shouldBeExactly 16
            nImage.height shouldBeExactly 16
            nImage.mono.shouldBeTrue()
            nImage.save("mono-subframe").second shouldBe "4d9984e778f82dde10b9aeeee7a29fe0"
        }
        "mono:sharpen" {
            val mImage = Image.open(NGC3344_MONO_8)
            mImage.transform(Sharpen)
            mImage.save("mono-sharpen").second shouldBe "0b162242a4e673f6480b5206cf49ca50"
        }
        "mono:mean" {
            val mImage = Image.open(NGC3344_MONO_8)
            mImage.transform(Mean)
            mImage.save("mono-mean").second shouldBe "cf866292f657c379ae3965931dd8eeea"
        }
        "mono:invert" {
            val mImage = Image.open(NGC3344_MONO_8)
            mImage.transform(Invert)
            mImage.save("mono-invert").second shouldBe "6e94463bb5b9561de1f0ee0a154db53e"
        }
        "mono:emboss" {
            val mImage = Image.open(NGC3344_MONO_8)
            mImage.transform(Emboss)
            mImage.save("mono-emboss").second shouldBe "94a8ef5e4573e392d087cf10c905ba12"
        }
        "mono:edges" {
            val mImage = Image.open(NGC3344_MONO_8)
            mImage.transform(Edges)
            mImage.save("mono-edges").second shouldBe "27ccd5f5e6098d0cae27e7495e18dd72"
        }
        "mono:blur" {
            val mImage = Image.open(NGC3344_MONO_8)
            mImage.transform(Blur)
            mImage.save("mono-blur").second shouldBe "f2c5466dccf71b5c4bee86c5fbbb95fc"
        }
        "mono:gaussian blur" {
            val mImage = Image.open(NGC3344_MONO_8)
            mImage.transform(GaussianBlur(sigma = 5.0, size = 9))
            mImage.save("mono-gaussian-blur").second shouldBe "69057b0c4461fb0d55b779da9e72fd69"
        }
        "mono:STF:midtone = 0.1, shadow = 0.0, highlight = 1.0" {
            val mImage = Image.open(NGC3344_MONO_8)
            mImage.transform(ScreenTransformFunction(0.1f))
            mImage.save("mono-stf-01-00-10").second shouldBe "22c0bd985e70a01330722d912869d6ee"
        }
        "mono:STF:midtone = 0.9, shadow = 0.0, highlight = 1.0" {
            val mImage = Image.open(NGC3344_MONO_8)
            mImage.transform(ScreenTransformFunction(0.9f))
            mImage.save("mono-stf-09-00-10").second shouldBe "553ccb7546dce3a8f742d5e8f7c58a3f"
        }
        "mono:STF:midtone = 0.1, shadow = 0.5, highlight = 1.0" {
            val mImage = Image.open(NGC3344_MONO_8)
            mImage.transform(ScreenTransformFunction(0.1f, shadow = 0.5f))
            mImage.save("mono-stf-01-05-10").second shouldBe "f31db854fab72033dce2f8c572ec6783"
        }
        "mono:STF:midtone = 0.9, shadow = 0.5, highlight = 1.0" {
            val mImage = Image.open(NGC3344_MONO_8)
            mImage.transform(ScreenTransformFunction(0.9f, shadow = 0.5f))
            mImage.save("mono-stf-09-05-10").second shouldBe "633b49c4a1dbb5ad8e6a9d74f330636d"
        }
        "mono:STF:midtone = 0.1, shadow = 0.0, highlight = 0.5" {
            val mImage = Image.open(NGC3344_MONO_8)
            mImage.transform(ScreenTransformFunction(0.1f, highlight = 0.5f))
            mImage.save("mono-stf-01-00-05").second shouldBe "26036937eb3e5f99cd6129f709ce4b31"
        }
        "mono:STF:midtone = 0.9, shadow = 0.0, highlight = 0.5" {
            val mImage = Image.open(NGC3344_MONO_8)
            mImage.transform(ScreenTransformFunction(0.9f, highlight = 0.5f))
            mImage.save("mono-stf-09-00-05").second shouldBe "e8f694dae666ac15ce2f8a169eb84024"
        }
        "mono:STF:midtone = 0.1, shadow = 0.4, highlight = 0.6" {
            val mImage = Image.open(NGC3344_MONO_8)
            mImage.transform(ScreenTransformFunction(0.1f, 0.4f, 0.6f))
            mImage.save("mono-stf-01-04-06").second shouldBe "5226aba21669a24f985703b3e7220568"
        }
        "mono:STF:midtone = 0.9, shadow = 0.4, highlight = 0.6" {
            val mImage = Image.open(NGC3344_MONO_8)
            mImage.transform(ScreenTransformFunction(0.9f, 0.4f, 0.6f))
            mImage.save("mono-stf-09-04-06").second shouldBe "c2acb25ef7be92a51f63e673ec9a850f"
        }
        "mono:auto STF" {
            val mImage = Image.open(NGC3344_MONO_8)
            mImage.transform(AutoScreenTransformFunction)
            mImage.save("mono-auto-stf").second shouldBe "e17cfc29c3b343409cd8617b6913330e"
        }
    }
}
