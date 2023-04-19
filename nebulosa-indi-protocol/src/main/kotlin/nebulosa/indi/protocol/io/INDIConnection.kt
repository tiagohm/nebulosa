package nebulosa.indi.protocol.io

import nebulosa.indi.protocol.INDIProtocol
import java.io.Closeable

interface INDIConnection : INDIInputStream, INDIOutputStream, Closeable {

    val input: INDIInputStream

    val output: INDIOutputStream

    val isOpen: Boolean

    override fun readINDIProtocol() = input.readINDIProtocol()

    override fun writeINDIProtocol(message: INDIProtocol) = output.writeINDIProtocol(message)

    override fun flush() = output.flush()
}
