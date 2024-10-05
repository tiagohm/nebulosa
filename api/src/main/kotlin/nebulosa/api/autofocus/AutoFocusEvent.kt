package nebulosa.api.autofocus

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import nebulosa.api.cameras.CameraCaptureEvent
import nebulosa.api.message.MessageEvent
import nebulosa.curve.fitting.CurvePoint
import nebulosa.curve.fitting.HyperbolicFitting
import nebulosa.curve.fitting.QuadraticFitting
import nebulosa.curve.fitting.TrendLineFitting
import nebulosa.indi.device.camera.Camera

data class AutoFocusEvent(
    @JvmField val camera: Camera,
    @JvmField var state: AutoFocusState = AutoFocusState.IDLE,
    @JvmField var focusPoint: CurvePoint? = null,
    @JvmField var determinedFocusPoint: CurvePoint? = null,
    @JvmField var starCount: Int = 0,
    @JvmField var starHFD: Double = 0.0,
    @JvmField var chart: Chart? = null,
    @JvmField @field:JsonIgnoreProperties("camera") val capture: CameraCaptureEvent = CameraCaptureEvent(camera),
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
