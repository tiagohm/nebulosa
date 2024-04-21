package nebulosa.api.alignment.polar.tppa

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import nebulosa.api.beans.converters.angle.DeclinationSerializer
import nebulosa.api.beans.converters.angle.RightAscensionSerializer
import nebulosa.api.messages.MessageEvent
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.mount.Mount
import nebulosa.math.Angle
import java.time.Duration

data class TPPAEvent(
    @JvmField val camera: Camera,
    @JvmField val mount: Mount? = null,
    @JvmField val state: TPPAState = TPPAState.IDLE,
    @JvmField val stepCount: Int = 0,
    @JvmField val elapsedTime: Duration = Duration.ZERO,
    @JvmField @field:JsonSerialize(using = RightAscensionSerializer::class) val rightAscension: Angle = 0.0,
    @JvmField @field:JsonSerialize(using = DeclinationSerializer::class) val declination: Angle = 0.0,
    @JvmField @field:JsonSerialize(using = DeclinationSerializer::class) val azimuthError: Angle = 0.0,
    @JvmField @field:JsonSerialize(using = DeclinationSerializer::class) val altitudeError: Angle = 0.0,
    @JvmField @JsonSerialize(using = DeclinationSerializer::class) val totalError: Angle = 0.0,
    @JvmField val azimuthErrorDirection: String = "",
    @JvmField val altitudeErrorDirection: String = "",
) : MessageEvent {

    override val eventName = "TPPA.ELAPSED"
}
