import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import nebulosa.autofocus.AutoFocus
import nebulosa.autofocus.AutoFocusFittingMode
import nebulosa.autofocus.AutoFocusFittingMode.*
import nebulosa.autofocus.AutoFocusResult
import nebulosa.curve.fitting.CurvePoint
import nebulosa.stardetector.StarDetector
import nebulosa.stardetector.StarPoint
import nebulosa.test.AbstractTest
import nebulosa.test.fits.*
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.math.cosh
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.roundToInt

class AutoFocusTest : AbstractTest() {

    @Test
    fun trendHyperbolic() {
        executeAutoFocus().shouldNotBeNull().x.roundToInt() shouldBeGreaterThanOrEqual 1295 shouldBeLessThanOrEqual 1305
        executeAutoFocus(1000).shouldNotBeNull().x.roundToInt() shouldBeGreaterThanOrEqual 1295 shouldBeLessThanOrEqual 1305
        executeAutoFocus(1600).shouldNotBeNull().x.roundToInt() shouldBeGreaterThanOrEqual 1295 shouldBeLessThanOrEqual 1305
        executeAutoFocus(exposureAmount = 3).shouldNotBeNull().x.roundToInt() shouldBeGreaterThanOrEqual 1295 shouldBeLessThanOrEqual 1305
    }

    @Test
    fun trendParabolic() {
        executeAutoFocus(fittingMode = TREND_PARABOLIC).shouldNotBeNull().x.roundToInt() shouldBeGreaterThanOrEqual 1295 shouldBeLessThanOrEqual 1305
        executeAutoFocus(1000, fittingMode = TREND_PARABOLIC).shouldNotBeNull().x.roundToInt() shouldBeGreaterThanOrEqual 1295 shouldBeLessThanOrEqual 1305
        executeAutoFocus(1600, fittingMode = TREND_PARABOLIC).shouldNotBeNull().x.roundToInt() shouldBeGreaterThanOrEqual 1295 shouldBeLessThanOrEqual 1307
        executeAutoFocus(exposureAmount = 3, fittingMode = TREND_PARABOLIC).shouldNotBeNull().x.roundToInt() shouldBeGreaterThanOrEqual 1295 shouldBeLessThanOrEqual 1305
    }

    @Test
    fun trendlines() {
        executeAutoFocus(fittingMode = TRENDLINES).shouldNotBeNull().x.roundToInt() shouldBeGreaterThanOrEqual 1295 shouldBeLessThanOrEqual 1305
        executeAutoFocus(1000, fittingMode = TRENDLINES).shouldNotBeNull().x.roundToInt() shouldBeGreaterThanOrEqual 1295 shouldBeLessThanOrEqual 1305
        executeAutoFocus(1600, fittingMode = TRENDLINES).shouldNotBeNull().x.roundToInt() shouldBeGreaterThanOrEqual 1295 shouldBeLessThanOrEqual 1305
        executeAutoFocus(exposureAmount = 3, fittingMode = TRENDLINES).shouldNotBeNull().x.roundToInt() shouldBeGreaterThanOrEqual 1295 shouldBeLessThanOrEqual 1305
    }

    @Test
    fun hyperbolic() {
        executeAutoFocus(fittingMode = HYPERBOLIC).shouldNotBeNull().x.roundToInt() shouldBeGreaterThanOrEqual 1295 shouldBeLessThanOrEqual 1305
        executeAutoFocus(1000, fittingMode = HYPERBOLIC).shouldNotBeNull().x.roundToInt() shouldBeGreaterThanOrEqual 1295 shouldBeLessThanOrEqual 1305
        executeAutoFocus(1600, fittingMode = HYPERBOLIC).shouldNotBeNull().x.roundToInt() shouldBeGreaterThanOrEqual 1295 shouldBeLessThanOrEqual 1305
        executeAutoFocus(exposureAmount = 3, fittingMode = HYPERBOLIC).shouldNotBeNull().x.roundToInt() shouldBeGreaterThanOrEqual 1295 shouldBeLessThanOrEqual 1305
    }

    @Test
    fun parabolic() {
        executeAutoFocus(fittingMode = PARABOLIC).shouldNotBeNull().x.roundToInt() shouldBeGreaterThanOrEqual 1295 shouldBeLessThanOrEqual 1305
        executeAutoFocus(1000, fittingMode = PARABOLIC).shouldNotBeNull().x.roundToInt() shouldBeGreaterThanOrEqual 1295 shouldBeLessThanOrEqual 1305
        executeAutoFocus(1600, fittingMode = PARABOLIC).shouldNotBeNull().x.roundToInt() shouldBeGreaterThanOrEqual 1295 shouldBeLessThanOrEqual 1315
        executeAutoFocus(exposureAmount = 3, fittingMode = PARABOLIC).shouldNotBeNull().x.roundToInt() shouldBeGreaterThanOrEqual 1295 shouldBeLessThanOrEqual 1305
    }

    private data class DetectedStar(
        override val hfd: Double,
        override val x: Double = 0.0, override val y: Double = 0.0,
        override val snr: Double = 0.0, override val flux: Double = 0.0,
    ) : StarPoint

    private data class HyperbolicStarDetector(private val offset: Int = 13) : StarDetector<Path> {

        override fun detect(input: Path): List<StarPoint> {
            val index = FOCUS_LIST.indexOf(input)
            val hfd = max(1.0, ln(cosh(((index - offset) * 2).toDouble())))
            return listOf(DetectedStar(hfd))
        }
    }

    companion object {

        @JvmStatic val FOCUS_LIST = listOf(
            FOCUS_01_FITS, FOCUS_02_FITS, FOCUS_03_FITS, FOCUS_04_FITS, FOCUS_05_FITS,
            FOCUS_06_FITS, FOCUS_07_FITS, FOCUS_08_FITS, FOCUS_09_FITS, FOCUS_10_FITS,
            FOCUS_11_FITS, FOCUS_12_FITS, FOCUS_13_FITS, FOCUS_14_FITS, FOCUS_15_FITS,
            FOCUS_16_FITS, FOCUS_17_FITS, FOCUS_18_FITS, FOCUS_19_FITS, FOCUS_20_FITS,
            FOCUS_21_FITS, FOCUS_22_FITS, FOCUS_23_FITS, FOCUS_24_FITS, FOCUS_25_FITS,
            FOCUS_26_FITS,
        )

        @JvmStatic
        private fun executeAutoFocus(
            initialFocusPosition: Int = 1000,
            exposureAmount: Int = 1,
            initialOffsetSteps: Int = 4,
            stepSize: Int = 100,
            fittingMode: AutoFocusFittingMode = TREND_HYPERBOLIC,
            rSquaredThreshold: Double = 0.0,
            reverse: Boolean = false,
        ): CurvePoint? {
            val autoFocus = AutoFocus(HyperbolicStarDetector(), exposureAmount, initialOffsetSteps, stepSize, fittingMode, rSquaredThreshold, reverse, 25000)

            var focusPosition = initialFocusPosition
            var focusPoint: CurvePoint? = null

            while (focusPoint == null) {
                when (val result = autoFocus.determinate(focusPosition)) {
                    AutoFocusResult.Determinate -> continue
                    is AutoFocusResult.MoveFocuser -> focusPosition = if (result.relative) focusPosition + result.position else result.position
                    AutoFocusResult.TakeExposure -> autoFocus.add(FOCUS_LIST[focusPosition / 100])
                    is AutoFocusResult.Completed -> focusPoint = result.determinedFocusPoint
                    is AutoFocusResult.Failed -> break
                }

                // Thread.sleep(100)
            }

            return focusPoint
        }
    }
}
