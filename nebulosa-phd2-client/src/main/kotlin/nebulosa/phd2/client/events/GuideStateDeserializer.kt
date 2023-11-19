package nebulosa.phd2.client.events

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import nebulosa.guiding.GuideState

data object GuideStateDeserializer : StdDeserializer<GuideState>(GuideState::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): GuideState? {
        return p.valueAsString?.let(GUIDE_STATE_NAMES::get)
    }

    @JvmStatic private val GUIDE_STATE_NAMES = mapOf(
        "Stopped" to GuideState.STOPPED,
        "Selected" to GuideState.SELECTED,
        "Calibrating" to GuideState.CALIBRATING,
        "Guiding" to GuideState.GUIDING,
        "LostLock" to GuideState.LOST_LOCK,
        "Paused" to GuideState.PAUSED,
        "Looping" to GuideState.LOOPING,
    )
}
