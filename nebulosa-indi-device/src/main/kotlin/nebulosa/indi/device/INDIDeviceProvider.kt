package nebulosa.indi.device

import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.guider.GuideOutput
import nebulosa.indi.device.lightbox.LightBox
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.rotator.Rotator
import nebulosa.indi.device.thermometer.Thermometer

interface INDIDeviceProvider : MessageSender, AutoCloseable {

    fun registerDeviceEventHandler(handler: DeviceEventHandler): Boolean

    fun unregisterDeviceEventHandler(handler: DeviceEventHandler): Boolean

    fun fireOnEventReceived(event: DeviceEvent<*>)

    fun fireOnConnectionClosed()

    fun device(id: String): Collection<Device>

    fun cameras(): Collection<Camera>

    fun camera(id: String): Camera?

    fun mounts(): Collection<Mount>

    fun mount(id: String): Mount?

    fun focusers(): Collection<Focuser>

    fun focuser(id: String): Focuser?

    fun wheels(): Collection<FilterWheel>

    fun wheel(id: String): FilterWheel?

    fun rotators(): Collection<Rotator>

    fun rotator(id: String): Rotator?

    fun gps(): Collection<GPS>

    fun gps(id: String): GPS?

    fun guideOutputs(): Collection<GuideOutput>

    fun guideOutput(id: String): GuideOutput?

    fun lightBoxes(): Collection<LightBox>

    fun lightBox(id: String): LightBox?

    fun thermometers(): Collection<Thermometer>

    fun thermometer(id: String): Thermometer?
}
