package nebulosa.indi.protocol.connection

import nebulosa.indi.protocol.io.INDIProtocolFactory

class INDIProccessConnection(val process: Process) : INDIConnection {

    override val input = INDIProtocolFactory.createInputStream(process.inputStream)

    override val output = INDIProtocolFactory.createOutputStream(process.outputStream)

    override val isOpen get() = process.isAlive

    override fun close() {
        process.destroyForcibly().waitFor()
    }
}
