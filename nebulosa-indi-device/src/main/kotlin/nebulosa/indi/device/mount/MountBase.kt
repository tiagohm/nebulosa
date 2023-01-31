package nebulosa.indi.device.mount

import nebulosa.indi.device.*
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.gps.GPSDetached
import nebulosa.indi.device.guider.GuiderAttached
import nebulosa.indi.device.guider.GuiderDetached
import nebulosa.indi.device.guider.GuiderPulsingChanged
import nebulosa.indi.protocol.*
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.deg
import nebulosa.math.Angle.Companion.hours
import nebulosa.math.Angle.Companion.rad
import nebulosa.math.Distance
import nebulosa.math.Distance.Companion.m
import nebulosa.nova.position.Geoid
import nebulosa.nova.position.ICRF
import nebulosa.time.TimeJD
import java.time.OffsetDateTime
import java.time.ZoneOffset

internal open class MountBase(
    sender: MessageSender,
    handler: DeviceProtocolHandler,
    name: String,
) : AbstractDevice(sender, handler, name), Mount {

    override var slewing = false
    override var tracking = false
    override var parking = false
    override var parked = false
    override var canAbort = false
    override var canSync = false
    override var canPark = false
    override var slewRates = emptyList<String>()
    override var slewRate: String? = null
    override var mountType = MountType.EQ_GEM
    override var trackModes = emptyList<TrackMode>()
    override var trackMode = TrackMode.SIDEREAL
    override var pierSide = PierSide.NEITHER
    override var guideRateWE = 0.0
    override var guideRateNS = 0.0
    override var rightAscension = Angle.ZERO
    override var declination = Angle.ZERO
    override var rightAscensionJ2000 = Angle.ZERO
    override var declinationJ2000 = Angle.ZERO
    override var azimuth = Angle.ZERO
    override var altitude = Angle.ZERO

    override var canPulseGuide = false
    override var pulseGuiding = false

    override var hasGPS = false
    override var longitude = Angle.ZERO
    override var latitude = Angle.ZERO
    override var elevation = Distance.ZERO
    override var time = OffsetDateTime.now()!!

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is SwitchVector<*> -> {
                when (message.name) {
                    "TELESCOPE_SLEW_RATE" -> {
                        if (message is DefSwitchVector) {
                            slewRates = message.map { it.name }

                            handler.fireOnEventReceived(MountSlewRatesChanged(this))
                        }

                        slewRate = message.firstOnSwitch().name

                        handler.fireOnEventReceived(MountSlewRateChanged(this))
                    }
                    "MOUNT_TYPE" -> {
                        mountType = MountType.valueOf(message.firstOnSwitch().name)

                        handler.fireOnEventReceived(MountTypeChanged(this))
                    }
                    "TELESCOPE_TRACK_MODE" -> {
                        if (message is DefSwitchVector) {
                            trackModes = message.map { TrackMode.valueOf(it.name.replace("TRACK_", "")) }

                            handler.fireOnEventReceived(MountTrackModesChanged(this))
                        }

                        trackMode = TrackMode.valueOf(message.firstOnSwitch().name.replace("TRACK_", ""))

                        handler.fireOnEventReceived(MountTrackModeChanged(this))
                    }
                    "TELESCOPE_TRACK_STATE" -> {
                        tracking = message.firstOnSwitch().name == "TRACK_ON"

                        handler.fireOnEventReceived(MountTrackingChanged(this))
                    }
                    "TELESCOPE_PIER_SIDE" -> {
                        val side = message.firstOnSwitchOrNull()

                        pierSide = if (side == null) PierSide.NEITHER
                        else if (side.name == "PIER_WEST") PierSide.WEST
                        else PierSide.EAST

                        handler.fireOnEventReceived(MountPierSideChanged(this))
                    }
                    "TELESCOPE_PARK" -> {
                        if (message is DefSwitchVector) {
                            canPark = message.isNotReadOnly

                            handler.fireOnEventReceived(MountCanParkChanged(this))
                        }

                        parking = message.isBusy
                        parked = message.firstOnSwitchOrNull()?.name == "PARK"

                        handler.fireOnEventReceived(MountParkChanged(this))
                    }
                    "TELESCOPE_ABORT_MOTION" -> {
                        canAbort = true

                        handler.fireOnEventReceived(MountCanAbortChanged(this))
                    }
                    "ON_COORD_SET" -> {
                        canSync = message.any { it.name == "SYNC" }

                        handler.fireOnEventReceived(MountCanSyncChanged(this))
                    }
                }
            }
            is NumberVector<*> -> {
                when (message.name) {
                    "GUIDE_RATE" -> {
                        guideRateWE = message["GUIDE_RATE_WE"]!!.value
                        guideRateNS = message["GUIDE_RATE_NS"]!!.value

                        handler.fireOnEventReceived(MountGuideRateChanged(this))
                    }
                    "EQUATORIAL_EOD_COORD" -> {
                        if (message.state == PropertyState.ALERT) {
                            handler.fireOnEventReceived(MountSlewFailed(this))
                        }

                        val prevIsIslewing = slewing
                        slewing = message.isBusy

                        if (slewing != prevIsIslewing) {
                            handler.fireOnEventReceived(MountSlewingChanged(this))
                        }

                        rightAscension = message["RA"]!!.value.hours
                        declination = message["DEC"]!!.value.deg

                        handler.fireOnEventReceived(MountEquatorialCoordinatesChanged(this))
                    }
                    "TELESCOPE_TIMED_GUIDE_NS" -> {
                        if (!canPulseGuide && message is DefNumberVector) {
                            canPulseGuide = true

                            handler.fireOnEventReceived(GuiderAttached(this))
                        }

                        if (canPulseGuide) {
                            val prevIsPulseGuiding = pulseGuiding
                            pulseGuiding = message.isBusy

                            if (pulseGuiding != prevIsPulseGuiding) {
                                handler.fireOnEventReceived(GuiderPulsingChanged(this))
                            }
                        }
                    }
                    "TELESCOPE_TIMED_GUIDE_WE" -> {
                        if (!canPulseGuide && message is DefNumberVector) {
                            canPulseGuide = true

                            handler.fireOnEventReceived(GuiderAttached(this))
                        }

                        if (canPulseGuide) {
                            val prevIsPulseGuiding = pulseGuiding
                            pulseGuiding = message.isBusy

                            if (pulseGuiding != prevIsPulseGuiding) {
                                handler.fireOnEventReceived(GuiderPulsingChanged(this))
                            }
                        }
                    }
                    "GEOGRAPHIC_COORD" -> {
                        latitude = message["LAT"]!!.value.deg
                        longitude = message["LONG"]!!.value.deg
                        elevation = message["ELEV"]!!.value.m

                        handler.fireOnEventReceived(MountGeographicCoordinateChanged(this))
                    }
                }
            }
            is TextVector<*> -> {
                when (message.name) {
                    "TIME_UTC" -> {
                        val utcTime = GPS.extractTime(message["UTC"]!!.value) ?: return
                        val utcOffset = message["OFFSET"]!!.value.toDoubleOrNull() ?: 0.0

                        time = OffsetDateTime.of(utcTime, ZoneOffset.ofTotalSeconds((utcOffset * 60.0).toInt()))

                        handler.fireOnEventReceived(MountTimeChanged(this))
                    }
                }
            }
            else -> Unit
        }

        super.handleMessage(message)
    }

    override fun tracking(enable: Boolean) {
        if (tracking != enable) {
            sendNewSwitch("TELESCOPE_TRACK_STATE", (if (enable) "TRACK_ON" else "TRACK_OFF") to true)
        }
    }

    override fun sync(ra: Angle, dec: Angle) {
        sendNewSwitch("ON_COORD_SET", "SYNC" to true)
        sendNewNumber("EQUATORIAL_EOD_COORD", "RA" to ra.hours, "DEC" to dec.degrees)
    }

    override fun syncJ2000(ra: Angle, dec: Angle) {
        val (raNow, decNow) = ICRF.equatorial(ra, dec).equatorialAtDate()
        sync(raNow.rad.normalized, decNow.rad)
    }

    override fun slewTo(ra: Angle, dec: Angle) {
        sendNewSwitch("ON_COORD_SET", "SLEW" to true)
        sendNewNumber("EQUATORIAL_EOD_COORD", "RA" to ra.hours, "DEC" to dec.degrees)
    }

    override fun slewToJ2000(ra: Angle, dec: Angle) {
        val (raNow, decNow) = ICRF.equatorial(ra, dec).equatorialAtDate()
        slewTo(raNow.rad.normalized, decNow.rad)
    }

    override fun goTo(ra: Angle, dec: Angle) {
        sendNewSwitch("ON_COORD_SET", "TRACK" to true)
        sendNewNumber("EQUATORIAL_EOD_COORD", "RA" to ra.hours, "DEC" to dec.degrees)
    }

    override fun goToJ2000(ra: Angle, dec: Angle) {
        val (raNow, decNow) = ICRF.equatorial(ra, dec).equatorialAtDate()
        goTo(raNow.rad.normalized, decNow.rad)
    }

    override fun park() {
        sendNewSwitch("TELESCOPE_PARK", "PARK" to true)
    }

    override fun unpark() {
        sendNewSwitch("TELESCOPE_PARK", "UNPARK" to true)
    }

    override fun abortMotion() {
        if (canAbort) {
            sendNewSwitch("TELESCOPE_ABORT_MOTION", "ABORT" to true)
        }
    }

    override fun trackingMode(mode: TrackMode) {
        sendNewSwitch("TELESCOPE_TRACK_MODE", "TRACK_$mode" to true)
    }

    override fun slewRate(rate: String) {
        if (rate in slewRates) {
            sendNewSwitch("TELESCOPE_SLEW_RATE", rate to true)
        }
    }

    override fun guideNorth(duration: Int) {
        if (canPulseGuide) {
            sendNewNumber("TELESCOPE_TIMED_GUIDE_NS", "TIMED_GUIDE_N" to duration.toDouble())
        }
    }

    override fun guideSouth(duration: Int) {
        if (canPulseGuide) {
            sendNewNumber("TELESCOPE_TIMED_GUIDE_NS", "TIMED_GUIDE_S" to duration.toDouble())
        }
    }

    override fun guideEast(duration: Int) {
        if (canPulseGuide) {
            sendNewNumber("TELESCOPE_TIMED_GUIDE_WE", "TIMED_GUIDE_E" to duration.toDouble())
        }
    }

    override fun guideWest(duration: Int) {
        if (canPulseGuide) {
            sendNewNumber("TELESCOPE_TIMED_GUIDE_WE", "TIMED_GUIDE_W" to duration.toDouble())
        }
    }

    override fun coordinates(longitude: Angle, latitude: Angle, elevation: Distance) {
        sendNewNumber("GEOGRAPHIC_COORD", "LAT" to latitude.degrees, "LONG" to longitude.degrees, "ELEV" to elevation.meters)
    }

    override fun time(time: OffsetDateTime) {

    }

    override fun computeCoordinates(j2000: Boolean, horizontal: Boolean) {
        if (j2000 || horizontal) {
            val epoch = TimeJD.now()
            val center = Geoid.IERS2010.latLon(longitude, latitude, elevation)
            val icrf = ICRF.equatorial(rightAscension, declination, time = epoch, epoch = epoch, center = center)

            if (j2000) {
                val raDec = icrf.equatorialJ2000()
                rightAscensionJ2000 = raDec.longitude.normalized
                declinationJ2000 = raDec.latitude
            }

            if (horizontal) {
                val altAz = icrf.horizontal()
                azimuth = altAz.longitude.normalized
                altitude = altAz.latitude
            }
        }
    }

    override fun close() {
        if (canPulseGuide) {
            canPulseGuide = false
            handler.fireOnEventReceived(GuiderDetached(this))
        }

        if (hasGPS) {
            hasGPS = false
            handler.fireOnEventReceived(GPSDetached(this))
        }
    }

    override fun toString(): String {
        return "Mount(name=$name, slewing=$slewing, tracking=$tracking," +
                " parking=$parking, parked=$parked, canAbort=$canAbort," +
                " canSync=$canSync, canPark=$canPark, slewRates=$slewRates," +
                " slewRate=$slewRate, mountType=$mountType, trackModes=$trackModes," +
                " trackMode=$trackMode, pierSide=$pierSide, guideRateWE=$guideRateWE," +
                " guideRateNS=$guideRateNS, rightAscension=$rightAscension," +
                " declination=$declination, canPulseGuide=$canPulseGuide," +
                " pulseGuiding=$pulseGuiding)"
    }
}
