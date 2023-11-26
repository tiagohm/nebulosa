import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import nebulosa.imaging.Image
import nebulosa.imaging.algorithms.computation.hfd.HalfFluxDiameter
import nebulosa.test.FitsStringSpec

class HFDTest : FitsStringSpec() {

    init {
        "focus" {
            val starFocus = arrayOf(
                STAR_FOCUS_1 to floatArrayOf(7.0f, 77.2f, 11950.023f),
                STAR_FOCUS_2 to floatArrayOf(7.3f, 114.1f, 26055.076f),
                STAR_FOCUS_3 to floatArrayOf(7.7f, 185.1f, 68531.1f),
                STAR_FOCUS_4 to floatArrayOf(6.9f, 363.4f, 264183.53f),
                STAR_FOCUS_5 to floatArrayOf(6.2f, 382.5f, 292700.53f),
                STAR_FOCUS_6 to floatArrayOf(6.1f, 410.0f, 336331.9f),
                STAR_FOCUS_7 to floatArrayOf(5.7f, 422.7f, 357451.03f),
                STAR_FOCUS_8 to floatArrayOf(5.1f, 430.5f, 370737.5f),
                STAR_FOCUS_9 to floatArrayOf(4.8f, 435.4f, 379284.03f),
                STAR_FOCUS_10 to floatArrayOf(4.3f, 444.6f, 395406.34f),
                STAR_FOCUS_11 to floatArrayOf(3.79f, 442.9f, 392338.5f),
                STAR_FOCUS_12 to floatArrayOf(3.71f, 443.3f, 393080.38f),
                STAR_FOCUS_13 to floatArrayOf(4.9f, 443.6f, 393603.97f),
                STAR_FOCUS_14 to floatArrayOf(6.1f, 368.8f, 272057.78f),
                STAR_FOCUS_15 to floatArrayOf(6.9f, 249.0f, 124078.05f),
                STAR_FOCUS_16 to floatArrayOf(6.5f, 188.2f, 70905.97f),
                STAR_FOCUS_17 to floatArrayOf(6.9f, 164.3f, 53994.75f),
            )

            for ((first, second) in starFocus) {
                val focusImage = Image.open(first)
                val star = focusImage.compute(HalfFluxDiameter(focusImage.width / 2, focusImage.height / 2, 50))
                star.hfd shouldBe (second[0] plusOrMinus 0.1f)
                star.snr shouldBe (second[1] plusOrMinus 0.1f)
                star.flux shouldBe (second[2] plusOrMinus 0.1f)
            }
        }
    }
}
