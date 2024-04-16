package nebulosa.alpaca.indi.client

import nebulosa.alpaca.api.AlpacaService
import nebulosa.alpaca.api.DeviceType
import nebulosa.alpaca.indi.device.cameras.ASCOMCamera
import nebulosa.alpaca.indi.device.focusers.ASCOMFocuser
import nebulosa.alpaca.indi.device.mounts.ASCOMMount
import nebulosa.alpaca.indi.device.wheels.ASCOMFilterWheel
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceEventHandler
import nebulosa.indi.device.INDIDeviceProvider
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraAttached
import nebulosa.indi.device.camera.CameraDetached
import nebulosa.indi.device.camera.GuideHead
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelAttached
import nebulosa.indi.device.filterwheel.FilterWheelDetached
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserAttached
import nebulosa.indi.device.focuser.FocuserDetached
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.gps.GPSAttached
import nebulosa.indi.device.gps.GPSDetached
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.guide.GuideOutputAttached
import nebulosa.indi.device.guide.GuideOutputDetached
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountAttached
import nebulosa.indi.device.mount.MountDetached
import nebulosa.indi.device.thermometer.Thermometer
import nebulosa.indi.device.thermometer.ThermometerAttached
import nebulosa.indi.device.thermometer.ThermometerDetached
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.log.loggerFor
import okhttp3.OkHttpClient
import java.util.*

data class AlpacaClient(
    val host: String, val port: Int,
    private val httpClient: OkHttpClient? = null,
) : INDIDeviceProvider {

    private val service = AlpacaService("http://$host:$port/", httpClient)
    private val handlers = LinkedHashSet<DeviceEventHandler>()
    private val cameras = HashMap<String, Camera>(2)
    private val mounts = HashMap<String, Mount>(2)
    private val wheels = HashMap<String, FilterWheel>(2)
    private val focusers = HashMap<String, Focuser>(2)
    private val gps = HashMap<String, GPS>(2)
    private val guideOutputs = HashMap<String, GuideOutput>(2)
    private val thermometers = HashMap<String, Thermometer>(2)

    override val id = UUID.randomUUID().toString()

    override fun registerDeviceEventHandler(handler: DeviceEventHandler) {
        handlers.add(handler)
    }

    override fun unregisterDeviceEventHandler(handler: DeviceEventHandler) {
        handlers.remove(handler)
    }

    override fun sendMessageToServer(message: INDIProtocol) {}

    internal fun fireOnEventReceived(event: DeviceEvent<*>) {
        handlers.forEach { it.onEventReceived(event) }
    }

    internal fun fireOnConnectionClosed() {
        handlers.forEach { it.onConnectionClosed() }
    }

    override fun cameras(): List<Camera> {
        return cameras.values.toList()
    }

    override fun camera(name: String): Camera? {
        return cameras[name] ?: cameras.values.find { it.name == name }
    }

    override fun mounts(): List<Mount> {
        return mounts.values.toList()
    }

    override fun mount(name: String): Mount? {
        return mounts[name] ?: mounts.values.find { it.name == name }
    }

    override fun focusers(): List<Focuser> {
        return focusers.values.toList()
    }

    override fun focuser(name: String): Focuser? {
        return focusers[name] ?: focusers.values.find { it.name == name }
    }

    override fun wheels(): List<FilterWheel> {
        return wheels.values.toList()
    }

    override fun wheel(name: String): FilterWheel? {
        return wheels[name] ?: wheels.values.find { it.name == name }
    }

    override fun gps(): List<GPS> {
        return gps.values.toList()
    }

    override fun gps(name: String): GPS? {
        return gps[name] ?: gps.values.find { it.name == name }
    }

    override fun guideOutputs(): List<GuideOutput> {
        return guideOutputs.values.toList()
    }

    override fun guideOutput(name: String): GuideOutput? {
        return guideOutputs[name] ?: guideOutputs.values.find { it.name == name }
    }

    override fun thermometers(): List<Thermometer> {
        return thermometers.values.toList()
    }

    override fun thermometer(name: String): Thermometer? {
        return thermometers[name] ?: thermometers.values.find { it.name == name }
    }

    fun discovery() {
        val response = service.management.configuredDevices().execute()

        if (response.isSuccessful) {
            val body = response.body() ?: return

            for (device in body.value) {
                when (device.type) {
                    DeviceType.CAMERA -> {
                        if (device.uid in cameras) continue

                        synchronized(cameras) {
                            with(ASCOMCamera(device, service.camera, this)) {
                                cameras[device.uid] = this
                                LOG.info("camera attached: {}", device.name)
                                fireOnEventReceived(CameraAttached(this))
                            }
                        }
                    }
                    DeviceType.TELESCOPE -> {
                        if (device.uid in mounts) continue

                        synchronized(mounts) {
                            with(ASCOMMount(device, service.telescope, this)) {
                                mounts[device.uid] = this
                                LOG.info("mount attached: {}", device.name)
                                fireOnEventReceived(MountAttached(this))
                            }
                        }
                    }
                    DeviceType.FILTER_WHEEL -> {
                        if (device.uid in wheels) continue

                        synchronized(wheels) {
                            with(ASCOMFilterWheel(device, service.filterWheel, this)) {
                                wheels[device.uid] = this
                                LOG.info("filter wheel attached: {}", device.name)
                                fireOnEventReceived(FilterWheelAttached(this))
                            }
                        }
                    }
                    DeviceType.FOCUSER -> {
                        if (device.uid in focusers) continue

                        synchronized(focusers) {
                            with(ASCOMFocuser(device, service.focuser, this)) {
                                focusers[device.uid] = this
                                LOG.info("focuser attached: {}", device.name)
                                fireOnEventReceived(FocuserAttached(this))
                            }
                        }
                    }
                    DeviceType.ROTATOR -> Unit
                    DeviceType.DOME -> Unit
                    DeviceType.SWITCH -> Unit
                    DeviceType.COVER_CALIBRATOR -> Unit
                    DeviceType.OBSERVING_CONDITIONS -> Unit
                    DeviceType.SAFETY_MONITOR -> Unit
                    DeviceType.VIDEO -> Unit
                }
            }
        } else {
            val body = response.errorBody()
            LOG.warn("unsuccessful response. code={}, body={}", response.code(), body?.string())
            body?.close()
        }
    }

    internal fun registerGPS(device: GPS) {
        if (device.id !in gps) {
            gps[device.id] = device
            fireOnEventReceived(GPSAttached(device))
        }
    }

    internal fun unregisterGPS(device: GPS) {
        if (device.id in gps) {
            gps.remove(device.id)
            fireOnEventReceived(GPSDetached(device))
        }
    }

    internal fun registerGuideHead(device: GuideHead) {
        if (device.id !in cameras) {
            cameras[device.id] = device
            fireOnEventReceived(CameraAttached(device))
        }
    }

    internal fun unregisterGuiderHead(device: GuideHead) {
        if (device.id in cameras) {
            cameras.remove(device.id)
            fireOnEventReceived(CameraDetached(device))
        }
    }

    internal fun registerGuideOutput(device: GuideOutput) {
        if (device.id !in guideOutputs) {
            guideOutputs[device.id] = device
            fireOnEventReceived(GuideOutputAttached(device))
        }
    }

    internal fun unregisterGuideOutput(device: GuideOutput) {
        if (device.id in guideOutputs) {
            guideOutputs.remove(device.id)
            fireOnEventReceived(GuideOutputDetached(device))
        }
    }

    internal fun registerThermometer(device: Thermometer) {
        if (device.id !in thermometers) {
            thermometers[device.id] = device
            fireOnEventReceived(ThermometerAttached(device))
        }
    }

    internal fun unregisterThermometer(device: Thermometer) {
        if (device.id in thermometers) {
            thermometers.remove(device.id)
            fireOnEventReceived(ThermometerDetached(device))
        }
    }

    override fun close() {
        for ((_, device) in cameras) {
            device.close()
            LOG.info("camera detached: {}", device.name)
            fireOnEventReceived(CameraDetached(device))
        }

        for ((_, device) in mounts) {
            device.close()
            LOG.info("mount detached: {}", device.name)
            fireOnEventReceived(MountDetached(device))
        }

        for ((_, device) in wheels) {
            device.close()
            LOG.info("filter wheel detached: {}", device.name)
            fireOnEventReceived(FilterWheelDetached(device))
        }

        for ((_, device) in focusers) {
            device.close()
            LOG.info("focuser detached: {}", device.name)
            fireOnEventReceived(FocuserDetached(device))
        }

        for ((_, device) in gps) {
            device.close()
            LOG.info("gps detached: {}", device.name)
            fireOnEventReceived(GPSDetached(device))
        }

        cameras.clear()
        mounts.clear()
        wheels.clear()
        focusers.clear()
        gps.clear()

        handlers.clear()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<AlpacaClient>()
    }
}
