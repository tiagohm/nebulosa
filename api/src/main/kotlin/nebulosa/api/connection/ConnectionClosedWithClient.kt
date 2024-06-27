package nebulosa.api.connection

import nebulosa.api.message.MessageEvent

data class ConnectionClosedWithClient(@JvmField val id: String) : MessageEvent {

    override val eventName = "CONNECTION.CLOSED"
}
