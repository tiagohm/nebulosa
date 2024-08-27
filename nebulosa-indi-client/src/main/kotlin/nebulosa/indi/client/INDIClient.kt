package nebulosa.indi.client

import nebulosa.indi.client.connection.INDIProccessConnection
import nebulosa.indi.client.connection.INDISocketConnection
import nebulosa.indi.client.device.DriverInfo
import nebulosa.indi.client.device.INDIDeviceProtocolHandler
import nebulosa.indi.client.device.auxiliary.INDIGPS
import nebulosa.indi.client.device.auxiliary.INDIGuideOutput
import nebulosa.indi.client.device.camera.AsiCamera
import nebulosa.indi.client.device.camera.INDICamera
import nebulosa.indi.client.device.camera.SVBonyCamera
import nebulosa.indi.client.device.camera.SimCamera
import nebulosa.indi.client.device.focuser.INDIFocuser
import nebulosa.indi.client.device.lightbox.INDILightBox
import nebulosa.indi.client.device.mount.INDIMount
import nebulosa.indi.client.device.rotator.INDIRotator
import nebulosa.indi.client.device.wheel.INDIFilterWheel
import nebulosa.indi.device.Device
import nebulosa.indi.device.INDIDeviceProvider
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.filterwheel.FilterWheel
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.guider.GuideOutput
import nebulosa.indi.device.lightbox.LightBox
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.rotator.Rotator
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
        get() = !connection.isOpen || super.isClosed

    override val input
        get() = connection.input

    override fun newCamera(driverInfo: DriverInfo): Camera {
        return CAMERAS[driverInfo.executable]?.create(this, driverInfo) ?: INDICamera(this, driverInfo)
    }

    override fun newMount(driverInfo: DriverInfo): Mount {
        return INDIMount(this, driverInfo)
    }

    override fun newFocuser(driverInfo: DriverInfo): Focuser {
        return INDIFocuser(this, driverInfo)
    }

    override fun newFilterWheel(driverInfo: DriverInfo): FilterWheel {
        return INDIFilterWheel(this, driverInfo)
    }

    override fun newRotator(driverInfo: DriverInfo): Rotator {
        return INDIRotator(this, driverInfo)
    }

    override fun newGPS(driverInfo: DriverInfo): GPS {
        return INDIGPS(this, driverInfo)
    }

    override fun newGuideOutput(driverInfo: DriverInfo): GuideOutput {
        return INDIGuideOutput(this, driverInfo)
    }

    override fun newLightBox(driverInfo: DriverInfo): LightBox {
        return INDILightBox(this, driverInfo)
    }

    override fun start() {
        super.start()
        sendMessageToServer(GetProperties())
    }

    override fun sendMessageToServer(message: INDIProtocol) {
        LOG.debug { "sending message: $message" }
        connection.writeINDIProtocol(message)
    }

    override fun onConnectionClosed() {
        fireOnConnectionClosed()
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

        @JvmStatic
        private fun <T : Device> Class<out T>.create(handler: INDIClient, driverInfo: DriverInfo): T {
            return getConstructor(INDIClient::class.java, DriverInfo::class.java)
                .newInstance(handler, driverInfo)
        }
    }
}
