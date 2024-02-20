package nebulosa.indi.client

import nebulosa.indi.client.connection.INDIProccessConnection
import nebulosa.indi.client.connection.INDISocketConnection
import nebulosa.indi.client.device.GPSDevice
import nebulosa.indi.client.device.INDIDeviceProtocolHandler
import nebulosa.indi.client.device.cameras.AsiCamera
import nebulosa.indi.client.device.cameras.INDICamera
import nebulosa.indi.client.device.cameras.SVBonyCamera
import nebulosa.indi.client.device.cameras.SimCamera
import nebulosa.indi.client.device.focusers.INDIFocuser
import nebulosa.indi.client.device.mounts.INDIMount
import nebulosa.indi.client.device.mounts.IoptronV3Mount
import nebulosa.indi.client.device.wheels.INDIFilterWheel
import nebulosa.indi.device.Device
import nebulosa.indi.device.INDIDeviceProvider
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.thermometer.Thermometer
import nebulosa.indi.protocol.GetProperties
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.indi.protocol.io.INDIConnection
import nebulosa.log.debug
import nebulosa.log.loggerFor
import java.util.*

data class INDIClient(val connection: INDIConnection) : INDIDeviceProtocolHandler(), INDIDeviceProvider {

    constructor(
        host: String,
        port: Int = INDIProtocol.DEFAULT_PORT,
    ) : this(INDISocketConnection(host, port))

    constructor(
        process: Process,
    ) : this(INDIProccessConnection(process))

    override val id = UUID.randomUUID().toString()

    override val isClosed
        get() = !connection.isOpen

    override val input
        get() = connection.input

    override fun newCamera(message: INDIProtocol, executable: String): Camera {
        return CAMERAS[executable]?.create(this, message.device) ?: INDICamera(this, message.device)
    }

    override fun newMount(message: INDIProtocol, executable: String): Mount {
        return MOUNTS[executable]?.create(this, message.device) ?: INDIMount(this, message.device)
    }

    override fun newFocuser(message: INDIProtocol): Focuser {
        return INDIFocuser(this, message.device)
    }

    override fun newFilterWheel(message: INDIProtocol): FilterWheel {
        return INDIFilterWheel(this, message.device)
    }

    override fun newGPS(message: INDIProtocol): GPS {
        return GPSDevice(this, message.device)
    }

    override fun start() {
        super.start()
        sendMessageToServer(GetProperties())
    }

    override fun sendMessageToServer(message: INDIProtocol) {
        LOG.debug { "sending message: $message" }
        connection.writeINDIProtocol(message)
    }

    override fun cameras(): List<Camera> {
        return cameras.values.toList()
    }

    override fun camera(name: String): Camera? {
        return cameras[name]
    }

    override fun mounts(): List<Mount> {
        return mounts.values.toList()
    }

    override fun mount(name: String): Mount? {
        return mounts[name]
    }

    override fun focusers(): List<Focuser> {
        return focusers.values.toList()
    }

    override fun focuser(name: String): Focuser? {
        return focusers[name]
    }

    override fun wheels(): List<FilterWheel> {
        return wheels.values.toList()
    }

    override fun wheel(name: String): FilterWheel? {
        return wheels[name]
    }

    override fun gps(): List<GPS> {
        return gps.values.toList()
    }

    override fun gps(name: String): GPS? {
        return gps[name]
    }

    override fun guideOutputs(): List<GuideOutput> {
        return guideOutputs.values.toList()
    }

    override fun guideOutput(name: String): GuideOutput? {
        return guideOutputs[name]
    }

    override fun thermometers(): List<Thermometer> {
        return thermometers.values.toList()
    }

    override fun thermometer(name: String): Thermometer? {
        return thermometers[name]
    }

    override fun close() {
        super.close()

        connection.close()
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<INDIClient>()

        @JvmStatic private val CAMERAS = mapOf(
            "indi_asi_ccd" to AsiCamera::class.java,
            "indi_asi_single_ccd" to AsiCamera::class.java,
            "indi_svbony_ccd" to SVBonyCamera::class.java,
            "indi_sv305_ccd" to SVBonyCamera::class.java, // legacy name.
            "indi_simulator_ccd" to SimCamera::class.java,
            "indi_simulator_guide" to SimCamera::class.java,
        )

        @JvmStatic private val MOUNTS = mapOf(
            "indi_ioptronv3_telescope" to IoptronV3Mount::class.java,
        )

        @JvmStatic
        fun <T : Device> Class<out T>.create(handler: INDIClient, name: String): T {
            return getConstructor(INDIClient::class.java, String::class.java)
                .newInstance(handler, name)
        }
    }
}
