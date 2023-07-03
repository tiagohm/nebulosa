package nebulosa.api.services

import nebulosa.api.data.dtos.ConnectionRequest
import nebulosa.api.exceptions.ConnectionFailedException
import nebulosa.indi.client.DefaultINDIClient
import nebulosa.indi.client.INDIClient
import nebulosa.indi.client.device.DeviceProtocolHandler
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceEventHandler
import nebulosa.indi.device.camera.CameraEvent
import nebulosa.log.error
import nebulosa.log.loggerFor
import org.springframework.stereotype.Service
import java.io.Closeable

@Service
class ConnectionService(
    private val cameraService: CameraService,
) : DeviceEventHandler, Closeable {

    @Volatile private var client: INDIClient? = null
    @Volatile private var deviceProtocolHandler: DeviceProtocolHandler? = null

    override fun onEventReceived(event: DeviceEvent<*>) {
        when (event) {
            is CameraEvent -> cameraService.onCameraEventReceived(event)
        }
    }

    fun isConnected(): Boolean {
        return client != null
    }

    @Synchronized
    fun connect(connection: ConnectionRequest) {
        try {
            disconnect()

            val client = DefaultINDIClient(connection.host, connection.port)
            val deviceProtocolHandler = DeviceProtocolHandler()
            deviceProtocolHandler.registerDeviceEventHandler(this)
            client.registerDeviceProtocolHandler(deviceProtocolHandler)
            deviceProtocolHandler.start()
            client.start()

            this.deviceProtocolHandler = deviceProtocolHandler
            this.client = client
        } catch (e: Throwable) {
            LOG.error(e)

            throw ConnectionFailedException
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
