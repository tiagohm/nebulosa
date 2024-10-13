package nebulosa.api.connection

import nebulosa.api.message.MessageEvent

data class ClientConnectionClosed(@JvmField val id: String) : MessageEvent {

    override val eventName = "CONNECTION.CLOSED"
}
