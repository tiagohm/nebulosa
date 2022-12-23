package nebulosa.server.connection

import nebulosa.indi.INDIClient
import nebulosa.indi.devices.DeviceEvent
import nebulosa.indi.devices.DeviceEventHandler
import nebulosa.indi.devices.DeviceProtocolHandler
import org.greenrobot.eventbus.EventBus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.atomic.AtomicReference

class ConnectionService : DeviceEventHandler, KoinComponent {

    private val eventBus by inject<EventBus>()
    private val client = AtomicReference<INDIClient>()
    private val deviceHandler = AtomicReference<DeviceProtocolHandler>()

    override fun onEventReceived(event: DeviceEvent<*>) {
        if (event.device?.client === client.get()) {
            eventBus.post(event)
        }
    }

    fun isConnected() = client.get() != null

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

        this.deviceHandler.set(deviceHandler)
        this.client.set(client)

        eventBus.post(Connected(client))
    }

    @Synchronized
    fun disconnect() {
        if (client.get() != null) {
            client.get().close()

            deviceHandler.get().close()
            deviceHandler.set(null)

            eventBus.post(Disconnected(client.get()))

            client.set(null)
        }
    }
}
