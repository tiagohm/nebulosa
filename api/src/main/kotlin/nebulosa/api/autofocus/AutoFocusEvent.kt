package nebulosa.api.autofocus

import nebulosa.api.cameras.CameraCaptureEvent
import nebulosa.api.messages.MessageEvent
import nebulosa.curve.fitting.CurvePoint

data class AutoFocusEvent(
    @JvmField val state: AutoFocusState = AutoFocusState.IDLE,
    @JvmField val focusPoint: CurvePoint = CurvePoint.ZERO,
    @JvmField val capture: CameraCaptureEvent? = null,
) : MessageEvent {

    override val eventName = "AUTO_FOCUS.ELAPSED"
}
