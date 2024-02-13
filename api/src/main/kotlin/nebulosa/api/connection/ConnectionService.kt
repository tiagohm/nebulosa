package nebulosa.api.connection

import nebulosa.alpaca.indi.client.AlpacaClient
import nebulosa.indi.client.INDIClient
import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceHub
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.thermometer.Thermometer
import nebulosa.log.error
import nebulosa.log.loggerFor
import okhttp3.OkHttpClient
import org.greenrobot.eventbus.EventBus
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerErrorException
import java.io.Closeable

@Service
class ConnectionService(
    private val eventBus: EventBus,
    private val connectionEventHandler: ConnectionEventHandler,
    private val httpClient: OkHttpClient,
) : Closeable {

    @Volatile private var deviceHub: DeviceHub? = null

    fun connectionStatus(): Boolean {
        return deviceHub != null
    }

    @Synchronized
    fun connect(host: String, port: Int, type: ConnectionType) {
        try {
            disconnect()

            deviceHub = when (type) {
                ConnectionType.INDI -> {
                    val client = INDIClient(host, port)
                    client.registerDeviceEventHandler(eventBus::post)
                    client.registerDeviceEventHandler(connectionEventHandler)
                    client.start()
                    client
                }
                else -> {
                    val client = AlpacaClient(host, port, httpClient)
                    client.registerDeviceEventHandler(eventBus::post)
                    client.registerDeviceEventHandler(connectionEventHandler)
                    client.discovery()
                    client
                }
            }
        } catch (e: Throwable) {
            LOG.error(e)

            throw ServerErrorException("Connection Failed", e)
        }
    }

    @Synchronized
    fun disconnect() {
        (deviceHub as? Closeable)?.close()
        deviceHub = null
    }

    override fun close() {
        disconnect()
    }

    fun cameras(): List<Camera> {
        return deviceHub?.cameras() ?: emptyList()
    }

    fun mounts(): List<Mount> {
        return deviceHub?.mounts() ?: emptyList()
    }

    fun focusers(): List<Focuser> {
        return deviceHub?.focusers() ?: emptyList()
    }

    fun wheels(): List<FilterWheel> {
        return deviceHub?.wheels() ?: emptyList()
    }

    fun gps(): List<GPS> {
        return deviceHub?.gps() ?: emptyList()
    }

    fun guideOutputs(): List<GuideOutput> {
        return deviceHub?.guideOutputs() ?: emptyList()
    }

    fun thermometers(): List<Thermometer> {
        return deviceHub?.thermometers() ?: emptyList()
    }

    fun camera(name: String): Camera? {
        return deviceHub?.camera(name)
    }

    fun mount(name: String): Mount? {
        return deviceHub?.mount(name)
    }

    fun focuser(name: String): Focuser? {
        return deviceHub?.focuser(name)
    }

    fun wheel(name: String): FilterWheel? {
        return deviceHub?.wheel(name)
    }

    fun gps(name: String): GPS? {
        return deviceHub?.gps(name)
    }

    fun guideOutput(name: String): GuideOutput? {
        return deviceHub?.guideOutput(name)
    }

    fun thermometer(name: String): Thermometer? {
        return deviceHub?.thermometer(name)
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
