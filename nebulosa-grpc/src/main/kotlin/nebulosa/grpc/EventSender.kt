package nebulosa.grpc

fun interface EventSender {

    fun sendEvent(event: Event)
}
