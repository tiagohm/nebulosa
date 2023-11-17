import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.shouldBe
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.*
import nebulosa.test.FitsStringSpec

class TransformAlgorithmTest : FitsStringSpec() {

    init {
        "mono:raw" {
            val mImage = Image.openFITS(m8bit)
            mImage.save("mono-raw").second shouldBe "ba68b3a4028d5542c4a97ee042aaf1bc"
        }
        "mono:vertical flip" {
            val mImage = Image.openFITS(m8bit)
            mImage.transform(VerticalFlip)
            mImage.save("mono-vertical-flip").second shouldBe "ab18fd028976bc35ab455bc204b3a63e"
        }
        "mono:horizontal flip" {
            val mImage = Image.openFITS(m8bit)
            mImage.transform(HorizontalFlip)
            mImage.save("mono-horizontal-flip").second shouldBe "8ebd0b8706df2b447b1d4012019e0936"
        }
        "mono:vertical & horizontal flip" {
            val mImage = Image.openFITS(m8bit)
            mImage.transform(VerticalFlip, HorizontalFlip)
            mImage.save("mono-vertical-horizontal-flip").second shouldBe "996ffbe5f1fa4f80084452e306c81124"
        }
        "mono:subframe" {
            val mImage = Image.openFITS(m8bit)
            val nImage = mImage.transform(SubFrame(2, 2, 3, 3))
            nImage.width shouldBeExactly 3
            nImage.height shouldBeExactly 3
            nImage.mono.shouldBeTrue()
            nImage.save("mono-subframe").second shouldBe "a216c3ca82fc036df540a01550f3cd01"
        }
        "mono:sharpen" {
            val mImage = Image.openFITS(m8bit)
            mImage.transform(Sharpen)
            mImage.save("mono-sharpen").second shouldBe "ba68b3a4028d5542c4a97ee042aaf1bc"
        }
        "mono:mean" {
            val mImage = Image.openFITS(m8bit)
            mImage.transform(Mean)
            mImage.save("mono-mean").second shouldBe "ffcb0db0b0c11ef37dca44e493102627"
        }
        "mono:invert" {
            val mImage = Image.openFITS(m8bit)
            mImage.transform(Invert)
            mImage.save("mono-invert").second shouldBe "1356fc73cfba955afc1125df21240d7f"
        }
        "mono:emboss" {
            val mImage = Image.openFITS(m8bit)
            mImage.transform(Emboss)
            mImage.save("mono-emboss").second shouldBe "1356fc73cfba955afc1125df21240d7f"
        }
    }
}
