package nebulosa.api.mounts

import nebulosa.api.beans.Subscriber
import nebulosa.constants.PI
import nebulosa.constants.TAU
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountGeographicCoordinateChanged
import nebulosa.indi.device.mount.SlewRate
import nebulosa.indi.device.mount.TrackMode
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.hours
import nebulosa.math.AngleFormatter
import nebulosa.math.Distance
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.position.GeographicPosition
import nebulosa.nova.position.Geoid
import nebulosa.nova.position.ICRF
import nebulosa.time.UTC
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Service
@Subscriber
class MountService {

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

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMountGeographicCoordinateChanged(event: MountGeographicCoordinateChanged) {
        val site = Geoid.IERS2010.latLon(event.device.longitude, event.device.latitude, event.device.elevation)
        this.site[event.device] = site
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

    fun move(mount: Mount, direction: GuideDirection, enabled: Boolean) {
        when (direction) {
            GuideDirection.UP_NORTH -> moveNorth(mount, enabled)
            GuideDirection.DOWN_SOUTH -> moveSouth(mount, enabled)
            GuideDirection.LEFT_WEST -> moveWest(mount, enabled)
            GuideDirection.RIGHT_EAST -> moveEast(mount, enabled)
        }
    }

    fun moveNorth(mount: Mount, enabled: Boolean) {
        mount.moveNorth(enabled)
    }

    fun moveSouth(mount: Mount, enabled: Boolean) {
        mount.moveSouth(enabled)
    }

    fun moveWest(mount: Mount, enabled: Boolean) {
        mount.moveWest(enabled)
    }

    fun moveEast(mount: Mount, enabled: Boolean) {
        mount.moveEast(enabled)
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

    fun computeZenithLocation(mount: Mount): ComputedLocation {
        return computeLocation(
            mount, computeLST(mount), mount.latitude,
            j2000 = false, equatorial = true, horizontal = true, meridianAt = false,
        )
    }

    fun computeNorthCelestialPoleLocation(mount: Mount): ComputedLocation {
        return computeCelestialPoleLocation(mount, false)
    }

    fun computeSouthCelestialPoleLocation(mount: Mount): ComputedLocation {
        return computeCelestialPoleLocation(mount, true)
    }

    fun computeCelestialPoleLocation(mount: Mount, south: Boolean): ComputedLocation {
        return computeLocation(
            mount, computeLST(mount), if (south) -Angle.QUARTER else Angle.QUARTER,
            j2000 = false, equatorial = true, horizontal = true, meridianAt = false,
        )
    }

    fun computeGalacticCenterLocation(mount: Mount): ComputedLocation {
        return computeLocation(
            mount, GALACTIC_CENTER_RA, GALACTIC_CENTER_DEC,
            j2000 = true, equatorial = true, horizontal = true, meridianAt = false,
        )
    }

    fun computeLocation(
        mount: Mount,
        rightAscension: Angle = mount.rightAscension, declination: Angle = mount.declination,
        j2000: Boolean = false, equatorial: Boolean = true, horizontal: Boolean = true, meridianAt: Boolean = true,
    ): ComputedLocation {
        val computedLocation = ComputedLocation()

        val center = site[mount]!!
        val time = currentTime
        val epoch = if (j2000) null else time

        val icrf = ICRF.equatorial(rightAscension, declination, time = time, epoch = epoch, center = center)
        computedLocation.constellation = Constellation.find(icrf)

        if (j2000) {
            if (equatorial) {
                val raDec = icrf.equatorialAtDate()
                computedLocation.rightAscension = raDec.longitude.normalized.format(AngleFormatter.HMS)
                computedLocation.declination = raDec.latitude.format(AngleFormatter.SIGNED_DMS)
            }

            computedLocation.rightAscensionJ2000 = rightAscension.format(AngleFormatter.HMS)
            computedLocation.declinationJ2000 = declination.format(AngleFormatter.SIGNED_DMS)
        } else {
            if (equatorial) {
                val raDec = icrf.equatorialJ2000()
                computedLocation.rightAscensionJ2000 = raDec.longitude.normalized.format(AngleFormatter.HMS)
                computedLocation.declinationJ2000 = raDec.latitude.format(AngleFormatter.SIGNED_DMS)
            }

            computedLocation.rightAscension = rightAscension.format(AngleFormatter.HMS)
            computedLocation.declination = declination.format(AngleFormatter.SIGNED_DMS)
        }

        if (horizontal) {
            val altAz = icrf.horizontal()
            computedLocation.azimuth = altAz.longitude.normalized.format(AngleFormatter.SIGNED_DMS)
            computedLocation.altitude = altAz.latitude.format(AngleFormatter.SIGNED_DMS)
        }

        if (meridianAt) {
            computeTimeLeftToMeridianFlip(rightAscension, computeLST(mount).also { computedLocation.lst = it.format(LST_FORMAT) })
                .also { computedLocation.timeLeftToMeridianFlip = it.format(LST_FORMAT) }
                .also { computedLocation.meridianAt = LocalDateTime.now().plusSeconds((it.hours * 3600.0).toLong()).format(MERIDIAN_TIME_FORMAT) }
        }

        return computedLocation
    }

    companion object {

        private const val SIDEREAL_TIME_DIFF = 0.06552777 * PI / 12.0

        @JvmStatic private val GALACTIC_CENTER_RA = "17 45 40.04".hours
        @JvmStatic private val GALACTIC_CENTER_DEC = "-29 00 28.1".deg

        @JvmStatic private val MERIDIAN_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
        @JvmStatic private val LST_FORMAT = AngleFormatter.Builder()
            .hours()
            .noSign()
            .noSeconds()
            .build()
    }
}
