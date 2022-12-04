package nebulosa.api.connection

import jakarta.validation.Valid
import nebulosa.indi.INDIClient
import nebulosa.indi.devices.Device
import nebulosa.indi.devices.DeviceEventHandler
import nebulosa.indi.devices.DeviceProtocolHandler
import nebulosa.indi.devices.events.DeviceEvent
import nebulosa.indi.protocol.connection.INDIProccessConnection
import nebulosa.indi.protocol.connection.INDISocketConnection
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("connection")
class ConnectionController : DeviceEventHandler {

    private var client: INDIClient? = null
    private var deviceHandler: DeviceProtocolHandler? = null

    @Autowired
    private lateinit var eventBus: ApplicationEventPublisher

    @PostMapping("connect")
    fun connect(@RequestBody @Valid connect: Connect) {
        if (client == null) {
            val client = INDIClient(connect.host, connect.port)
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

    @PostMapping("disconnect")
    fun disconnect() {
        if (client != null) {
            client!!.close()

            deviceHandler?.close()
            deviceHandler = null

            eventBus.publishEvent(DisconnectedEvent(client!!))

            client = null
        }
    }

    @GetMapping("status")
    fun status(): ConnectionStatus {
        return when (val connection = client?.connection) {
            is INDISocketConnection -> ConnectionStatus(true, connection.host, connection.port)
            is INDIProccessConnection -> ConnectionStatus(true, "", 0)
            else -> ConnectionStatus(false, "", 0)
        }
    }

    override fun onEventReceived(device: Device, event: DeviceEvent<*>) {
        if (device.client === client) {
            eventBus.publishEvent(event)
        }
    }
}
