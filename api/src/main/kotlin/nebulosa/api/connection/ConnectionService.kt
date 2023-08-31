package nebulosa.api.connection

import jakarta.annotation.PostConstruct
import nebulosa.indi.client.DefaultINDIClient
import nebulosa.indi.client.INDIClient
import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceEventHandler
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.thermometer.Thermometer
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
            client.registerDeviceEventHandler(eventQueue::offer)
            client.start()

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

        eventQueue.clear()
    }

    override fun close() {
        disconnect()
    }

    fun cameras(): List<Camera> {
        return client?.cameras() ?: emptyList()
    }

    fun mounts(): List<Mount> {
        return client?.mounts() ?: emptyList()
    }

    fun focusers(): List<Focuser> {
        return client?.focusers() ?: emptyList()
    }

    fun wheels(): List<FilterWheel> {
        return client?.wheels() ?: emptyList()
    }

    fun gps(): List<GPS> {
        return client?.gps() ?: emptyList()
    }

    fun guideOutputs(): List<GuideOutput> {
        return client?.guideOutputs() ?: emptyList()
    }

    fun thermometers(): List<Thermometer> {
        return client?.thermometers() ?: emptyList()
    }

    fun camera(name: String): Camera? {
        return client?.camera(name)
    }

    fun mount(name: String): Mount? {
        return client?.mount(name)
    }

    fun focuser(name: String): Focuser? {
        return client?.focuser(name)
    }

    fun wheel(name: String): FilterWheel? {
        return client?.wheel(name)
    }

    fun gps(name: String): GPS? {
        return client?.gps(name)
    }

    fun guideOutput(name: String): GuideOutput? {
        return client?.guideOutput(name)
    }

    fun thermometer(name: String): Thermometer? {
        return client?.thermometer(name)
    }

    fun device(name: String): Device? {
        return camera(name)
            ?: mount(name)
            ?: focuser(name)
            ?: wheel(name)
            ?: guideOutput(name)
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
