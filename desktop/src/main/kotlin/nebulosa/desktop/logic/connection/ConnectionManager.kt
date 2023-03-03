package nebulosa.desktop.logic.connection

import nebulosa.indi.client.DefaultINDIClient
import nebulosa.indi.client.INDIClient
import nebulosa.indi.client.device.DeviceProtocolHandler
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceEventHandler
import org.greenrobot.eventbus.EventBus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.Closeable

@Service
class ConnectionManager : DeviceEventHandler, Closeable {

    @Volatile private var client: INDIClient? = null
    @Volatile private var deviceHandler: DeviceProtocolHandler? = null

    @Autowired private lateinit var eventBus: EventBus

    override fun onEventReceived(event: DeviceEvent<*>) {
        if (event.device?.sender === client) {
            eventBus.post(event)
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

        eventBus.post(Connected(client))
    }

    fun disconnect() {
        if (client != null) {
            client!!.close()

            deviceHandler!!.close()
            deviceHandler = null

            eventBus.post(Disconnected(client!!))

            client = null
        }
    }

    override fun close() {
        disconnect()
    }
}
