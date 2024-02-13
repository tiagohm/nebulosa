package nebulosa.alpaca.indi.client

import nebulosa.alpaca.api.AlpacaService
import nebulosa.alpaca.api.DeviceType
import nebulosa.alpaca.indi.devices.ASCOMDevice
import nebulosa.alpaca.indi.devices.cameras.ASCOMCamera
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceEventHandler
import nebulosa.indi.device.INDIDeviceProvider
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraAttached
import nebulosa.indi.device.camera.CameraDetached
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.guide.GuideOutputAttached
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.thermometer.Thermometer
import nebulosa.log.loggerFor
import okhttp3.OkHttpClient

class AlpacaClient(
    host: String,
    port: Int,
    httpClient: OkHttpClient? = null,
) : INDIDeviceProvider {

    private val service = AlpacaService("http://$host:$port/", httpClient)
    private val handlers = LinkedHashSet<DeviceEventHandler>()
    private val cameras = HashMap<String, Camera>()
    private val guideOutputs = HashMap<String, GuideOutput>()

    override fun registerDeviceEventHandler(handler: DeviceEventHandler) {
        handlers.add(handler)
    }

    override fun unregisterDeviceEventHandler(handler: DeviceEventHandler) {
        handlers.remove(handler)
    }

    internal fun fireOnEventReceived(event: DeviceEvent<*>) {
        handlers.forEach { it.onEventReceived(event) }
    }

    override fun cameras(): List<Camera> {
        return synchronized(cameras) { cameras.values.toList() }
    }

    override fun camera(name: String): Camera? {
        return synchronized(cameras) { cameras[name] ?: cameras.values.find { it.name == name } }
    }

    override fun mounts(): List<Mount> {
        return emptyList()
    }

    override fun mount(name: String): Mount? {
        return null
    }

    override fun focusers(): List<Focuser> {
        return emptyList()
    }

    override fun focuser(name: String): Focuser? {
        return null
    }

    override fun wheels(): List<FilterWheel> {
        return emptyList()
    }

    override fun wheel(name: String): FilterWheel? {
        return null
    }

    override fun gps(): List<GPS> {
        return emptyList()
    }

    override fun gps(name: String): GPS? {
        return null
    }

    override fun guideOutputs(): List<GuideOutput> {
        return emptyList()
    }

    override fun guideOutput(name: String): GuideOutput? {
        return null
    }

    override fun thermometers(): List<Thermometer> {
        return emptyList()
    }

    override fun thermometer(name: String): Thermometer? {
        return null
    }

    fun discovery() {
        val response = service.management.configuredDevices().execute()

        if (response.isSuccessful) {
            val body = response.body() ?: return

            for (device in body.value) {
                if (device.type == DeviceType.CAMERA) {
                    if (device.uid in cameras) continue

                    synchronized(cameras) {
                        with(ASCOMCamera(device, service.camera, this)) {
                            cameras[device.uid] = this
                            LOG.info("camera attached: {}", device.name)
                            fireOnEventReceived(CameraAttached(this))
                        }
                    }
                }
            }
        } else {
            val body = response.errorBody()
            LOG.warn("unsuccessful response. code={}, body={}", response.code(), body?.string())
            body?.close()
        }
    }

    internal fun registerGuideOutput(device: GuideOutput) {
        if (device is ASCOMDevice) {
            guideOutputs[device.uid] = device
            fireOnEventReceived(GuideOutputAttached(device))
        }
    }

    override fun close() {
        for ((_, device) in cameras) {
            device.close()
            LOG.info("camera detached: {}", device.name)
            fireOnEventReceived(CameraDetached(device))
        }

        // for ((_, device) in mounts) {
        //     device.close()
        //     LOG.info("mount detached: {}", device.name)
        //     fireOnEventReceived(MountDetached(device))
        // }

        // for ((_, device) in wheels) {
        //     device.close()
        //     LOG.info("filter wheel detached: {}", device.name)
        //     fireOnEventReceived(FilterWheelDetached(device))
        // }

        // for ((_, device) in focusers) {
        //     device.close()
        //     LOG.info("focuser detached: {}", device.name)
        //     fireOnEventReceived(FocuserDetached(device))
        // }

        // for ((_, device) in gps) {
        //     device.close()
        //     LOG.info("gps detached: {}", device.name)
        //     fireOnEventReceived(GPSDetached(device))
        // }

        cameras.clear()
        // mounts.clear()
        // wheels.clear()
        // focusers.clear()
        // gps.clear()

        handlers.clear()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<AlpacaClient>()
    }
}
