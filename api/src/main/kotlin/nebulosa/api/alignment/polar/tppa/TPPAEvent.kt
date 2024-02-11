package nebulosa.api.alignment.polar.tppa

import nebulosa.api.messages.MessageEvent
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.mount.Mount
import nebulosa.math.Angle
import java.time.Duration

sealed interface TPPAEvent : MessageEvent {

    val camera: Camera

    val mount: Mount?

    val state: TPPAState

    val stepCount: Int

    val elapsedTime: Duration

    val rightAscension: Angle
        get() = 0.0

    val declination: Angle
        get() = 0.0

    val azimuth: Angle
        get() = 0.0

    val altitude: Angle
        get() = 0.0

    override val eventName
        get() = "TPPA_ALIGNMENT.ELAPSED"

    data class Slewing(
        override val camera: Camera,
        override val mount: Mount?,
        override val stepCount: Int,
        override val elapsedTime: Duration,
        override val rightAscension: Angle,
        override val declination: Angle,
    ) : TPPAEvent {

        override val state = TPPAState.SLEWING
    }

    data class Solving(
        override val camera: Camera,
        override val mount: Mount?,
        override val stepCount: Int,
        override val elapsedTime: Duration,
    ) : TPPAEvent {

        override val state = TPPAState.SOLVING
    }

    data class Solved(
        override val camera: Camera,
        override val mount: Mount?,
        override val stepCount: Int,
        override val elapsedTime: Duration,
        override val rightAscension: Angle,
        override val declination: Angle,
    ) : TPPAEvent {

        override val state = TPPAState.SOLVED
    }

    data class Computed(
        override val camera: Camera,
        override val mount: Mount?,
        override val stepCount: Int,
        override val elapsedTime: Duration,
        override val azimuth: Double,
        override val altitude: Double,
    ) : TPPAEvent {

        override val state = TPPAState.COMPUTED
    }

    data class Failed(
        override val camera: Camera,
        override val mount: Mount?,
        override val stepCount: Int,
        override val elapsedTime: Duration,
    ) : TPPAEvent {

        override val state = TPPAState.FAILED
    }

    data class Finished(
        override val camera: Camera,
        override val mount: Mount?,
    ) : TPPAEvent {

        override val stepCount = 0
        override val elapsedTime: Duration = Duration.ZERO
        override val state = TPPAState.FINISHED
    }
}
