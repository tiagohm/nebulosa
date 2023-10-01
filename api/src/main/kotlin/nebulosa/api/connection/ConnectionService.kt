package nebulosa.api.connection

import nebulosa.indi.client.DefaultINDIClient
import nebulosa.indi.client.INDIClient
import nebulosa.indi.device.Device
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.thermometer.Thermometer
import nebulosa.log.error
import nebulosa.log.loggerFor
import org.greenrobot.eventbus.EventBus
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerErrorException
import java.io.Closeable

@Service
class ConnectionService(
    private val eventBus: EventBus,
    private val connectionEventHandler: ConnectionEventHandler,
) : Closeable {

    @Volatile private var client: INDIClient? = null

    fun connectionStatus(): Boolean {
        return client != null
    }

    @Synchronized
    fun connect(host: String, port: Int) {
        try {
            disconnect()

            val client = DefaultINDIClient(host, port)
            client.registerDeviceEventHandler(eventBus::post)
            client.registerDeviceEventHandler(connectionEventHandler)
            client.start()

            this.client = client
        } catch (e: Throwable) {
            LOG.error(e)

            throw ServerErrorException("Connection Failed", e)
        }
    }

    @Synchronized
    fun disconnect() {
        runCatching { client?.close() }
        client = null
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

    companion object {

        @JvmStatic private val LOG = loggerFor<ConnectionService>()
    }
}
