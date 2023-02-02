package nebulosa.indi.parser

import nebulosa.indi.protocol.INDIProtocol

interface INDIProtocolHandler {

    fun handleMessage(message: INDIProtocol)
}
