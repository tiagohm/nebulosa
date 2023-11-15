package nebulosa.api.guiding

import nebulosa.api.services.MessageEvent

data class GuiderMessageEvent(
    override val eventName: String,
    val data: Any? = null,
) : MessageEvent
