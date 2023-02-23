package nebulosa.desktop.logic.connection

import nebulosa.desktop.logic.ConnectionEventBus
import nebulosa.desktop.logic.DeviceEventBus
import nebulosa.indi.client.DefaultINDIClient
import nebulosa.indi.client.INDIClient
import nebulosa.indi.client.device.DeviceProtocolHandler
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceEventHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ConnectionManager : DeviceEventHandler {

    @Volatile private final var client: INDIClient? = null
    @Volatile private final var deviceHandler: DeviceProtocolHandler? = null

    @Autowired private final lateinit var connectionEventBus: ConnectionEventBus
    @Autowired private final lateinit var deviceEventBus: DeviceEventBus

    override fun onEventReceived(event: DeviceEvent<*>) {
        if (event.device?.sender === client) {
            deviceEventBus.onNext(event)
        }
    }

    fun isConnected() = client != null

    @Synchronized
    fun connect(
        host: String, port: Int,
    ) {
        disconnect()

        val client = DefaultINDIClient(host, port)
        val deviceHandler = DeviceProtocolHandler()
        deviceHandler.registerDeviceEventHandler(this)
        client.registerDeviceProtocolHandler(deviceHandler)
        deviceHandler.start()
        client.start()

        this.deviceHandler = deviceHandler
        this.client = client

        connectionEventBus.onNext(Connected(client))
    }

    @Synchronized
    fun disconnect() {
        if (client != null) {
            client!!.close()

            deviceHandler!!.close()
            deviceHandler = null

            connectionEventBus.onNext(Disconnected(client!!))

            client = null
        }
    }
}
