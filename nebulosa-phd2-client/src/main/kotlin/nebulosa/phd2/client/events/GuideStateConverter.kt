package nebulosa.phd2.client.events

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import nebulosa.guiding.GuideState
import nebulosa.json.FromJson
import nebulosa.json.ToJson
import java.util.*

object GuideStateConverter : ToJson<GuideState>, FromJson<GuideState> {

    override val type = GuideState::class.java

    override fun serialize(value: GuideState, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(GUIDE_STATES[value])
    }

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): GuideState? {
        return p.valueAsString?.let(GUIDE_STATE_NAMES::get)
    }

    @JvmStatic private val GUIDE_STATES = EnumMap<GuideState, String>(GuideState::class.java).also {
        it[GuideState.STOPPED] = "Stopped"
        it[GuideState.SELECTED] = "Selected"
        it[GuideState.CALIBRATING] = "Calibrating"
        it[GuideState.GUIDING] = "Guiding"
        it[GuideState.LOST_LOCK] = "LostLock"
        it[GuideState.PAUSED] = "Paused"
        it[GuideState.LOOPING] = "Looping"
    }

    @JvmStatic private val GUIDE_STATE_NAMES = GUIDE_STATES.map { it.value to it.key }.toMap()
}
