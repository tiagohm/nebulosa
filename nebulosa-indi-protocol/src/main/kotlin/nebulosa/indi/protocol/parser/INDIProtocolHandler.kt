package nebulosa.indi.protocol.parser

import nebulosa.indi.protocol.INDIProtocol

fun interface INDIProtocolHandler {

    fun handleMessage(message: INDIProtocol)
}
