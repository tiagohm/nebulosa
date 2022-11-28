package nebulosa.indi.protocol.connection

import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.io.INDIInputStream
import nebulosa.indi.protocol.io.INDIOutputStream
import java.io.Closeable

interface INDIConnection : INDIInputStream, INDIOutputStream, Closeable {

    val input: INDIInputStream

    val output: INDIOutputStream

    val isOpen: Boolean

    override fun readINDIProtocol() = input.readINDIProtocol()

    override fun writeINDIProtocol(message: INDIProtocol) = output.writeINDIProtocol(message)

    override fun flush() = output.flush()
}
