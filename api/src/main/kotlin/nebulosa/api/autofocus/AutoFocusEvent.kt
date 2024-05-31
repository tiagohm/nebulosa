package nebulosa.api.autofocus

import nebulosa.api.cameras.CameraCaptureEvent
import nebulosa.api.messages.MessageEvent
import nebulosa.curve.fitting.*
import nebulosa.nova.almanac.evenlySpacedNumbers

data class AutoFocusEvent(
    @JvmField val state: AutoFocusState = AutoFocusState.IDLE,
    @JvmField val focusPoint: CurvePoint? = null,
    @JvmField val determinedFocusPoint: CurvePoint? = null,
    @JvmField val starCount: Int = 0,
    @JvmField val starHFD: Double = 0.0,
    @JvmField val minX: Double = 0.0,
    @JvmField val minY: Double = 0.0,
    @JvmField val maxX: Double = 0.0,
    @JvmField val maxY: Double = 0.0,
    @JvmField val chart: Chart? = null,
    @JvmField val capture: CameraCaptureEvent? = null,
) : MessageEvent {

    data class Chart(
        @JvmField val trendLine: Map<String, Any?>? = null,
        @JvmField val parabolic: Map<String, Any?>? = null,
        @JvmField val hyperbolic: Map<String, Any?>? = null,
    )

    override val eventName = "AUTO_FOCUS.ELAPSED"

    companion object {

        @JvmStatic
        fun makeChart(
            points: List<CurvePoint>,
            trendLine: TrendLineFitting.Curve?,
            parabolic: QuadraticFitting.Curve?,
            hyperbolic: HyperbolicFitting.Curve?
        ) = with(evenlySpacedNumbers(points.first().x, points.last().x, 100)) {
            Chart(trendLine?.mapped(this), parabolic?.mapped(this), hyperbolic?.mapped(this))
        }

        @JvmStatic
        private fun TrendLineFitting.Curve.mapped(points: DoubleArray) = mapOf(
            "left" to left.mapped(points),
            "right" to right.mapped(points),
            "intersection" to intersection,
            "minimum" to minimum, "rSquared" to rSquared,
        )

        @JvmStatic
        private fun TrendLine.mapped(points: DoubleArray) = mapOf(
            "slope" to slope, "intercept" to intercept,
            "rSquared" to rSquared,
            "points" to makePoints(points)
        )

        @JvmStatic
        private fun QuadraticFitting.Curve.mapped(points: DoubleArray) = mapOf(
            "minimum" to minimum, "rSquared" to rSquared,
            "points" to makePoints(points)
        )

        @JvmStatic
        private fun HyperbolicFitting.Curve.mapped(points: DoubleArray) = mapOf(
            "a" to a, "b" to b, "p" to p,
            "minimum" to minimum, "rSquared" to rSquared,
            "points" to makePoints(points)
        )

        @Suppress("NOTHING_TO_INLINE")
        private inline fun Curve.makePoints(points: DoubleArray): List<CurvePoint> {
            return points.map { CurvePoint(it, this(it)) }
        }
    }
}
