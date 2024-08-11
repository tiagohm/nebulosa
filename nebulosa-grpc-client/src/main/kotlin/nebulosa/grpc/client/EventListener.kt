package nebulosa.grpc.client

import nebulosa.grpc.EventType

fun interface EventListener {

    fun onEventReceived(type: EventType, device: String)
}
