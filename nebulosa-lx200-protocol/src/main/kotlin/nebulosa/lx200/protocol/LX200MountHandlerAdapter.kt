package nebulosa.lx200.protocol

import nebulosa.indi.device.mount.Mount
import nebulosa.math.Angle
import java.time.OffsetDateTime

data class LX200MountHandlerAdapter(private val mount: Mount) : LX200MountHandler {

    override val rightAscension
        get() = mount.rightAscension

    override val declination
        get() = mount.declination

    override val latitude
        get() = mount.latitude

    override val longitude
        get() = mount.longitude

    override val slewing
        get() = mount.slewing

    override val tracking
        get() = mount.tracking

    override val parked
        get() = mount.parked

    override fun goTo(rightAscension: Angle, declination: Angle) {
        mount.goToJ2000(rightAscension, declination)
    }

    override fun syncTo(rightAscension: Angle, declination: Angle) {
        mount.syncJ2000(rightAscension, declination)
    }

    override fun abort() {
        mount.abortMotion()
    }

    override fun moveNorth(enabled: Boolean) {
        mount.moveNorth(enabled)
    }

    override fun moveSouth(enabled: Boolean) {
        mount.moveSouth(enabled)
    }

    override fun moveWest(enabled: Boolean) {
        mount.moveWest(enabled)
    }

    override fun moveEast(enabled: Boolean) {
        mount.moveEast(enabled)
    }

    override fun time(time: OffsetDateTime) {
        mount.dateTime(time)
    }

    override fun coordinates(longitude: Angle, latitude: Angle) {
        mount.coordinates(longitude, latitude, mount.elevation)
    }
}
