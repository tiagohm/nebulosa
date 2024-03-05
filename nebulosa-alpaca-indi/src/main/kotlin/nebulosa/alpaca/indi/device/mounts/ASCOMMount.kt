package nebulosa.alpaca.indi.device.mounts

import nebulosa.alpaca.api.*
import nebulosa.alpaca.indi.client.AlpacaClient
import nebulosa.alpaca.indi.device.ASCOMDevice
import nebulosa.indi.device.Device
import nebulosa.indi.device.guide.GuideOutputPulsingChanged
import nebulosa.indi.device.mount.*
import nebulosa.indi.device.mount.PierSide
import nebulosa.indi.protocol.INDIProtocol
import nebulosa.math.*
import nebulosa.nova.position.ICRF
import nebulosa.time.CurrentTime
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneOffset

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

    private val axisRates = HashMap<String, AxisRate>(4)
    @Volatile private var axisRate: AxisRate? = null
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
        service.tracking(device.number, true)

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
        service.tracking(device.number, true)

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
            service.trackingRate(device.number, DriveRate.entries[mode.ordinal])
        }
    }

    override fun slewRate(rate: SlewRate) {
        axisRate = axisRates[rate.name]?.takeIf { it !== axisRate } ?: return
        sender.fireOnEventReceived(MountSlewRateChanged(this))
    }

    private fun moveAxis(axisType: AxisType, negative: Boolean, enabled: Boolean) {
        val rate = axisRate?.maximum ?: return

        service.moveAxis(device.number, axisType, 0.0).doRequest()

        if (enabled) {
            service.moveAxis(device.number, axisType, if (negative) -rate else rate).doRequest()
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
        service.utcDate(device.number, dateTime.toInstant())
    }

    override fun snoop(devices: Iterable<Device?>) {}

    override fun handleMessage(message: INDIProtocol) {}

    override fun onConnected() {
        processCapabilities()
        processGuideRates()
        processSiteCoordinates()
        processDateTime()

        equatorialSystem = service.equatorialSystem(device.number).doRequest()?.value ?: equatorialSystem

        LOG.info("The mount {} uses {} equatorial system", name, equatorialSystem)
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
        axisRate = null
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

        processTracking()
        processSlewing()
        processParked()
        processEquatorialCoordinates()
        processSiteCoordinates()
        processTrackMode()
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

        service.trackingRates(device.number).doRequest {
            trackModes = it.value.map { m -> TrackMode.valueOf(m.name) }
            sender.fireOnEventReceived(MountTrackModesChanged(this))
            processTrackMode()
        }

        service.axisRates(device.number).doRequest {
            val rates = ArrayList<SlewRate>(it.value.size)

            axisRates.clear()

            for (i in it.value.indices) {
                val rate = it.value[i]
                val name = "RATE_$i"
                axisRates[name] = rate
                rates.add(SlewRate(name, "%f.1f deg/s".format(rate.maximum)))
            }

            axisRate = it.value.firstOrNull()

            if (axisRate != null) {
                sender.fireOnEventReceived(MountSlewRateChanged(this))
            }
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
            dateTime = it.value.atOffset(ZoneOffset.systemDefault().rules.getOffset(it.value))
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

    override fun toString() = "Mount(name=$name, connected=$connected, slewing=$slewing, tracking=$tracking," +
            " parking=$parking, parked=$parked, canAbort=$canAbort," +
            " canSync=$canSync, canPark=$canPark, slewRates=$slewRates," +
            " slewRate=$slewRate, mountType=$mountType, trackModes=$trackModes," +
            " trackMode=$trackMode, pierSide=$pierSide, guideRateWE=$guideRateWE," +
            " guideRateNS=$guideRateNS, rightAscension=$rightAscension," +
            " declination=$declination, canPulseGuide=$canPulseGuide," +
            " pulseGuiding=$pulseGuiding)"
}
