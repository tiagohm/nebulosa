package nebulosa.indi.connection

import nebulosa.indi.connection.io.INDIInputStream
import nebulosa.indi.connection.io.INDIOutputStream
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
