package nebulosa.guiding.phd2

import nebulosa.guiding.Guider
import nebulosa.guiding.SiderealShiftTrackingRate
import nebulosa.phd2.client.PHD2Client
import nebulosa.phd2.client.PHD2EventListener
import nebulosa.phd2.client.event.PHD2Event

class PHD2Guider(
    host: String,
    port: Int = 4400,
) : Guider, PHD2EventListener {

    private val client = PHD2Client(host, port)

    override val canClearCalibration: Boolean
        get() = TODO("Not yet implemented")

    override var shiftRate: SiderealShiftTrackingRate
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun autoSelectGuideStar() {
        TODO("Not yet implemented")
    }

    override fun connect() {
        client.registerListener(this)
        client.connect()
    }

    override fun start(forceCalibration: Boolean) {
        TODO("Not yet implemented")
    }

    override fun dither() {
        TODO("Not yet implemented")
    }

    override fun stop() {
    }

    override fun clearCalibration() {
        TODO("Not yet implemented")
    }

    override fun stopShifting() {
        TODO("Not yet implemented")
    }

    override fun onEvent(client: PHD2Client, event: PHD2Event) {
        if (client === this.client) {
            println(event)
        }
    }

    override fun close() {
        client.unregisterListener(this)
        client.close()
    }
}
