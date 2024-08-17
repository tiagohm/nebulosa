package nebulosa.api.connection

import nebulosa.alpaca.indi.client.AlpacaClient
import nebulosa.api.message.MessageService
import nebulosa.indi.client.INDIClient
import nebulosa.indi.client.connection.INDISocketConnection
import nebulosa.indi.device.Device
import nebulosa.indi.device.DeviceEventHandler
import nebulosa.indi.device.INDIDeviceProvider
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.rotator.Rotator
import nebulosa.indi.device.thermometer.Thermometer
import nebulosa.log.error
import nebulosa.log.loggerFor
import okhttp3.OkHttpClient
import org.greenrobot.eventbus.EventBus
import org.springframework.stereotype.Service
import org.springframework.web.server.ServerErrorException

@Service
class ConnectionService(
    private val eventBus: EventBus,
    private val connectionEventHub: ConnectionEventHub,
    private val alpacaHttpClient: OkHttpClient,
    private val messageService: MessageService,
) : AutoCloseable {

    private val providers = linkedMapOf<String, INDIDeviceProvider>()

    fun connectionStatuses(): List<ConnectionStatus> {
        return providers.keys.map { connectionStatus(it)!! }
    }

    fun connectionStatus(id: String): ConnectionStatus? {
        when (val client = providers[id]) {
            is INDIClient -> {
                when (val connection = client.connection) {
                    is INDISocketConnection -> {
                        return ConnectionStatus(id, ConnectionType.INDI, connection.remoteHost, connection.remotePort, connection.remoteIP)
                    }
                }
            }
            is AlpacaClient -> {
                return ConnectionStatus(id, ConnectionType.ALPACA, client.host, client.port)
            }
        }

        return null
    }

    @Synchronized
    fun connect(host: String, port: Int, type: ConnectionType): String {
        try {
            val provider = when (type) {
                ConnectionType.INDI -> {
                    val client = INDIClient(host, port)
                    client.registerDeviceEventHandler(DeviceEventHandler.EventReceived(eventBus::post))
                    client.registerDeviceEventHandler(connectionEventHub)
                    client.registerDeviceEventHandler(DeviceEventHandler.ConnectionClosed { sendConnectionClosedEvent(client) })
                    client.start()
                    client
                }
                else -> {
                    val client = AlpacaClient(host, port, alpacaHttpClient)
                    client.registerDeviceEventHandler(DeviceEventHandler.EventReceived(eventBus::post))
                    client.registerDeviceEventHandler(connectionEventHub)
                    client.registerDeviceEventHandler(DeviceEventHandler.ConnectionClosed { sendConnectionClosedEvent(client) })
                    client.discovery()
                    client
                }
            }

            providers[provider.id] = provider

            return provider.id
        } catch (e: Throwable) {
            LOG.error(e)

            throw ServerErrorException("Connection Failed", e)
        }
    }

    @Synchronized
    fun disconnect(id: String) {
        providers[id]?.close()
        providers.remove(id)
    }

    fun disconnectAll() {
        providers.forEach { it.value.close() }
        providers.clear()
    }

    private fun sendConnectionClosedEvent(provider: INDIDeviceProvider) {
        LOG.info("client connection was closed. id={}", provider.id)
        providers.remove(provider.id)
        messageService.sendMessage(ConnectionClosedWithClient(provider.id))
    }

    override fun close() {
        disconnectAll()
    }

    fun cameras(id: String): Collection<Camera> {
        return providers[id]?.cameras() ?: emptyList()
    }

    fun mounts(id: String): Collection<Mount> {
        return providers[id]?.mounts() ?: emptyList()
    }

    fun focusers(id: String): Collection<Focuser> {
        return providers[id]?.focusers() ?: emptyList()
    }

    fun wheels(id: String): Collection<FilterWheel> {
        return providers[id]?.wheels() ?: emptyList()
    }

    fun rotators(id: String): Collection<Rotator> {
        return providers[id]?.rotators() ?: emptyList()
    }

    fun gpss(id: String): Collection<GPS> {
        return providers[id]?.gps() ?: emptyList()
    }

    fun guideOutputs(id: String): Collection<GuideOutput> {
        return providers[id]?.guideOutputs() ?: emptyList()
    }

    fun thermometers(id: String): Collection<Thermometer> {
        return providers[id]?.thermometers() ?: emptyList()
    }

    fun cameras(): List<Camera> {
        return providers.values.flatMap { it.cameras() }
    }

    fun mounts(): List<Mount> {
        return providers.values.flatMap { it.mounts() }
    }

    fun focusers(): List<Focuser> {
        return providers.values.flatMap { it.focusers() }
    }

    fun wheels(): List<FilterWheel> {
        return providers.values.flatMap { it.wheels() }
    }

    fun rotators(): List<Rotator> {
        return providers.values.flatMap { it.rotators() }
    }

    fun gpss(): List<GPS> {
        return providers.values.flatMap { it.gps() }
    }

    fun guideOutputs(): List<GuideOutput> {
        return providers.values.flatMap { it.guideOutputs() }
    }

    fun thermometers(): List<Thermometer> {
        return providers.values.flatMap { it.thermometers() }
    }

    fun camera(id: String, name: String): Camera? {
        return providers[id]?.camera(name)
    }

    fun mount(id: String, name: String): Mount? {
        return providers[id]?.mount(name)
    }

    fun focuser(id: String, name: String): Focuser? {
        return providers[id]?.focuser(name)
    }

    fun wheel(id: String, name: String): FilterWheel? {
        return providers[id]?.wheel(name)
    }

    fun rotator(id: String, name: String): Rotator? {
        return providers[id]?.rotator(name)
    }

    fun gps(id: String, name: String): GPS? {
        return providers[id]?.gps(name)
    }

    fun guideOutput(id: String, name: String): GuideOutput? {
        return providers[id]?.guideOutput(name)
    }

    fun thermometer(id: String, name: String): Thermometer? {
        return providers[id]?.thermometer(name)
    }

    fun camera(name: String): Camera? {
        return providers.firstNotNullOfOrNull { it.value.camera(name) }
    }

    fun mount(name: String): Mount? {
        return providers.firstNotNullOfOrNull { it.value.mount(name) }
    }

    fun focuser(name: String): Focuser? {
        return providers.firstNotNullOfOrNull { it.value.focuser(name) }
    }

    fun wheel(name: String): FilterWheel? {
        return providers.firstNotNullOfOrNull { it.value.wheel(name) }
    }

    fun rotator(name: String): Rotator? {
        return providers.firstNotNullOfOrNull { it.value.rotator(name) }
    }

    fun gps(name: String): GPS? {
        return providers.firstNotNullOfOrNull { it.value.gps(name) }
    }

    fun guideOutput(name: String): GuideOutput? {
        return providers.firstNotNullOfOrNull { it.value.guideOutput(name) }
    }

    fun thermometer(name: String): Thermometer? {
        return providers.firstNotNullOfOrNull { it.value.thermometer(name) }
    }

    fun device(name: String): Device? {
        return camera(name)
            ?: mount(name)
            ?: focuser(name)
            ?: wheel(name)
            ?: rotator(name)
            ?: guideOutput(name)
            ?: gps(name)
            ?: thermometer(name)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<ConnectionService>()
    }
}
