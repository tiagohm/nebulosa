package nebulosa.indi.client.connection

import nebulosa.indi.client.io.INDIProtocolFactory
import nebulosa.indi.protocol.io.INDIConnection

data class INDIProcessConnection(private val process: Process) : INDIConnection {

    override val input = INDIProtocolFactory.createInputStream(process.inputStream)

    override val output = INDIProtocolFactory.createOutputStream(process.outputStream)

    override val isOpen
        get() = process.isAlive

    override fun close() {
        process.destroyForcibly().waitFor()
    }
}
