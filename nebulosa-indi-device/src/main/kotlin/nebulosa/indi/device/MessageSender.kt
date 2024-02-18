package nebulosa.indi.device

import nebulosa.indi.protocol.INDIProtocol

interface MessageSender {

    val id: String

    fun sendMessageToServer(message: INDIProtocol)
}
