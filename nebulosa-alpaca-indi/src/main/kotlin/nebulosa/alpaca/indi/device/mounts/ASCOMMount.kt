package nebulosa.alpaca.indi.device.mounts

import nebulosa.alpaca.api.AlpacaTelescopeService
import nebulosa.alpaca.api.ConfiguredDevice
import nebulosa.alpaca.api.DriveRate
import nebulosa.alpaca.api.PulseGuideDirection
import nebulosa.alpaca.indi.client.AlpacaClient
import nebulosa.alpaca.indi.device.ASCOMDevice
import nebulosa.indi.device.Device
import nebulosa.indi.device.guide.GuideOutputPulsingChanged
import nebulosa.indi.device.mount.*
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.math.*
import nebulosa.nova.position.ICRF
import nebulosa.time.CurrentTime
import java.time.Duration
import java.time.OffsetDateTime

data class ASCOMMount(
    override val device: ConfiguredDevice,
    override val service: AlpacaTelescopeService,
    override val sender: AlpacaClient,
) : ASCOMDevice(), Mount {

    @Volatile override var slewing = false
        private set
    @Volatile override var tracking = false
        private set
    @Volatile override var parking = false
        private set
    @Volatile override var parked = false
        private set
    @Volatile override var canAbort = true
        private set
    @Volatile override var canSync = false
        private set
    @Volatile override var canGoTo = false
        private set
    @Volatile override var canPark = false
        private set
    @Volatile override var canHome = false
        private set
    @Volatile override var slewRates = emptyList<SlewRate>()
        private set
    @Volatile override var slewRate: SlewRate? = null
        private set
    @Volatile override var mountType = MountType.EQ_GEM
        private set
    @Volatile override var trackModes = emptyList<TrackMode>()
        private set
    @Volatile override var trackMode = TrackMode.SIDEREAL
        private set
    @Volatile override var pierSide = PierSide.NEITHER
        private set
    @Volatile override var guideRateWE = 0.0
        private set
    @Volatile override var guideRateNS = 0.0
        private set
    @Volatile override var rightAscension = 0.0
        private set
    @Volatile override var declination = 0.0
        private set

    @Volatile override var canPulseGuide = false
        private set
    @Volatile override var pulseGuiding = false
        private set

    @Volatile override var hasGPS = false
        private set
    @Volatile override var longitude = 0.0
        private set
    @Volatile override var latitude = 0.0
        private set
    @Volatile override var elevation = 0.0
        private set
    @Volatile override var dateTime = OffsetDateTime.now()!!
        private set

    override fun park() {
        if (canPark) {
            service.park(device.number).doRequest()
        }
    }

    override fun unpark() {
        if (canPark) {
            service.unpark(device.number).doRequest()
        }
    }

    private fun pulseGuide(direction: PulseGuideDirection, duration: Duration) {
        if (canPulseGuide) {
            val durationInMilliseconds = duration.toMillis()

            service.pulseGuide(device.number, direction, durationInMilliseconds).doRequest() ?: return

            if (durationInMilliseconds > 0) {
                pulseGuiding = true
                sender.fireOnEventReceived(GuideOutputPulsingChanged(this))
            }
        }
    }

    override fun guideNorth(duration: Duration) {
        pulseGuide(PulseGuideDirection.NORTH, duration)
    }

    override fun guideSouth(duration: Duration) {
        pulseGuide(PulseGuideDirection.SOUTH, duration)
    }

    override fun guideEast(duration: Duration) {
        pulseGuide(PulseGuideDirection.EAST, duration)
    }

    override fun guideWest(duration: Duration) {
        pulseGuide(PulseGuideDirection.WEST, duration)
    }

    override fun tracking(enable: Boolean) {
        service.tracking(device.number, enable).doRequest()
    }

    override fun sync(ra: Angle, dec: Angle) {
        if (canSync) {
            service.syncToCoordinates(device.number, ra.toHours, dec.toDegrees).doRequest()
        }
    }

    override fun syncJ2000(ra: Angle, dec: Angle) {
        if (canSync) {
            with(ICRF.equatorial(ra, dec, epoch = CurrentTime).equatorial()) {
                sync(longitude.normalized, latitude)
            }
        }
    }

    override fun slewTo(ra: Angle, dec: Angle) {
        service.slewToCoordinates(device.number, ra.toHours, dec.toDegrees).doRequest()
    }

    override fun slewToJ2000(ra: Angle, dec: Angle) {
        if (canSync) {
            with(ICRF.equatorial(ra, dec, epoch = CurrentTime).equatorial()) {
                slewTo(longitude.normalized, latitude)
            }
        }
    }

    override fun goTo(ra: Angle, dec: Angle) {
        slewTo(ra, dec)
    }

    override fun goToJ2000(ra: Angle, dec: Angle) {
        slewToJ2000(ra, dec)
    }

    override fun home() {
        if (canHome) {
            service.findHome(device.number).doRequest()
        }
    }

    override fun abortMotion() {
        if (canAbort) {
            service.abortSlew(device.number).doRequest()
        }
    }

    override fun trackMode(mode: TrackMode) {
        if (mode != TrackMode.CUSTOM) {
            service.trackingRate(device.number, DriveRate.entries[mode.ordinal])
        }
    }

    override fun slewRate(rate: SlewRate) {
    }

    override fun moveNorth(enabled: Boolean) {
    }

    override fun moveSouth(enabled: Boolean) {
    }

    override fun moveWest(enabled: Boolean) {
    }

    override fun moveEast(enabled: Boolean) {
    }

    override fun coordinates(longitude: Angle, latitude: Angle, elevation: Distance) {
        service.siteLongitude(device.number, longitude.toDegrees).doRequest {} &&
            service.siteLatitude(device.number, latitude.toDegrees).doRequest {} &&
            service.siteElevation(device.number, elevation.toMeters).doRequest {}
    }

    override fun dateTime(dateTime: OffsetDateTime) {
        service.utcDate(device.number, dateTime.toInstant())
    }

    override fun snoop(devices: Iterable<Device?>) {}

    override fun handleMessage(message: INDIProtocol) {}

    override fun onConnected() {
        processCapabilities()
        processSiteCoordinates()
    }

    override fun onDisconnected() {}

    override fun reset() {
        super.reset()

        slewing = false
        tracking = false
        parking = false
        parked = false
        canAbort = false
        canSync = false
        canGoTo = false
        canPark = false
        canHome = false
        slewRates = emptyList()
        slewRate = null
        mountType = MountType.EQ_GEM
        trackModes = emptyList()
        trackMode = TrackMode.SIDEREAL
        pierSide = PierSide.NEITHER
        guideRateWE = 0.0
        guideRateNS = 0.0
        rightAscension = 0.0
        declination = 0.0

        canPulseGuide = false
        pulseGuiding = false

        hasGPS = false
        longitude = 0.0
        latitude = 0.0
        elevation = 0.0
        dateTime = OffsetDateTime.now()!!
    }

    override fun close() {
        super.close()

        if (canPulseGuide) {
            canPulseGuide = false
            sender.unregisterGuideOutput(this)
        }
    }

    override fun refresh(elapsedTimeInSeconds: Long) {
        super.refresh(elapsedTimeInSeconds)

        processParked()
        processSiteCoordinates()
    }

    private fun processCapabilities() {
        service.canFindHome(device.number).doRequest {
            if (it.value) {
                canHome = true

                sender.fireOnEventReceived(MountCanHomeChanged(this))
            }
        }

        service.canPark(device.number).doRequest {
            if (it.value) {
                canPark = true

                sender.fireOnEventReceived(MountCanParkChanged(this))
            }
        }

        service.canPulseGuide(device.number).doRequest {
            if (it.value) {
                canPulseGuide = true
                sender.registerGuideOutput(this)
                LOG.info("guide output attached: {}", name)
            }
        }

        service.canSync(device.number).doRequest {
            if (it.value) {
                canSync = true

                sender.fireOnEventReceived(MountCanSyncChanged(this))
            }
        }
    }

    private fun processParked() {
        if (canPark) {
            service.isAtPark(device.number).doRequest {
                if (it.value != parked) {
                    parked = it.value

                    sender.fireOnEventReceived(MountParkChanged(this))
                }
            }
        }
    }

    private fun processSiteCoordinates() {
        service.siteLongitude(device.number).doRequest { a ->
            val lng = a.value.deg

            service.siteLatitude(device.number).doRequest { b ->
                val lat = b.value.deg

                service.siteLatitude(device.number).doRequest { c ->
                    val elev = c.value.m

                    if (lng != longitude || lat != latitude || elev != elevation) {
                        longitude = lng
                        latitude = lat
                        elevation = elev

                        sender.fireOnEventReceived(MountGeographicCoordinateChanged(this))
                    }
                }
            }
        }
    }
}
