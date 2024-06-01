package nebulosa.api.autofocus

import nebulosa.api.cameras.CameraCaptureEvent
import nebulosa.api.messages.MessageEvent
import nebulosa.curve.fitting.CurvePoint
import nebulosa.curve.fitting.HyperbolicFitting
import nebulosa.curve.fitting.QuadraticFitting
import nebulosa.curve.fitting.TrendLineFitting

data class AutoFocusEvent(
    @JvmField val state: AutoFocusState = AutoFocusState.IDLE,
    @JvmField val focusPoint: CurvePoint? = null,
    @JvmField val determinedFocusPoint: CurvePoint? = null,
    @JvmField val starCount: Int = 0,
    @JvmField val starHFD: Double = 0.0,
    @JvmField val chart: Chart? = null,
    @JvmField val capture: CameraCaptureEvent? = null,
) : MessageEvent {

    data class Chart(
        @JvmField val predictedFocusPoint: CurvePoint? = null,
        @JvmField val minX: Double = 0.0,
        @JvmField val minY: Double = 0.0,
        @JvmField val maxX: Double = 0.0,
        @JvmField val maxY: Double = 0.0,
        @JvmField val trendLine: TrendLineFitting.Curve? = null,
        @JvmField val parabolic: QuadraticFitting.Curve? = null,
        @JvmField val hyperbolic: HyperbolicFitting.Curve? = null,
    )

    override val eventName = "AUTO_FOCUS.ELAPSED"
}
