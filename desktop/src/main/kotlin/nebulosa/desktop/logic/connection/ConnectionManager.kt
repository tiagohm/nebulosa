package nebulosa.desktop.logic.connection

import nebulosa.desktop.logic.EventBus
import nebulosa.indi.INDIClient
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceEventHandler
import nebulosa.indi.device.DeviceProtocolHandler
import org.koin.core.component.KoinComponent

class ConnectionManager : DeviceEventHandler, KoinComponent {

    @Volatile private var client: INDIClient? = null
    @Volatile private var deviceHandler: DeviceProtocolHandler? = null

    override fun onEventReceived(event: DeviceEvent<*>) {
        if (event.device?.sender === client) {
            EventBus.DEVICE.post(event)
        }
    }

    fun isConnected() = client != null

    @Synchronized
    fun connect(
        host: String,
        port: Int,
    ) {
        disconnect()

        val client = INDIClient(host, port)
        val deviceHandler = DeviceProtocolHandler()
        deviceHandler.registerDeviceEventHandler(this)
        client.registerDeviceProtocolHandler(deviceHandler)
        deviceHandler.start()
        client.start()

        this.deviceHandler = deviceHandler
        this.client = client

        EventBus.CONNECTION.post(Connected(client))
    }

    @Synchronized
    fun disconnect() {
        if (client != null) {
            client!!.close()

            deviceHandler!!.close()
            deviceHandler = null

            EventBus.CONNECTION.post(Disconnected(client!!))

            client = null
        }
    }
}
