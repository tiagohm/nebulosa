package nebulosa.indi.client

import nebulosa.indi.device.MessageSender
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.thermometer.Thermometer
import nebulosa.indi.protocol.io.INDIConnection
import nebulosa.indi.protocol.parser.INDIProtocolParser

interface INDIClient : INDIProtocolParser, MessageSender {

    val connection: INDIConnection

    fun start()

    fun cameras(): List<Camera>

    fun camera(name: String): Camera?

    fun mounts(): List<Mount>

    fun mount(name: String): Mount?

    fun focusers(): List<Focuser>

    fun focuser(name: String): Focuser?

    fun wheels(): List<FilterWheel>

    fun wheel(name: String): FilterWheel?

    fun gps(): List<GPS>

    fun gps(name: String): GPS?

    fun guideOutputs(): List<GuideOutput>

    fun guideOutput(name: String): GuideOutput?

    fun thermometers(): List<Thermometer>

    fun thermometer(name: String): Thermometer?
}
