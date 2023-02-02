package nebulosa.indi.client

import nebulosa.indi.connection.INDIConnection
import nebulosa.indi.device.DeviceProtocolHandler
import nebulosa.indi.device.MessageSender
import nebulosa.indi.parser.INDIProtocolParser
import java.io.Closeable

interface INDIClient : INDIProtocolParser, MessageSender, Closeable {

    val connection: INDIConnection

    fun start()

    fun registerDeviceProtocolHandler(handler: DeviceProtocolHandler)

    fun unregisterDeviceProtocolHandler(handler: DeviceProtocolHandler)
}
