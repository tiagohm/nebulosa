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
import java.time.Duration
import java.time.OffsetDateTime

class ASCOMMount(
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
    @Volatile override var canAbort = false
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

    override fun onConnected() {}

    override fun onDisconnected() {}

    override fun snoop(devices: Iterable<Device?>) {}

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
        TODO("Not yet implemented")
    }

    override fun slewTo(ra: Angle, dec: Angle) {
        service.slewToCoordinates(device.number, ra.toHours, dec.toDegrees).doRequest()
    }

    override fun slewToJ2000(ra: Angle, dec: Angle) {
        TODO("Not yet implemented")
    }

    override fun goTo(ra: Angle, dec: Angle) {
        TODO("Not yet implemented")
    }

    override fun goToJ2000(ra: Angle, dec: Angle) {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun moveNorth(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun moveSouth(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun moveWest(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun moveEast(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun coordinates(longitude: Angle, latitude: Angle, elevation: Distance) {
        service.siteLongitude(device.number, longitude.toDegrees).doRequest {} &&
            service.siteLatitude(device.number, latitude.toDegrees).doRequest {} &&
            service.siteElevation(device.number, elevation.toMeters).doRequest {}
    }

    override fun dateTime(dateTime: OffsetDateTime) {
        service.utcDate(device.number, dateTime.toInstant())
    }

    override fun handleMessage(message: INDIProtocol) {}
}
