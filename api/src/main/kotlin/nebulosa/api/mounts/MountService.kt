package nebulosa.api.mounts

import nebulosa.api.beans.annotations.Subscriber
import nebulosa.api.image.ImageBucket
import nebulosa.constants.PI
import nebulosa.constants.TAU
import nebulosa.erfa.CartesianCoordinate
import nebulosa.erfa.SphericalCoordinate
import nebulosa.guiding.GuideDirection
import nebulosa.indi.device.mount.*
import nebulosa.log.loggerFor
import nebulosa.lx200.protocol.LX200ProtocolServer
import nebulosa.math.*
import nebulosa.nova.astrometry.Constellation
import nebulosa.nova.frame.Ecliptic
import nebulosa.nova.position.GeographicPosition
import nebulosa.nova.position.Geoid
import nebulosa.nova.position.ICRF
import nebulosa.stellarium.protocol.StellariumProtocolServer
import nebulosa.time.CurrentTime
import nebulosa.wcs.WCS
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.springframework.stereotype.Service
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Service
@Subscriber
class MountService(private val imageBucket: ImageBucket) {

    private val site = HashMap<Mount, GeographicPosition>(2)
    private val remoteControls = ArrayList<MountRemoteControl>(2)

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMountGeographicCoordinateChanged(event: MountGeographicCoordinateChanged) {
        val site = Geoid.IERS2010.lonLat(event.device.longitude, event.device.latitude, event.device.elevation)
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
            GuideDirection.NORTH -> moveNorth(mount, enabled)
            GuideDirection.SOUTH -> moveSouth(mount, enabled)
            GuideDirection.WEST -> moveWest(mount, enabled)
            GuideDirection.EAST -> moveEast(mount, enabled)
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
        return if (timeLeft < 0.0) timeLeft - SIDEREAL_TIME_DIFF * (timeLeft.normalized / TAU)
        else timeLeft + SIDEREAL_TIME_DIFF * (timeLeft / TAU)
    }

    fun coordinates(mount: Mount, longitude: Angle, latitude: Angle, elevation: Distance) {
        mount.coordinates(longitude, latitude, elevation)
    }

    fun dateTime(mount: Mount, dateTime: OffsetDateTime) {
        mount.dateTime(dateTime)
    }

    fun computeLST(mount: Mount): Angle {
        return site[mount]!!.lstAt(CurrentTime)
    }

    fun computeZenithLocation(mount: Mount): ComputedLocation {
        return computeLocation(mount, computeLST(mount), mount.latitude, j2000 = false, meridianAt = false)
    }

    fun computeNorthCelestialPoleLocation(mount: Mount): ComputedLocation {
        return computeCelestialPoleLocation(mount, false)
    }

    fun computeSouthCelestialPoleLocation(mount: Mount): ComputedLocation {
        return computeCelestialPoleLocation(mount, true)
    }

    fun computeCelestialPoleLocation(mount: Mount, south: Boolean): ComputedLocation {
        return computeLocation(mount, computeLST(mount), if (south) -QUARTER else QUARTER, j2000 = false, meridianAt = false)
    }

    fun computeGalacticCenterLocation(mount: Mount): ComputedLocation {
        return computeLocation(mount, GALACTIC_CENTER_RA, GALACTIC_CENTER_DEC, j2000 = true, meridianAt = false)
    }

    fun computeMeridianEquatorLocation(mount: Mount): ComputedLocation {
        return computeLocation(mount, computeLST(mount), 0.0, j2000 = false, meridianAt = false)
    }

    fun computeMeridianEclipticLocation(mount: Mount): ComputedLocation {
        val equatorial = Ecliptic.rotationAt(CurrentTime) * CartesianCoordinate.of(computeLST(mount), 0.0, 1.0)
        val (rightAscension, declination) = SphericalCoordinate.of(equatorial)
        return computeLocation(mount, rightAscension, declination, j2000 = false, meridianAt = false)
    }

    fun computeEquatorEclipticLocation(mount: Mount): ComputedLocation {
        val a = computeLocation(mount, PI, 0.0, j2000 = false, meridianAt = false)
        val b = computeLocation(mount, 0.0, 0.0, j2000 = false, meridianAt = false)
        return if (a.altitude >= b.altitude) a else b
    }

    fun computeLocation(
        mount: Mount,
        rightAscension: Angle = mount.rightAscension, declination: Angle = mount.declination,
        j2000: Boolean = false, equatorial: Boolean = true, horizontal: Boolean = true, meridianAt: Boolean = true,
    ): ComputedLocation {
        val computedLocation = ComputedLocation()

        val center = site[mount]!!
        val epoch = if (j2000) null else CurrentTime

        val icrf = ICRF.equatorial(rightAscension, declination, epoch = epoch, center = center)
        computedLocation.constellation = Constellation.find(icrf)

        if (j2000) {
            if (equatorial) {
                val raDec = icrf.equatorialAtEpoch(CurrentTime)
                computedLocation.rightAscension = raDec.longitude.normalized
                computedLocation.declination = raDec.latitude
            }

            computedLocation.rightAscensionJ2000 = rightAscension
            computedLocation.declinationJ2000 = declination
        } else {
            if (equatorial) {
                val raDec = icrf.equatorial()
                computedLocation.rightAscensionJ2000 = raDec.longitude.normalized
                computedLocation.declinationJ2000 = raDec.latitude
            }

            computedLocation.rightAscension = rightAscension
            computedLocation.declination = declination
        }

        if (horizontal) {
            val altAz = icrf.horizontal()
            computedLocation.azimuth = altAz.longitude.normalized
            computedLocation.altitude = altAz.latitude
        }

        if (meridianAt) {
            computeTimeLeftToMeridianFlip(rightAscension, computeLST(mount).also { computedLocation.lst = it })
                .also { computedLocation.timeLeftToMeridianFlip = it }
                .also { computedLocation.meridianAt = LocalDateTime.now().plusSeconds((it.toHours * 3600.0).toLong()) }
        }

        computedLocation.pierSide = PierSide.expectedPierSide(computedLocation.rightAscension, computedLocation.declination, computeLST(mount))

        return computedLocation
    }

    fun pointMountHere(mount: Mount, path: Path, x: Double, y: Double) {
        val calibration = imageBucket[path]?.second ?: return

        if (calibration.isNotEmpty() && calibration.solved) {
            val wcs = WCS(calibration)
            val (rightAscension, declination) = wcs.use { it.pixToSky(x, y) } // J2000

            val icrf = ICRF.equatorial(calibration.rightAscension, calibration.declination)
            val (calibratedRA, calibratedDEC) = icrf.equatorialAtDate()
            val raOffset = mount.rightAscension - calibratedRA
            val decOffset = mount.declination - calibratedDEC
            LOG.info(
                "pointing mount adjusted. ra={}, dec={}, dx={}, dy={}", rightAscension.formatHMS(),
                declination.formatSignedDMS(), raOffset.formatHMS(), decOffset.formatSignedDMS()
            )
            goTo(mount, rightAscension + raOffset, declination + decOffset, true)
        }
    }

    fun remoteControlStart(mount: Mount, type: MountRemoteControlType, host: String, port: Int) {
        check(remoteControls.none { it.mount === mount && it.type == type }) { "$type ${mount.name} Remote Control is already running" }

        val server = if (type == MountRemoteControlType.STELLARIUM) StellariumProtocolServer(host, port)
        else LX200ProtocolServer(host, port)

        server.run()

        remoteControls.add(MountRemoteControl(type, server, mount))
    }

    fun remoteControlStop(mount: Mount, type: MountRemoteControlType) {
        val remoteControl = remoteControls.find { it.mount === mount && it.type == type } ?: return
        remoteControl.use(remoteControls::remove)
    }

    fun remoteControlList(mount: Mount): List<MountRemoteControl> {
        return remoteControls.filter { it.mount === mount }
    }

    companion object {

        private const val SIDEREAL_TIME_DIFF = 0.06552777 * PI / 12.0

        @JvmStatic private val GALACTIC_CENTER_RA = "17 45 40.04".hours
        @JvmStatic private val GALACTIC_CENTER_DEC = "-29 00 28.1".deg

        @JvmStatic private val LOG = loggerFor<MountService>()
    }
}
