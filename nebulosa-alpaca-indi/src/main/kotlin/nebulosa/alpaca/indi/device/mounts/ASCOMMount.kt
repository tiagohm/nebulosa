package nebulosa.alpaca.indi.device.mounts

import nebulosa.alpaca.api.*
import nebulosa.alpaca.indi.client.AlpacaClient
import nebulosa.alpaca.indi.device.ASCOMDevice
import nebulosa.indi.device.Device
import nebulosa.indi.device.guide.GuideOutputPulsingChanged
import nebulosa.indi.device.mount.*
import nebulosa.indi.device.mount.PierSide
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.log.loggerFor
import nebulosa.math.*
import nebulosa.nova.position.ICRF
import nebulosa.time.CurrentTime
import java.math.BigDecimal
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Suppress("RedundantModalityModifier")
data class ASCOMMount(
    override val device: ConfiguredDevice,
    override val service: AlpacaTelescopeService,
    override val sender: AlpacaClient,
) : ASCOMDevice(), Mount {

    @Volatile final override var slewing = false
    @Volatile final override var tracking = false
    @Volatile final override var parking = false
    @Volatile final override var parked = false
    @Volatile final override var canAbort = true
    @Volatile final override var canSync = false
    @Volatile final override var canGoTo = false
    @Volatile final override var canPark = false
    @Volatile final override var canHome = false
    @Volatile final override var slewRates = emptyList<SlewRate>()
    @Volatile final override var slewRate: SlewRate? = null
    @Volatile final override var mountType = MountType.EQ_GEM
    @Volatile final override var trackModes = emptyList<TrackMode>()
    @Volatile final override var trackMode = TrackMode.SIDEREAL
    @Volatile final override var pierSide = PierSide.NEITHER
    @Volatile final override var guideRateWE = 0.0
    @Volatile final override var guideRateNS = 0.0
    @Volatile final override var rightAscension = 0.0
    @Volatile final override var declination = 0.0

    @Volatile final override var canPulseGuide = false
    @Volatile final override var pulseGuiding = false

    @Volatile final override var hasGPS = false
    @Volatile final override var longitude = 0.0
    @Volatile final override var latitude = 0.0
    @Volatile final override var elevation = 0.0
    @Volatile final override var dateTime = OffsetDateTime.now()!!

    override val snoopedDevices = emptyList<Device>()

    private val axisRates = HashMap<String, Pair<AxisRate, BigDecimal>>(4)
    @Volatile private var equatorialSystem = EquatorialCoordinateType.J2000

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
            if (equatorialSystem != EquatorialCoordinateType.J2000) {
                service.syncToCoordinates(device.number, ra.toHours, dec.toDegrees).doRequest()
            } else {
                // J2000 -> JNOW.
                with(ICRF.equatorial(ra, dec).equatorialAtDate()) {
                    service.syncToCoordinates(device.number, longitude.normalized.toHours, latitude.toDegrees).doRequest()
                }
            }
        }
    }

    override fun syncJ2000(ra: Angle, dec: Angle) {
        if (canSync) {
            if (equatorialSystem == EquatorialCoordinateType.J2000) {
                service.syncToCoordinates(device.number, ra.toHours, dec.toDegrees).doRequest()
            } else {
                // JNOW -> J2000.
                with(ICRF.equatorial(ra, dec, epoch = CurrentTime).equatorial()) {
                    service.syncToCoordinates(device.number, longitude.normalized.toHours, latitude.toDegrees).doRequest()
                }
            }
        }
    }

    override fun slewTo(ra: Angle, dec: Angle) {
        tracking(true)

        if (equatorialSystem != EquatorialCoordinateType.J2000) {
            service.slewToCoordinates(device.number, ra.toHours, dec.toDegrees).doRequest()
        } else {
            // J2000 -> JNOW.
            with(ICRF.equatorial(ra, dec).equatorialAtDate()) {
                service.slewToCoordinates(device.number, longitude.normalized.toHours, latitude.toDegrees).doRequest()
            }
        }
    }

    override fun slewToJ2000(ra: Angle, dec: Angle) {
        tracking(true)

        if (equatorialSystem == EquatorialCoordinateType.J2000) {
            service.slewToCoordinates(device.number, ra.toHours, dec.toDegrees).doRequest()
        } else {
            // JNOW -> J2000.
            with(ICRF.equatorial(ra, dec, epoch = CurrentTime).equatorial()) {
                service.slewToCoordinates(device.number, longitude.normalized.toHours, latitude.toDegrees).doRequest()
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
            service.trackingRate(device.number, DriveRate.entries[mode.ordinal]).doRequest()
        }
    }

    override fun slewRate(rate: SlewRate) {
        slewRate = slewRates.firstOrNull { it.name == rate.name } ?: return
        sender.fireOnEventReceived(MountSlewRateChanged(this))
    }

    private fun moveAxis(axisType: AxisType, negative: Boolean, enabled: Boolean) {
        val rate = slewRate?.name?.let { axisRates[it] }?.second ?: return LOG.warn("axisRate is null")

        if (enabled) {
            service.moveAxis(device.number, axisType, if (negative) -(rate.toDouble()) else rate.toDouble()).doRequest()
        } else {
            service.moveAxis(device.number, axisType, 0.0).doRequest()
        }
    }

    override fun moveNorth(enabled: Boolean) {
        moveAxis(AxisType.SECONDARY, false, enabled)
    }

    override fun moveSouth(enabled: Boolean) {
        moveAxis(AxisType.SECONDARY, true, enabled)
    }

    override fun moveWest(enabled: Boolean) {
        moveAxis(AxisType.PRIMARY, true, enabled)
    }

    override fun moveEast(enabled: Boolean) {
        moveAxis(AxisType.PRIMARY, false, enabled)
    }

    override fun coordinates(longitude: Angle, latitude: Angle, elevation: Distance) {
        service.siteLongitude(device.number, longitude.toDegrees).doRequest {} &&
                service.siteLatitude(device.number, latitude.toDegrees).doRequest {} &&
                service.siteElevation(device.number, elevation.toMeters).doRequest {}
    }

    override fun dateTime(dateTime: OffsetDateTime) {
        service.utcDate(device.number, dateTime.toInstant()).doRequest()
    }

    override fun snoop(devices: Iterable<Device?>) {}

    override fun handleMessage(message: INDIProtocol) {}

    override fun onConnected() {
        processCapabilities()
        processGuideRates()
        processSiteCoordinates()
        processDateTime()

        equatorialSystem = service.equatorialSystem(device.number).doRequest()?.value ?: equatorialSystem
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

        axisRates.clear()
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

        if (connected) {
            processTracking()
            processSlewing()
            processParked()
            processEquatorialCoordinates()
            processSiteCoordinates()
            processTrackMode()
        }
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
            }
        }

        service.canSync(device.number).doRequest {
            if (it.value) {
                canSync = true
                sender.fireOnEventReceived(MountCanSyncChanged(this))
            }
        }

        service.trackingRates(device.number).doRequest {
            trackModes = it.value.map { m -> TrackMode.valueOf(m.name) }
            sender.fireOnEventReceived(MountTrackModesChanged(this))
            processTrackMode()
        }

        axisRates.clear()

        val rates = sortedMapOf<BigDecimal, Pair<SlewRate, AxisRate>>()

        fun Array<AxisRate>.populateWithNewRates() {
            for (rate in this) {
                var min = rate.minimum

                while (min <= rate.maximum) {
                    if (min > BigDecimal.ZERO && min !in rates) {
                        val name = "RATE_${axisRates.size}"
                        axisRates[name] = rate to min
                        rates[min] = SlewRate(name, "%.1f °/s".format(min)) to rate
                    }

                    min += SLEW_RATE_INCREMENT
                }
            }
        }

        service.axisRates(device.number, AxisType.PRIMARY).doRequest {
            it.value.populateWithNewRates()
        }

        service.axisRates(device.number, AxisType.SECONDARY).doRequest {
            it.value.populateWithNewRates()
        }

        slewRates = rates.values.map { it.first }
        slewRate = slewRates.firstOrNull()

        if (slewRates.isNotEmpty()) {
            sender.fireOnEventReceived(MountSlewRatesChanged(this))
        }

        if (slewRate != null) {
            sender.fireOnEventReceived(MountSlewRateChanged(this))
        }
    }

    private fun processTracking() {
        service.isTracking(device.number).doRequest {
            if (it.value != tracking) {
                tracking = it.value
                sender.fireOnEventReceived(MountTrackingChanged(this))
            }
        }
    }

    private fun processSlewing() {
        service.isSlewing(device.number).doRequest {
            if (it.value != slewing) {
                slewing = it.value
                sender.fireOnEventReceived(MountSlewingChanged(this))
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

                service.siteElevation(device.number).doRequest { c ->
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

    private fun processDateTime() {
        service.utcDate(device.number).doRequest {
            dateTime = it.value.atOffset(ZoneOffset.UTC)
            sender.fireOnEventReceived(MountTimeChanged(this))
        }
    }

    private fun processTrackMode() {
        service.trackingRate(device.number).doRequest {
            if (it.value.name != trackMode.name) {
                trackMode = TrackMode.valueOf(it.value.name)
                sender.fireOnEventReceived(MountTrackModeChanged(this))
            }
        }
    }

    private fun processGuideRates() {
        service.guideRateRightAscension(device.number).doRequest { ra ->
            // TODO: deg/s is the same for INDI?
            guideRateWE = ra.value

            service.guideRateDeclination(device.number).doRequest { de ->
                guideRateNS = de.value
            }
        }
    }

    private fun processEquatorialCoordinates() {
        service.rightAscension(device.number).doRequest { a ->
            var ra = a.value.hours

            service.declination(device.number).doRequest { b ->
                var dec = b.value.deg

                // J2000 -> JNOW.
                if (equatorialSystem == EquatorialCoordinateType.J2000) {
                    with(ICRF.equatorial(ra, dec).equatorialAtDate()) {
                        ra = longitude.normalized
                        dec = latitude
                    }
                }

                if (ra != rightAscension || dec != declination) {
                    rightAscension = ra
                    declination = dec

                    sender.fireOnEventReceived(MountEquatorialCoordinatesChanged(this))
                }
            }
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<ASCOMMount>()
        @JvmStatic private val SLEW_RATE_INCREMENT = BigDecimal("0.1")
    }
}
