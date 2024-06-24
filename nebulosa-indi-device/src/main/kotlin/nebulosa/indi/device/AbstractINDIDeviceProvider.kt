package nebulosa.indi.device

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
import nebulosa.indi.device.rotator.Rotator
import nebulosa.indi.device.rotator.RotatorAttached
import nebulosa.indi.device.rotator.RotatorDetached
import nebulosa.indi.device.thermometer.Thermometer
import nebulosa.indi.device.thermometer.ThermometerAttached
import nebulosa.indi.device.thermometer.ThermometerDetached
import nebulosa.log.loggerFor

abstract class AbstractINDIDeviceProvider : INDIDeviceProvider {

    private val handlers = LinkedHashSet<DeviceEventHandler>()

    private val cameras = HashMap<String, Camera>(2)
    private val mounts = HashMap<String, Mount>(2)
    private val wheels = HashMap<String, FilterWheel>(2)
    private val focusers = HashMap<String, Focuser>(2)
    private val rotators = HashMap<String, Rotator>(2)
    private val gps = HashMap<String, GPS>(2)
    private val guideOutputs = HashMap<String, GuideOutput>(2)
    private val thermometers = HashMap<String, Thermometer>(2)

    override fun registerDeviceEventHandler(handler: DeviceEventHandler) = handlers.add(handler)

    override fun unregisterDeviceEventHandler(handler: DeviceEventHandler) = handlers.remove(handler)

    fun fireOnEventReceived(event: DeviceEvent<*>) = handlers.forEach { it.onEventReceived(event) }

    fun fireOnConnectionClosed() = handlers.forEach { it.onConnectionClosed() }

    override fun cameras() = cameras.values

    override fun camera(id: String) = cameras[id] ?: cameras.values.find { it.name == id }

    override fun mounts() = mounts.values

    override fun mount(id: String) = mounts[id] ?: mounts.values.find { it.name == id }

    override fun focusers() = focusers.values

    override fun focuser(id: String) = focusers[id] ?: focusers.values.find { it.name == id }

    override fun wheels() = wheels.values

    override fun wheel(id: String) = wheels[id] ?: wheels.values.find { it.name == id }

    override fun rotators() = rotators.values

    override fun rotator(id: String) = rotators[id] ?: rotators.values.find { it.name == id }

    override fun gps() = gps.values

    override fun gps(id: String) = gps[id] ?: gps.values.find { it.name == id }

    override fun guideOutputs() = guideOutputs.values

    override fun guideOutput(id: String) = guideOutputs[id] ?: guideOutputs.values.find { it.name == id }

    override fun thermometers() = thermometers.values

    override fun thermometer(id: String) = thermometers[id] ?: thermometers.values.find { it.name == id }

    fun registerCamera(device: Camera): Boolean {
        if (device.id in cameras) return false
        cameras[device.id] = device
        fireOnEventReceived(CameraAttached(device))
        LOG.info("camera attached: {} ({})", device.name, device.id)
        return true
    }

    fun unregisterCamera(device: Camera) {
        fireOnEventReceived(CameraDetached(cameras.remove(device.id) ?: return))
        LOG.info("camera detached: {} ({})", device.name, device.id)
    }

    fun registerMount(device: Mount): Boolean {
        if (device.id in mounts) return false
        mounts[device.id] = device
        fireOnEventReceived(MountAttached(device))
        LOG.info("mount attached: {} ({})", device.name, device.id)
        return true
    }

    fun unregisterMount(device: Mount) {
        fireOnEventReceived(MountDetached(mounts.remove(device.id) ?: return))
        LOG.info("mount detached: {} ({})", device.name, device.id)
    }

    fun registerFocuser(device: Focuser): Boolean {
        if (device.id in focusers) return false
        focusers[device.id] = device
        fireOnEventReceived(FocuserAttached(device))
        LOG.info("focuser attached: {} ({})", device.name, device.id)
        return true
    }

    fun unregisterFocuser(device: Focuser) {
        fireOnEventReceived(FocuserDetached(focusers.remove(device.id) ?: return))
        LOG.info("focuser detached: {} ({})", device.name, device.id)
    }

    fun registerRotator(device: Rotator): Boolean {
        if (device.id in rotators) return false
        rotators[device.id] = device
        fireOnEventReceived(RotatorAttached(device))
        LOG.info("rotator attached: {} ({})", device.name, device.id)
        return true
    }

    fun unregisterRotator(device: Rotator) {
        fireOnEventReceived(RotatorDetached(rotators.remove(device.id) ?: return))
        LOG.info("rotator detached: {} ({})", device.name, device.id)
    }

    fun registerFilterWheel(device: FilterWheel): Boolean {
        if (device.id in wheels) return false
        wheels[device.id] = device
        fireOnEventReceived(FilterWheelAttached(device))
        LOG.info("filter wheel attached: {} ({})", device.name, device.id)
        return true
    }

    fun unregisterFilterWheel(device: FilterWheel) {
        fireOnEventReceived(FilterWheelDetached(wheels.remove(device.id) ?: return))
        LOG.info("filter wheel detached: {} ({})", device.name, device.id)
    }

    fun registerGPS(device: GPS): Boolean {
        if (device.id in gps) return false
        gps[device.id] = device
        fireOnEventReceived(GPSAttached(device))
        LOG.info("gps attached: {} ({})", device.name, device.id)
        return true
    }

    fun unregisterGPS(device: GPS) {
        fireOnEventReceived(GPSDetached(gps.remove(device.id) ?: return))
        LOG.info("gps detached: {} ({})", device.name, device.id)
    }

    fun registerGuideHead(device: GuideHead): Boolean {
        if (device.id in cameras) return false
        cameras[device.id] = device
        fireOnEventReceived(CameraAttached(device))
        LOG.info("guide head attached: {} ({})", device.name, device.id)
        return true
    }

    fun unregisterGuiderHead(device: GuideHead) {
        fireOnEventReceived(CameraDetached(cameras.remove(device.id) ?: return))
        LOG.info("guide head detached: {} ({})", device.name, device.id)
    }

    fun registerGuideOutput(device: GuideOutput): Boolean {
        if (device.id in guideOutputs) return false
        guideOutputs[device.id] = device
        fireOnEventReceived(GuideOutputAttached(device))
        LOG.info("guide output attached: {} ({})", device.name, device.id)
        return true
    }

    fun unregisterGuideOutput(device: GuideOutput) {
        fireOnEventReceived(GuideOutputDetached(guideOutputs.remove(device.id) ?: return))
        LOG.info("guide output detached: {} ({})", device.name, device.id)
    }

    fun registerThermometer(device: Thermometer): Boolean {
        if (device.id in thermometers) return false
        thermometers[device.id] = device
        fireOnEventReceived(ThermometerAttached(device))
        LOG.info("thermometer attached: {} ({})", device.name, device.id)
        return true
    }

    fun unregisterThermometer(device: Thermometer) {
        fireOnEventReceived(ThermometerDetached(thermometers.remove(device.id) ?: return))
        LOG.info("thermometer detached: {} ({})", device.name, device.id)
    }

    override fun close() {
        cameras().toList().onEach(Device::close).onEach(::unregisterCamera)
        mounts().toList().onEach(Device::close).onEach(::unregisterMount)
        wheels().toList().onEach(Device::close).onEach(::unregisterFilterWheel)
        focusers().toList().onEach(Device::close).onEach(::unregisterFocuser)
        rotators().toList().onEach(Device::close).onEach(::unregisterRotator)
        gps().toList().onEach(Device::close).onEach(::unregisterGPS)

        cameras.clear()
        mounts.clear()
        wheels.clear()
        focusers.clear()
        rotators.clear()
        gps.clear()
        guideOutputs.clear()
        thermometers.clear()

        handlers.clear()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<AbstractINDIDeviceProvider>()
    }
}
