package nebulosa.api.alignment.polar.tppa

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import nebulosa.api.beans.converters.angle.DeclinationSerializer
import nebulosa.api.beans.converters.angle.RightAscensionSerializer
import nebulosa.api.cameras.CameraCaptureEvent
import nebulosa.api.message.MessageEvent
import nebulosa.indi.device.camera.Camera
import nebulosa.math.Angle

data class TPPAEvent(
    @JvmField val camera: Camera,
    @JvmField var state: TPPAState = TPPAState.IDLE,
    @JvmField @field:JsonSerialize(using = RightAscensionSerializer::class) var rightAscension: Angle = 0.0,
    @JvmField @field:JsonSerialize(using = DeclinationSerializer::class) var declination: Angle = 0.0,
    @JvmField @field:JsonSerialize(using = DeclinationSerializer::class) var azimuthError: Angle = 0.0,
    @JvmField @field:JsonSerialize(using = DeclinationSerializer::class) var altitudeError: Angle = 0.0,
    @JvmField @JsonSerialize(using = DeclinationSerializer::class) var totalError: Angle = 0.0,
    @JvmField var azimuthErrorDirection: String = "",
    @JvmField var altitudeErrorDirection: String = "",
    @JvmField val capture: CameraCaptureEvent = CameraCaptureEvent(camera),
    @JvmField var pausing: Boolean = false,
) : MessageEvent {

    override val eventName = "TPPA.ELAPSED"
}
