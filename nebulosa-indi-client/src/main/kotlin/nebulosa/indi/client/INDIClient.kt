package nebulosa.indi.client

import nebulosa.indi.client.connection.INDIProccessConnection
import nebulosa.indi.client.connection.INDISocketConnection
import nebulosa.indi.client.device.DeviceProtocolHandler
import nebulosa.indi.device.DeviceHub
import nebulosa.indi.device.MessageSender
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

class INDIClient(private val connection: INDIConnection) : DeviceProtocolHandler(), MessageSender, DeviceHub {

    constructor(
        host: String,
        port: Int = INDIProtocol.DEFAULT_PORT,
    ) : this(INDISocketConnection(host, port))

    constructor(
        process: Process,
    ) : this(INDIProccessConnection(process))

    override val isClosed
        get() = !connection.isOpen

    override val input
        get() = connection.input

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
    }
}
