package nebulosa.indi.protocol.io

import nebulosa.indi.protocol.INDIProtocol
import java.io.Flushable

interface INDIOutputStream : AutoCloseable, Flushable {

    fun writeINDIProtocol(message: INDIProtocol)
}
