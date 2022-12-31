package nebulosa.desktop.connections

import nebulosa.desktop.eventbus.EventBus
import nebulosa.indi.INDIClient
import nebulosa.indi.devices.DeviceEvent
import nebulosa.indi.devices.DeviceEventHandler
import nebulosa.indi.devices.DeviceProtocolHandler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ConnectionService : DeviceEventHandler, KoinComponent {

    private val eventBus by inject<EventBus>()

    @Volatile private var client: INDIClient? = null
    @Volatile private var deviceHandler: DeviceProtocolHandler? = null

    override fun onEventReceived(event: DeviceEvent<*>) {
        if (event.device?.client === client) {
            eventBus.post(event)
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

        eventBus.post(Connected(client))
    }

    @Synchronized
    fun disconnect() {
        if (client != null) {
            client!!.close()

            deviceHandler!!.close()
            deviceHandler = null

            eventBus.post(Disconnected(client!!))

            client = null
        }
    }
}
