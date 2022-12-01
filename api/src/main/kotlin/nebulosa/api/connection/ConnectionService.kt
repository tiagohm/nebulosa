package nebulosa.api.connection

import nebulosa.indi.INDIClient
import nebulosa.indi.devices.Device
import nebulosa.indi.devices.DeviceEventHandler
import nebulosa.indi.devices.DeviceProtocolHandler
import nebulosa.indi.devices.events.DeviceEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class ConnectionService : DeviceEventHandler {

    private var client: INDIClient? = null
    private var deviceHandler: DeviceProtocolHandler? = null

    @Autowired
    private lateinit var eventBus: ApplicationEventPublisher

    @Synchronized
    fun connection(
        host: String,
        port: Int,
    ) {
        if (client == null) {
            val client = INDIClient(host, port)
            val deviceHandler = DeviceProtocolHandler()
            deviceHandler.registerDeviceEventHandler(this)
            client.registerDeviceProtocolHandler(deviceHandler)
            deviceHandler.start()
            client.start()

            this.deviceHandler = deviceHandler
            this.client = client

            eventBus.publishEvent(ConnectedEvent(client))
        } else {
            throw IllegalStateException("client already connected")
        }
    }

    fun connection(connectionReq: ConnectionReq) {
        return connection(connectionReq.host, connectionReq.port)
    }

    @Synchronized
    fun disconnect() {
        if (client != null) {
            client!!.close()

            deviceHandler?.close()
            deviceHandler = null

            eventBus.publishEvent(DisconnectedEvent(client!!))

            client = null
        }
    }

    fun status(): ConnectionStatusRes {
        return ConnectionStatusRes(client != null)
    }

    override fun onEventReceived(device: Device, event: DeviceEvent<*>) {
        if (device.client === client) {
            eventBus.publishEvent(event)
        }
    }
}
