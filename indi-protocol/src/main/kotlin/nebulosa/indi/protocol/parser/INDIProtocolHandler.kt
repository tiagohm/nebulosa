package nebulosa.indi.protocol.parser

import nebulosa.indi.protocol.INDIProtocol

interface INDIProtocolHandler {

    fun handleMessage(message: INDIProtocol)
}
