package nebulosa.api.connection

import jakarta.annotation.PostConstruct
import nebulosa.indi.client.DefaultINDIClient
import nebulosa.indi.client.INDIClient
import nebulosa.indi.client.device.DeviceProtocolHandler
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceEventHandler
import nebulosa.log.error
import nebulosa.log.loggerFor
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerErrorException
import java.io.Closeable
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

@Service
class ConnectionService(
    eventHandlers: List<DeviceEventHandler>,
) : Closeable {

    @Volatile private var client: INDIClient? = null
    @Volatile private var deviceProtocolHandler: DeviceProtocolHandler? = null
    private val eventQueue = LinkedBlockingQueue<DeviceEvent<*>>()
    private val eventQueueHandler = EventQueueHandler(eventQueue, eventHandlers)

    @PostConstruct
    private fun initialize() {
        eventQueueHandler.start()
    }

    fun connectionStatus(): Boolean {
        return client != null
    }

    @Synchronized
    fun connect(host: String, port: Int) {
        try {
            disconnect()

            val client = DefaultINDIClient(host, port)
            val deviceProtocolHandler = DeviceProtocolHandler()
            deviceProtocolHandler.registerDeviceEventHandler { eventQueue.offer(it) }
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

    @Synchronized
    fun disconnect() {
        client?.close()
        client = null

        deviceProtocolHandler?.close()
        deviceProtocolHandler = null

        eventQueue.clear()
    }

    override fun close() {
        disconnect()
    }

    private class EventQueueHandler(
        private val queue: BlockingQueue<DeviceEvent<*>>,
        private val handlers: List<DeviceEventHandler>,
    ) : Thread("Event Queue Handler") {

        init {
            isDaemon = true
        }

        override fun run() {
            while (true) {
                val event = queue.take()
                handlers.forEach { it.onEventReceived(event) }
            }
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<ConnectionService>()
    }
}
