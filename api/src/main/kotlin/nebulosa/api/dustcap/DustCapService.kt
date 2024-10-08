package nebulosa.api.dustcap

import nebulosa.indi.device.dustcap.DustCap

class DustCapService(private val dustCapEventHub: DustCapEventHub) {

    fun connect(dustCap: DustCap) {
        dustCap.connect()
    }

    fun disconnect(dustCap: DustCap) {
        dustCap.disconnect()
    }

    fun park(dustCap: DustCap) {
        dustCap.park()
    }

    fun unpark(dustCap: DustCap) {
        dustCap.unpark()
    }

    fun listen(dustCap: DustCap) {
        dustCapEventHub.listen(dustCap)
    }
}
