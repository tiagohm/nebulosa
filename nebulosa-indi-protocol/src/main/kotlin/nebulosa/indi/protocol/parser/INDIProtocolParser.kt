package nebulosa.indi.protocol.parser

import nebulosa.indi.protocol.io.INDIInputStream
import java.io.Closeable

interface INDIProtocolParser : INDIProtocolHandler, Closeable {

    val input: INDIInputStream?
}
