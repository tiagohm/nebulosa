package nebulosa.indi.devices

import nebulosa.indi.INDIClient
import nebulosa.indi.protocol.EnableBLOB
import nebulosa.indi.protocol.GetProperties
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.parser.INDIProtocolHandler
import java.io.Closeable

interface Device : INDIProtocolHandler, Closeable, Map<String, PropertyVector<*, *>> {

    val client: INDIClient

    val name: String

    val isConnected: Boolean

    fun sendMessageToServer(message: INDIProtocol)

    fun connect()

    fun disconnect()

    fun ask() {
        sendMessageToServer(GetProperties().also { it.device = name })
    }

    fun enableBlob() {
        sendMessageToServer(EnableBLOB().also { it.device = name })
    }
}
