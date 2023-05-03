package nebulosa.indi.protocol.io

import nebulosa.indi.protocol.INDIProtocol
import java.io.Closeable
import java.io.Flushable

interface INDIOutputStream : Closeable, Flushable {

    fun writeINDIProtocol(message: INDIProtocol)
}
