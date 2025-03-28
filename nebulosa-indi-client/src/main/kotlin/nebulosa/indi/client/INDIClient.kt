package nebulosa.indi.client

import nebulosa.indi.client.connection.INDIProcessConnection
import nebulosa.indi.client.connection.INDISocketConnection
import nebulosa.indi.client.device.INDIDriverInfo
import nebulosa.indi.client.device.INDIDeviceProtocolHandler
import nebulosa.indi.client.device.auxiliary.INDIGPS
import nebulosa.indi.client.device.auxiliary.INDIGuideOutput
import nebulosa.indi.client.device.camera.AsiCamera
import nebulosa.indi.client.device.camera.INDICamera
import nebulosa.indi.client.device.camera.SVBonyCamera
import nebulosa.indi.client.device.camera.SimCamera
import nebulosa.indi.client.device.dustcap.INDIDustCap
import nebulosa.indi.client.device.focuser.INDIFocuser
import nebulosa.indi.client.device.lightbox.INDILightBox
import nebulosa.indi.client.device.mount.INDIMount
import nebulosa.indi.client.device.rotator.INDIRotator
import nebulosa.indi.client.device.wheel.INDIFilterWheel
import nebulosa.indi.device.Device
import nebulosa.indi.device.INDIDeviceProvider
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.dustcap.DustCap
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
import nebulosa.log.d
import nebulosa.log.loggerFor
import java.util.*

data class INDIClient(val connection: INDIConnection) : INDIDeviceProtocolHandler(), INDIDeviceProvider {

    constructor(
        host: String,
        port: Int = INDIProtocol.DEFAULT_PORT,
    ) : this(INDISocketConnection(host, port))

    constructor(
        process: Process,
    ) : this(INDIProcessConnection(process))

    override val id = UUID.randomUUID().toString()

    override val isClosed
        get() = !connection.isOpen || super.isClosed

    override val input
        get() = connection.input

    override fun newCamera(driver: INDIDriverInfo): Camera {
        return CAMERAS[driver.executable]?.create(this, driver) ?: INDICamera(this, driver)
    }

    override fun newMount(driver: INDIDriverInfo): Mount {
        return INDIMount(this, driver)
    }

    override fun newFocuser(driver: INDIDriverInfo): Focuser {
        return INDIFocuser(this, driver)
    }

    override fun newFilterWheel(driver: INDIDriverInfo): FilterWheel {
        return INDIFilterWheel(this, driver)
    }

    override fun newRotator(driver: INDIDriverInfo): Rotator {
        return INDIRotator(this, driver)
    }

    override fun newGPS(driver: INDIDriverInfo): GPS {
        return INDIGPS(this, driver)
    }

    override fun newGuideOutput(driver: INDIDriverInfo): GuideOutput {
        return INDIGuideOutput(this, driver)
    }

    override fun newLightBox(driver: INDIDriverInfo): LightBox {
        return INDILightBox(this, driver)
    }

    override fun newDustCap(driver: INDIDriverInfo): DustCap {
        return INDIDustCap(this, driver)
    }

    override fun start() {
        super.start()
        sendMessageToServer(GetProperties())
    }

    override fun sendMessageToServer(message: INDIProtocol) {
        LOG.d { debug("sending message: {}", message) }
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

        private val LOG = loggerFor<INDIClient>()

        private val CAMERAS = mapOf(
            "indi_asi_ccd" to AsiCamera::class.java,
            "indi_asi_single_ccd" to AsiCamera::class.java,
            "indi_svbony_ccd" to SVBonyCamera::class.java,
            "indi_sv305_ccd" to SVBonyCamera::class.java, // legacy name.
            "indi_simulator_ccd" to SimCamera::class.java,
            "indi_simulator_guide" to SimCamera::class.java,
        )

        private fun <T : Device> Class<out T>.create(handler: INDIClient, driver: INDIDriverInfo): T {
            return getConstructor(INDIClient::class.java, INDIDriverInfo::class.java)
                .newInstance(handler, driver)
        }
    }
}
