package nebulosa.api.connection

import nebulosa.api.messages.MessageEvent

data class ConnectionClosedWithClient(@JvmField val id: String) : MessageEvent {

    override val eventName = "CONNECTION.CLOSED"
}
