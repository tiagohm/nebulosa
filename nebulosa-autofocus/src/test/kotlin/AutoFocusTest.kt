import io.kotest.matchers.ints.shouldBeExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import nebulosa.autofocus.AutoFocus
import nebulosa.autofocus.AutoFocusFittingMode
import nebulosa.autofocus.AutoFocusFittingMode.*
import nebulosa.autofocus.AutoFocusResult
import nebulosa.curve.fitting.CurvePoint
import nebulosa.stardetector.StarDetector
import nebulosa.stardetector.StarPoint
import nebulosa.test.*
import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.math.cosh
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.roundToInt

class AutoFocusTest : AbstractTest() {

    @Test
    fun trendHyperbolic() {
        executeAutoFocus().shouldNotBeNull().x.roundToInt() shouldBeExactly 798
        executeAutoFocus(700).shouldNotBeNull().x.roundToInt() shouldBeExactly 800
        executeAutoFocus(1200).shouldNotBeNull().x.roundToInt() shouldBeExactly 798
        executeAutoFocus(exposureAmount = 3).shouldNotBeNull().x.roundToInt() shouldBeExactly 798
    }

    @Test
    fun trendParabolic() {
        executeAutoFocus(fittingMode = TREND_PARABOLIC).shouldNotBeNull().x.roundToInt() shouldBeExactly 807
        executeAutoFocus(700, fittingMode = TREND_PARABOLIC).shouldNotBeNull().x.roundToInt() shouldBeExactly 800
        executeAutoFocus(1200, fittingMode = TREND_PARABOLIC).shouldNotBeNull().x.roundToInt() shouldBeExactly 805
        executeAutoFocus(exposureAmount = 3, fittingMode = TREND_PARABOLIC).shouldNotBeNull().x.roundToInt() shouldBeExactly 807
    }

    @Test
    fun trendlines() {
        executeAutoFocus(fittingMode = TRENDLINES).shouldNotBeNull().x.roundToInt() shouldBeExactly 800
        executeAutoFocus(700, fittingMode = TRENDLINES).shouldNotBeNull().x.roundToInt() shouldBeExactly 800
        executeAutoFocus(1200, fittingMode = TRENDLINES).shouldNotBeNull().x.roundToInt() shouldBeExactly 800
        executeAutoFocus(exposureAmount = 3, fittingMode = TRENDLINES).shouldNotBeNull().x.roundToInt() shouldBeExactly 800
    }

    @Test
    fun hyperbolic() {
        executeAutoFocus(fittingMode = HYPERBOLIC).shouldNotBeNull().x.roundToInt() shouldBeExactly 796
        executeAutoFocus(700, fittingMode = HYPERBOLIC).shouldNotBeNull().x.roundToInt() shouldBeExactly 800
        executeAutoFocus(1200, fittingMode = HYPERBOLIC).shouldNotBeNull().x.roundToInt() shouldBeExactly 795
        executeAutoFocus(exposureAmount = 3, fittingMode = HYPERBOLIC).shouldNotBeNull().x.roundToInt() shouldBeExactly 796
    }

    @Test
    fun parabolic() {
        executeAutoFocus(fittingMode = PARABOLIC).shouldNotBeNull().x.roundToInt() shouldBeExactly 814
        executeAutoFocus(700, fittingMode = PARABOLIC).shouldNotBeNull().x.roundToInt() shouldBeExactly 800
        executeAutoFocus(1200, fittingMode = PARABOLIC).shouldNotBeNull().x.roundToInt() shouldBeExactly 809
        executeAutoFocus(exposureAmount = 3, fittingMode = PARABOLIC).shouldNotBeNull().x.roundToInt() shouldBeExactly 814
    }

    private data class DetectedStar(
        override val hfd: Double,
        override val x: Double = 0.0, override val y: Double = 0.0,
        override val snr: Double = 0.0, override val flux: Double = 0.0,
    ) : StarPoint

    private data class HyperbolicStarDetector(private val offset: Int = 8) : StarDetector<Path> {

        override fun detect(input: Path): List<StarPoint> {
            val index = STAR_FOCUS_LIST.indexOf(input)
            val hfd = max(1.0, ln(cosh(((index - offset) * 2).toDouble())))
            return listOf(DetectedStar(hfd))
        }
    }

    companion object {

        @JvmStatic val STAR_FOCUS_LIST = listOf(
            STAR_FOCUS_1, STAR_FOCUS_2, STAR_FOCUS_3, STAR_FOCUS_4, STAR_FOCUS_5,
            STAR_FOCUS_6, STAR_FOCUS_7, STAR_FOCUS_8, STAR_FOCUS_9, STAR_FOCUS_10,
            STAR_FOCUS_11, STAR_FOCUS_12, STAR_FOCUS_13, STAR_FOCUS_14, STAR_FOCUS_15,
            STAR_FOCUS_16, STAR_FOCUS_17,
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
            val autoFocus =
                AutoFocus(HyperbolicStarDetector(), exposureAmount, initialOffsetSteps, stepSize, fittingMode, rSquaredThreshold, reverse, 16000)

            var focusPosition = initialFocusPosition
            var focusPoint: CurvePoint? = null

            while (focusPoint == null) {
                when (val result = autoFocus.determinate(focusPosition)) {
                    AutoFocusResult.Determinate -> continue
                    is AutoFocusResult.MoveFocuser -> focusPosition = if (result.relative) focusPosition + result.position else result.position
                    AutoFocusResult.TakeExposure -> autoFocus.add(STAR_FOCUS_LIST[focusPosition / 100])
                    is AutoFocusResult.Completed -> focusPoint = result.point
                    is AutoFocusResult.Failed -> break
                }

                // Thread.sleep(100)
            }

            return focusPoint
        }
    }
}
