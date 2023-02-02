package nebulosa.indi.parser

import nebulosa.indi.connection.io.INDIInputStream
import java.io.Closeable

interface INDIProtocolParser : INDIProtocolHandler, Closeable {

    val input: INDIInputStream?
}
