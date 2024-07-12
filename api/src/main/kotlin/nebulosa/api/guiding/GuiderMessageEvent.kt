package nebulosa.api.guiding

import nebulosa.api.message.MessageEvent

data class GuiderMessageEvent(override val eventName: String, @JvmField val data: Any? = null) : MessageEvent
