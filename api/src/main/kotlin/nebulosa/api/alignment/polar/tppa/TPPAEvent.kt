package nebulosa.api.alignment.polar.tppa

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import nebulosa.api.beans.converters.angle.DeclinationSerializer
import nebulosa.api.beans.converters.angle.RightAscensionSerializer
import nebulosa.api.messages.MessageEvent
import nebulosa.math.Angle
import java.time.Duration
import kotlin.math.hypot

sealed interface TPPAEvent : MessageEvent {

    val id: String

    val state: TPPAState

    val stepCount: Int

    val elapsedTime: Duration

    val rightAscension: Angle
        get() = 0.0

    val declination: Angle
        get() = 0.0

    val azimuthError: Angle
        get() = 0.0

    val altitudeError: Angle
        get() = 0.0

    val totalError: Angle
        get() = 0.0

    val azimuthErrorDirection: String
        get() = ""

    val altitudeErrorDirection: String
        get() = ""

    override val eventName
        get() = "TPPA.ELAPSED"

    data class Slewing(
        override val id: String,
        override val stepCount: Int,
        override val elapsedTime: Duration,
        @field:JsonSerialize(using = RightAscensionSerializer::class) override val rightAscension: Angle,
        @field:JsonSerialize(using = DeclinationSerializer::class) override val declination: Angle,
    ) : TPPAEvent {

        override val state = TPPAState.SLEWING
    }

    data class Solving(
        override val id: String,
        override val stepCount: Int,
        override val elapsedTime: Duration,
    ) : TPPAEvent {

        override val state = TPPAState.SOLVING
    }

    data class Solved(
        override val id: String,
        override val stepCount: Int,
        override val elapsedTime: Duration,
        @field:JsonSerialize(using = RightAscensionSerializer::class) override val rightAscension: Angle,
        @field:JsonSerialize(using = DeclinationSerializer::class) override val declination: Angle,
    ) : TPPAEvent {

        override val state = TPPAState.SOLVED
    }

    data class Paused(
        override val id: String,
        override val stepCount: Int,
        override val elapsedTime: Duration,
    ) : TPPAEvent {

        override val state = TPPAState.PAUSED
    }

    data class Computed(
        override val id: String,
        override val stepCount: Int,
        override val elapsedTime: Duration,
        @field:JsonSerialize(using = DeclinationSerializer::class) override val azimuthError: Angle,
        @field:JsonSerialize(using = DeclinationSerializer::class) override val altitudeError: Angle,
        override val azimuthErrorDirection: String,
        override val altitudeErrorDirection: String,
    ) : TPPAEvent {

        @JsonSerialize(using = DeclinationSerializer::class) override val totalError = hypot(azimuthError, altitudeError)
        override val state = TPPAState.COMPUTED
    }

    data class Failed(
        override val id: String,
        override val stepCount: Int,
        override val elapsedTime: Duration,
    ) : TPPAEvent {

        override val state = TPPAState.FAILED
    }

    data class Finished(
        override val id: String,
    ) : TPPAEvent {

        override val stepCount = 0
        override val elapsedTime: Duration = Duration.ZERO
        override val state = TPPAState.FINISHED
    }
}
