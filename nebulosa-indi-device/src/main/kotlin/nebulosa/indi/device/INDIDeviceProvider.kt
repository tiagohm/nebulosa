package nebulosa.indi.device

import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.thermometer.Thermometer
import java.io.Closeable

interface INDIDeviceProvider : MessageSender, Closeable {

    fun registerDeviceEventHandler(handler: DeviceEventHandler): Boolean

    fun unregisterDeviceEventHandler(handler: DeviceEventHandler): Boolean

    fun device(id: String): Device? {
        return camera(id) ?: mount(id) ?: focuser(id) ?: wheel(id)
        ?: gps(id) ?: guideOutput(id) ?: thermometer(id)
    }

    fun cameras(): List<Camera>

    fun camera(id: String): Camera?

    fun mounts(): List<Mount>

    fun mount(id: String): Mount?

    fun focusers(): List<Focuser>

    fun focuser(id: String): Focuser?

    fun wheels(): List<FilterWheel>

    fun wheel(id: String): FilterWheel?

    fun gps(): List<GPS>

    fun gps(id: String): GPS?

    fun guideOutputs(): List<GuideOutput>

    fun guideOutput(id: String): GuideOutput?

    fun thermometers(): List<Thermometer>

    fun thermometer(id: String): Thermometer?
}
