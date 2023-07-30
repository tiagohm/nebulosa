package nebulosa.api.services

import jakarta.annotation.PostConstruct
import nebulosa.api.data.responses.ComputedCoordinateResponse
import nebulosa.indi.device.PropertyChangedEvent
import nebulosa.indi.device.mount.*
import nebulosa.math.Angle
import nebulosa.math.AngleFormatter
import nebulosa.math.Distance
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.position.GeographicPosition
import nebulosa.nova.position.Geoid
import nebulosa.nova.position.ICRF
import nebulosa.time.UTC
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class MountService(
    private val webSocketService: WebSocketService,
    private val eventBus: EventBus,
) {

    private val centerPosition = HashMap<Mount, GeographicPosition>(2)

    @PostConstruct
    private fun initialize() {
        eventBus.register(this)
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMountEvent(event: MountEvent) {
        when (event) {
            is PropertyChangedEvent -> webSocketService.sendMountUpdated(event.device!!)
            is MountAttached -> webSocketService.sendMountAttached(event.device)
            is MountDetached -> webSocketService.sendMountDetached(event.device)
        }

        if (event is MountGeographicCoordinateChanged) {
            val site = Geoid.IERS2010.latLon(event.device.longitude, event.device.latitude, event.device.elevation)
            centerPosition[event.device] = site
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

    fun trackingMode(mount: Mount, mode: TrackMode) {
        mount.trackingMode(mode)
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

    fun coordinates(mount: Mount, longitude: Angle, latitude: Angle, elevation: Distance) {
        mount.coordinates(longitude, latitude, elevation)
    }

    fun dateTime(mount: Mount, dateTime: OffsetDateTime) {
        mount.dateTime(dateTime)
    }

    @Suppress("NAME_SHADOWING")
    fun computeCoordinates(
        mount: Mount,
        rightAscension: Angle = mount.rightAscension, declination: Angle = mount.declination,
        j2000: Boolean,
        equatorial: Boolean,
        horizontal: Boolean,
    ): ComputedCoordinateResponse {
        val center = centerPosition[mount]!!
        val time = UTC.now()
        val epoch = if (j2000) null else time

        val icrf = ICRF.equatorial(rightAscension, declination, time = time, epoch = epoch, center = center)
        val constellation = Constellation.find(icrf)

        var rightAscension = ""
        var declination = ""
        var azimuth = ""
        var altitude = ""

        if (equatorial) {
            val raDec = if (j2000) icrf.equatorialAtDate() else icrf.equatorialJ2000()
            rightAscension = raDec.longitude.normalized.format(AngleFormatter.HMS)
            declination = raDec.latitude.format(AngleFormatter.SIGNED_DMS)
        }

        if (horizontal) {
            val altAz = icrf.horizontal()
            azimuth = altAz.longitude.normalized.format(AngleFormatter.SIGNED_DMS)
            altitude = altAz.latitude.format(AngleFormatter.SIGNED_DMS)
        }

        return ComputedCoordinateResponse(rightAscension, declination, azimuth, altitude, constellation)
    }
}
