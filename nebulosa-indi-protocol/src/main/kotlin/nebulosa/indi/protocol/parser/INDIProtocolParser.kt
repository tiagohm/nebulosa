package nebulosa.indi.protocol.parser

import nebulosa.indi.protocol.io.INDIInputStream

interface INDIProtocolParser : INDIProtocolHandler, AutoCloseable {

    val input: INDIInputStream?

    val isClosed: Boolean
}
