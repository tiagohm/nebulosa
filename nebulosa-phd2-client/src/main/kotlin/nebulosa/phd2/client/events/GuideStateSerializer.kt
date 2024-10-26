package nebulosa.phd2.client.events

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import nebulosa.guiding.GuideState
import java.util.*

data object GuideStateSerializer : StdSerializer<GuideState>(GuideState::class.java) {

    override fun serialize(value: GuideState, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(GUIDE_STATES[value])
    }

    private val GUIDE_STATES = EnumMap<GuideState, String>(GuideState::class.java).also {
        it[GuideState.STOPPED] = "Stopped"
        it[GuideState.SELECTED] = "Selected"
        it[GuideState.CALIBRATING] = "Calibrating"
        it[GuideState.GUIDING] = "Guiding"
        it[GuideState.LOST_LOCK] = "LostLock"
        it[GuideState.PAUSED] = "Paused"
        it[GuideState.LOOPING] = "Looping"
    }
}
