package nebulosa.indi.device

import nebulosa.indi.protocol.INDIProtocol

interface MessageSender {

    fun sendMessageToServer(message: INDIProtocol)
}
