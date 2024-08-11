package nebulosa.grpc.driver

import nebulosa.grpc.EventSender

interface Driver {

    val name: String

    val connected: Boolean

    fun attach(sender: EventSender)

    fun detach(sender: EventSender)

    fun ask(name: String)
}
