package nebulosa.api.services

import nebulosa.indi.client.DefaultINDIClient
import nebulosa.indi.client.INDIClient
import nebulosa.indi.client.device.DeviceProtocolHandler
import nebulosa.log.error
import nebulosa.log.loggerFor
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerErrorException
import java.io.Closeable

@Service
class ConnectionService(
    private val equipmentService: EquipmentService,
) : Closeable {

    @Volatile private var client: INDIClient? = null
    @Volatile private var deviceProtocolHandler: DeviceProtocolHandler? = null

    fun connectionStatus(): Boolean {
        return client != null
    }

    @Synchronized
    fun connect(host: String, port: Int) {
        try {
            disconnect()

            val client = DefaultINDIClient(host, port)
            val deviceProtocolHandler = DeviceProtocolHandler()
            deviceProtocolHandler.registerDeviceEventHandler(equipmentService)
            client.registerDeviceProtocolHandler(deviceProtocolHandler)
            deviceProtocolHandler.start()
            client.start()

            this.deviceProtocolHandler = deviceProtocolHandler
            this.client = client
        } catch (e: Throwable) {
            LOG.error(e)

            throw ServerErrorException("Connection Failed", e)
        }
    }

    fun disconnect() {
        client?.close()
        client = null

        deviceProtocolHandler?.close()
        deviceProtocolHandler = null
    }

    override fun close() {
        disconnect()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<ConnectionService>()
    }
}
