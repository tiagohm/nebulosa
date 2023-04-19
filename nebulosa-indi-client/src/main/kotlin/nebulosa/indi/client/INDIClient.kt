package nebulosa.indi.client

import nebulosa.indi.client.device.DeviceProtocolHandler
import nebulosa.indi.device.MessageSender
import nebulosa.indi.protocol.io.INDIConnection
import nebulosa.indi.protocol.parser.INDIProtocolParser
import java.io.Closeable

interface INDIClient : INDIProtocolParser, MessageSender, Closeable {

    val connection: INDIConnection

    fun start()

    fun registerDeviceProtocolHandler(handler: DeviceProtocolHandler)

    fun unregisterDeviceProtocolHandler(handler: DeviceProtocolHandler)
}
