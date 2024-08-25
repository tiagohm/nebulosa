package nebulosa.indi.device

import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.CameraAttached
import nebulosa.indi.device.camera.CameraDetached
import nebulosa.indi.device.camera.GuideHead
import nebulosa.indi.device.dustcap.DustCap
import nebulosa.indi.device.dustcap.DustCapAttached
import nebulosa.indi.device.dustcap.DustCapDetached
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.filterwheel.FilterWheelAttached
import nebulosa.indi.device.filterwheel.FilterWheelDetached
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserAttached
import nebulosa.indi.device.focuser.FocuserDetached
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.gps.GPSAttached
import nebulosa.indi.device.gps.GPSDetached
import nebulosa.indi.device.guider.GuideOutput
import nebulosa.indi.device.guider.GuideOutputAttached
import nebulosa.indi.device.guider.GuideOutputDetached
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
    private val dustCaps = HashMap<String, DustCap>(2)
    private val thermometers = HashMap<String, Thermometer>(2)

    override fun registerDeviceEventHandler(handler: DeviceEventHandler) = handlers.add(handler)

    override fun unregisterDeviceEventHandler(handler: DeviceEventHandler) = handlers.remove(handler)

    override fun fireOnEventReceived(event: DeviceEvent<*>) = handlers.forEach { it.onEventReceived(event) }

    override fun fireOnConnectionClosed() = handlers.forEach { it.onConnectionClosed() }

    override fun device(id: String): Collection<Device> {
        val devices = HashSet<Device>(2)
        camera(id)?.also(devices::add)
        mount(id)?.also(devices::add)
        focuser(id)?.also(devices::add)
        wheel(id)?.also(devices::add)
        rotator(id)?.also(devices::add)
        gps(id)?.also(devices::add)
        guideOutput(id)?.also(devices::add)
        dustCap(id)?.also(devices::add)
        thermometer(id)?.also(devices::add)
        return devices
    }

    override fun cameras() = cameras.values.toSet()

    override fun camera(id: String) = cameras[id]

    override fun mounts() = mounts.values.toSet()

    override fun mount(id: String) = mounts[id]

    override fun focusers() = focusers.values.toSet()

    override fun focuser(id: String) = focusers[id]

    override fun wheels() = wheels.values.toSet()

    override fun wheel(id: String) = wheels[id]

    override fun rotators() = rotators.values.toSet()

    override fun rotator(id: String) = rotators[id]

    override fun gps() = gps.values.toSet()

    override fun gps(id: String) = gps[id]

    override fun guideOutputs() = guideOutputs.values.toSet()

    override fun guideOutput(id: String) = guideOutputs[id]

    override fun dustCaps() = dustCaps.values.toSet()

    override fun dustCap(id: String) = dustCaps[id]

    override fun thermometers() = thermometers.values.toSet()

    override fun thermometer(id: String) = thermometers[id]

    fun registerCamera(device: Camera): Boolean {
        if (device.id in cameras) return false
        cameras[device.id] = device
        cameras[device.name] = device
        fireOnEventReceived(CameraAttached(device))
        LOG.info("camera attached: {} ({})", device.name, device.id)
        return true
    }

    fun unregisterCamera(device: Camera) {
        cameras.remove(device.name)
        fireOnEventReceived(CameraDetached(cameras.remove(device.id) ?: return))
        LOG.info("camera detached: {} ({})", device.name, device.id)
    }

    fun registerMount(device: Mount): Boolean {
        if (device.id in mounts) return false
        mounts[device.id] = device
        mounts[device.name] = device
        fireOnEventReceived(MountAttached(device))
        LOG.info("mount attached: {} ({})", device.name, device.id)
        return true
    }

    fun unregisterMount(device: Mount) {
        mounts.remove(device.name)
        fireOnEventReceived(MountDetached(mounts.remove(device.id) ?: return))
        LOG.info("mount detached: {} ({})", device.name, device.id)
    }

    fun registerFocuser(device: Focuser): Boolean {
        if (device.id in focusers) return false
        focusers[device.id] = device
        focusers[device.name] = device
        fireOnEventReceived(FocuserAttached(device))
        LOG.info("focuser attached: {} ({})", device.name, device.id)
        return true
    }

    fun unregisterFocuser(device: Focuser) {
        focusers.remove(device.name)
        fireOnEventReceived(FocuserDetached(focusers.remove(device.id) ?: return))
        LOG.info("focuser detached: {} ({})", device.name, device.id)
    }

    fun registerRotator(device: Rotator): Boolean {
        if (device.id in rotators) return false
        rotators[device.id] = device
        rotators[device.name] = device
        fireOnEventReceived(RotatorAttached(device))
        LOG.info("rotator attached: {} ({})", device.name, device.id)
        return true
    }

    fun unregisterRotator(device: Rotator) {
        rotators.remove(device.name)
        fireOnEventReceived(RotatorDetached(rotators.remove(device.id) ?: return))
        LOG.info("rotator detached: {} ({})", device.name, device.id)
    }

    fun registerFilterWheel(device: FilterWheel): Boolean {
        if (device.id in wheels) return false
        wheels[device.id] = device
        wheels[device.name] = device
        fireOnEventReceived(FilterWheelAttached(device))
        LOG.info("filter wheel attached: {} ({})", device.name, device.id)
        return true
    }

    fun unregisterFilterWheel(device: FilterWheel) {
        wheels.remove(device.name)
        fireOnEventReceived(FilterWheelDetached(wheels.remove(device.id) ?: return))
        LOG.info("filter wheel detached: {} ({})", device.name, device.id)
    }

    fun registerGPS(device: GPS): Boolean {
        if (device.id in gps) return false
        gps[device.id] = device
        gps[device.name] = device
        fireOnEventReceived(GPSAttached(device))
        LOG.info("gps attached: {} ({})", device.name, device.id)
        return true
    }

    fun unregisterGPS(device: GPS) {
        gps.remove(device.name)
        fireOnEventReceived(GPSDetached(gps.remove(device.id) ?: return))
        LOG.info("gps detached: {} ({})", device.name, device.id)
    }

    fun registerGuideHead(device: GuideHead): Boolean {
        if (device.id in cameras) return false
        cameras[device.id] = device
        cameras[device.name] = device
        fireOnEventReceived(CameraAttached(device))
        LOG.info("guide head attached: {} ({})", device.name, device.id)
        return true
    }

    fun unregisterGuiderHead(device: GuideHead) {
        cameras.remove(device.name)
        fireOnEventReceived(CameraDetached(cameras.remove(device.id) ?: return))
        LOG.info("guide head detached: {} ({})", device.name, device.id)
    }

    fun registerGuideOutput(device: GuideOutput): Boolean {
        if (device.id in guideOutputs) return false
        guideOutputs[device.id] = device
        guideOutputs[device.name] = device
        fireOnEventReceived(GuideOutputAttached(device))
        LOG.info("guide output attached: {} ({})", device.name, device.id)
        return true
    }

    fun unregisterGuideOutput(device: GuideOutput) {
        guideOutputs.remove(device.name)
        fireOnEventReceived(GuideOutputDetached(guideOutputs.remove(device.id) ?: return))
        LOG.info("guide output detached: {} ({})", device.name, device.id)
    }

    fun registerDustCap(device: DustCap): Boolean {
        if (device.id in dustCaps) return false
        dustCaps[device.id] = device
        dustCaps[device.name] = device
        fireOnEventReceived(DustCapAttached(device))
        LOG.info("dust cap attached: {} ({})", device.name, device.id)
        return true
    }

    fun unregisterDustCap(device: DustCap) {
        dustCaps.remove(device.name)
        fireOnEventReceived(DustCapDetached(dustCaps.remove(device.id) ?: return))
        LOG.info("dust cap detached: {} ({})", device.name, device.id)
    }

    fun registerThermometer(device: Thermometer): Boolean {
        if (device.id in thermometers) return false
        thermometers[device.id] = device
        thermometers[device.name] = device
        fireOnEventReceived(ThermometerAttached(device))
        LOG.info("thermometer attached: {} ({})", device.name, device.id)
        return true
    }

    fun unregisterThermometer(device: Thermometer) {
        thermometers.remove(device.name)
        fireOnEventReceived(ThermometerDetached(thermometers.remove(device.id) ?: return))
        LOG.info("thermometer detached: {} ({})", device.name, device.id)
    }

    override fun close() {
        cameras().onEach(Device::close).onEach(::unregisterCamera)
        mounts().onEach(Device::close).onEach(::unregisterMount)
        wheels().onEach(Device::close).onEach(::unregisterFilterWheel)
        focusers().onEach(Device::close).onEach(::unregisterFocuser)
        rotators().onEach(Device::close).onEach(::unregisterRotator)
        gps().onEach(Device::close).onEach(::unregisterGPS)
        guideOutputs().onEach(Device::close).onEach(::unregisterGuideOutput)
        dustCaps().onEach(Device::close).onEach(::unregisterDustCap)

        cameras.clear()
        mounts.clear()
        wheels.clear()
        focusers.clear()
        rotators.clear()
        gps.clear()
        guideOutputs.clear()
        dustCaps.clear()
        thermometers.clear()

        handlers.clear()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<AbstractINDIDeviceProvider>()
    }
}
