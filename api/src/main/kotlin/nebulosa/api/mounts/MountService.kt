package nebulosa.api.mounts

import nebulosa.api.data.responses.ComputedCoordinateResponse
import nebulosa.constants.PI
import nebulosa.constants.TAU
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.DeviceEventHandler
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountGeographicCoordinateChanged
import nebulosa.indi.device.mount.SlewRate
import nebulosa.indi.device.mount.TrackMode
import nebulosa.math.Angle
import nebulosa.math.AngleFormatter
import nebulosa.math.Distance
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.position.GeographicPosition
import nebulosa.nova.position.Geoid
import nebulosa.nova.position.ICRF
import nebulosa.time.UTC
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Service
class MountService : DeviceEventHandler {

    private val site = HashMap<Mount, GeographicPosition>(2)

    @Volatile private var prevTime = 0L

    private var currentTime = UTC.now()
        @Synchronized get() {
            val curTime = System.currentTimeMillis()

            if (curTime - prevTime >= 30000L) {
                prevTime = curTime
                field = UTC.now()
            }

            return field
        }

    override fun onEventReceived(event: DeviceEvent<*>) {
        if (event is MountGeographicCoordinateChanged) {
            val site = Geoid.IERS2010.latLon(event.device.longitude, event.device.latitude, event.device.elevation)
            this.site[event.device] = site
        }
    }

    fun connect(mount: Mount) {
        mount.connect()
    }

    fun disconnect(mount: Mount) {
        mount.disconnect()
    }

    fun tracking(mount: Mount, enable: Boolean) {
        mount.tracking(enable)
    }

    fun sync(mount: Mount, ra: Angle, dec: Angle, j2000: Boolean) {
        if (j2000) mount.syncJ2000(ra, dec)
        else mount.sync(ra, dec)
    }

    fun slewTo(mount: Mount, ra: Angle, dec: Angle, j2000: Boolean) {
        if (j2000) mount.slewToJ2000(ra, dec)
        else mount.slewTo(ra, dec)
    }

    fun goTo(mount: Mount, ra: Angle, dec: Angle, j2000: Boolean) {
        if (j2000) mount.goToJ2000(ra, dec)
        else mount.goTo(ra, dec)
    }

    fun home(mount: Mount) {
        mount.home()
    }

    fun abort(mount: Mount) {
        mount.abortMotion()
    }

    fun trackMode(mount: Mount, mode: TrackMode) {
        mount.trackMode(mode)
    }

    fun slewRate(mount: Mount, rate: SlewRate) {
        mount.slewRate(rate)
    }

    fun moveNorth(mount: Mount, enable: Boolean) {
        mount.moveNorth(enable)
    }

    fun moveSouth(mount: Mount, enable: Boolean) {
        mount.moveSouth(enable)
    }

    fun moveWest(mount: Mount, enable: Boolean) {
        mount.moveWest(enable)
    }

    fun moveEast(mount: Mount, enable: Boolean) {
        mount.moveEast(enable)
    }

    fun park(mount: Mount) {
        mount.park()
    }

    fun unpark(mount: Mount) {
        mount.unpark()
    }

    private fun computeTimeLeftToMeridianFlip(rightAscension: Angle, lst: Angle): Angle {
        val timeLeft = rightAscension - lst
        return if (timeLeft.value < 0.0) timeLeft - SIDEREAL_TIME_DIFF * (timeLeft.normalized.value / TAU)
        else timeLeft + SIDEREAL_TIME_DIFF * (timeLeft.value / TAU)
    }

    fun coordinates(mount: Mount, longitude: Angle, latitude: Angle, elevation: Distance) {
        mount.coordinates(longitude, latitude, elevation)
    }

    fun dateTime(mount: Mount, dateTime: OffsetDateTime) {
        mount.dateTime(dateTime)
    }

    fun computeLST(mount: Mount): Angle {
        return site[mount]!!.lstAt(currentTime)
    }

    fun computeCoordinates(
        mount: Mount,
        rightAscension: Angle = mount.rightAscension, declination: Angle = mount.declination,
        j2000: Boolean = false,
        equatorial: Boolean = true, horizontal: Boolean = true, meridian: Boolean = true,
    ): ComputedCoordinateResponse {
        val center = site[mount]!!
        val time = currentTime
        val epoch = if (j2000) null else time

        val icrf = ICRF.equatorial(rightAscension, declination, time = time, epoch = epoch, center = center)
        val constellation = Constellation.find(icrf)

        var rightAscensionJNOW = ""
        var declinationJNOW = ""
        var rightAscensionJ2000 = ""
        var declinationJ2000 = ""
        var azimuth = ""
        var altitude = ""

        if (j2000) {
            if (equatorial) {
                val raDec = icrf.equatorialAtDate()
                rightAscensionJNOW = raDec.longitude.normalized.format(AngleFormatter.HMS)
                declinationJNOW = raDec.latitude.format(AngleFormatter.SIGNED_DMS)
            }

            rightAscensionJ2000 = rightAscension.format(AngleFormatter.HMS)
            declinationJ2000 = declination.format(AngleFormatter.SIGNED_DMS)
        } else {
            if (equatorial) {
                val raDec = icrf.equatorialJ2000()
                rightAscensionJ2000 = raDec.longitude.normalized.format(AngleFormatter.HMS)
                declinationJ2000 = raDec.latitude.format(AngleFormatter.SIGNED_DMS)
            }

            rightAscensionJNOW = rightAscension.format(AngleFormatter.HMS)
            declinationJNOW = declination.format(AngleFormatter.SIGNED_DMS)
        }

        if (horizontal) {
            val altAz = icrf.horizontal()
            azimuth = altAz.longitude.normalized.format(AngleFormatter.SIGNED_DMS)
            altitude = altAz.latitude.format(AngleFormatter.SIGNED_DMS)
        }

        var meridianAt = ""
        var timeLeftToMeridianFlip = ""
        var lst = ""

        if (meridian) {
            computeTimeLeftToMeridianFlip(rightAscension, computeLST(mount).also { lst = it.format(LST_FORMAT) })
                .also { timeLeftToMeridianFlip = it.format(LST_FORMAT) }
                .also { meridianAt = LocalDateTime.now().plusSeconds((it.hours * 3600.0).toLong()).format(MERIDIAN_TIME_FORMAT) }
        }

        return ComputedCoordinateResponse(
            rightAscensionJNOW, declinationJNOW, rightAscensionJ2000, declinationJ2000,
            azimuth, altitude,
            constellation, lst, meridianAt, timeLeftToMeridianFlip,
        )
    }

    companion object {

        private const val SIDEREAL_TIME_DIFF = 0.06552777 * PI / 12.0

        @JvmStatic private val MERIDIAN_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
        @JvmStatic private val LST_FORMAT = AngleFormatter.Builder()
            .hours()
            .noSign()
            .noSeconds()
            .build()
    }
}
