package nebulosa.indi.client.device.mount

import nebulosa.indi.client.INDIClient
import nebulosa.indi.client.device.DriverInfo
import nebulosa.indi.client.device.INDIDevice
import nebulosa.indi.client.device.handler.INDIGuideOutputHandler
import nebulosa.indi.device.firstOnSwitch
import nebulosa.indi.device.firstOnSwitchOrNull
import nebulosa.indi.device.gps.GPS
import nebulosa.indi.device.mount.*
import nebulosa.indi.protocol.*
import nebulosa.indi.protocol.DefVector.Companion.isNotReadOnly
import nebulosa.indi.protocol.Vector.Companion.isBusy
import nebulosa.math.*
import nebulosa.nova.position.ICRF
import nebulosa.time.SystemClock
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneOffset

// https://github.com/indilib/indi/blob/master/libs/indibase/inditelescope.cpp

internal open class INDIMount(
    final override val sender: INDIClient,
    final override val driverInfo: DriverInfo,
) : INDIDevice(), Mount {

    @Volatile final override var slewing = false
    @Volatile final override var tracking = false
    @Volatile final override var parking = false
    @Volatile final override var parked = false
    @Volatile final override var canAbort = false
    @Volatile final override var canSync = false
    @Volatile final override var canGoTo = false
    @Volatile final override var canPark = false
    @Volatile final override var canHome = false
    @Volatile final override var slewRates = emptyList<SlewRate>()
    @Volatile final override var slewRate: SlewRate? = null
    @Volatile final override var mountType = MountType.EQ_GEM // TODO: Ver os telescópios possui tipos.
    @Volatile final override var trackModes = emptyList<TrackMode>()
    @Volatile final override var trackMode = TrackMode.SIDEREAL
    @Volatile final override var pierSide = PierSide.NEITHER
    @Volatile final override var guideRateWE = 0.0 // TODO: Tratar para cada driver. iOptronV3 tem RA/DE. LX200 tem 1.0x, 0.8x, 0.6x, 0.4x.
    @Volatile final override var guideRateNS = 0.0
    @Volatile final override var rightAscension = 0.0
    @Volatile final override var declination = 0.0

    @Suppress("LeakingThis")
    private val guideOutput = INDIGuideOutputHandler(this)

    final override val canPulseGuide
        get() = guideOutput.canPulseGuide

    final override val pulseGuiding
        get() = guideOutput.pulseGuiding

    @Volatile final override var hasGPS = false
    @Volatile final override var longitude = 0.0
    @Volatile final override var latitude = 0.0
    @Volatile final override var elevation = 0.0
    @Volatile final override var dateTime = OffsetDateTime.now(SystemClock)!!

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is SwitchVector<*> -> {
                when (message.name) {
                    "TELESCOPE_SLEW_RATE" -> {
                        if (message is DefSwitchVector) {
                            slewRates = message.map { SlewRate(it.name, it.label) }

                            sender.fireOnEventReceived(MountSlewRatesChanged(this))
                        }

                        val name = message.firstOnSwitch().name

                        if (slewRate?.name != name) {
                            slewRate = slewRates.firstOrNull { it.name == name }
                            sender.fireOnEventReceived(MountSlewRateChanged(this))
                        }
                    }
                    // "MOUNT_TYPE" -> {
                    //     mountType = MountType.valueOf(message.firstOnSwitch().name)
                    //
                    //     provider.fireOnEventReceived(MountTypeChanged(this))
                    // }
                    "TELESCOPE_TRACK_MODE" -> {
                        if (message is DefSwitchVector) {
                            trackModes = message.map { TrackMode.valueOf(it.name.replace("TRACK_", "")) }

                            sender.fireOnEventReceived(MountTrackModesChanged(this))
                        }

                        trackMode = TrackMode.valueOf(message.firstOnSwitch().name.replace("TRACK_", ""))

                        sender.fireOnEventReceived(MountTrackModeChanged(this))
                    }
                    "TELESCOPE_TRACK_STATE" -> {
                        tracking = message.firstOnSwitch().name == "TRACK_ON"

                        sender.fireOnEventReceived(MountTrackingChanged(this))
                    }
                    "TELESCOPE_PIER_SIDE" -> {
                        val side = message.firstOnSwitchOrNull()

                        pierSide = if (side == null) PierSide.NEITHER
                        else if (side.name == "PIER_WEST") PierSide.WEST
                        else PierSide.EAST

                        sender.fireOnEventReceived(MountPierSideChanged(this))
                    }
                    "TELESCOPE_PARK" -> {
                        if (message is DefSwitchVector) {
                            canPark = message.isNotReadOnly

                            sender.fireOnEventReceived(MountCanParkChanged(this))
                        }

                        parking = message.isBusy
                        parked = message.firstOnSwitchOrNull()?.name == "PARK"

                        sender.fireOnEventReceived(MountParkChanged(this))
                    }
                    "TELESCOPE_ABORT_MOTION" -> {
                        canAbort = true

                        sender.fireOnEventReceived(MountCanAbortChanged(this))
                    }
                    "ON_COORD_SET" -> {
                        canSync = message.any { it.name == "SYNC" }
                        canGoTo = message.any { it.name == "TRACK" }

                        sender.fireOnEventReceived(MountCanSyncChanged(this))
                        sender.fireOnEventReceived(MountCanGoToChanged(this))
                    }
                    "TELESCOPE_HOME" -> {
                        canHome = true
                        sender.fireOnEventReceived(MountCanHomeChanged(this))
                    }
                }
            }
            is NumberVector<*> -> {
                when (message.name) {
                    // "GUIDE_RATE" -> {
                    //     guideRateWE = message["GUIDE_RATE_WE"]!!.value
                    //     guideRateNS = message["GUIDE_RATE_NS"]!!.value
                    //
                    //     provider.fireOnEventReceived(MountGuideRateChanged(this))
                    // }
                    "EQUATORIAL_EOD_COORD" -> {
                        if (message.state == PropertyState.ALERT) {
                            sender.fireOnEventReceived(MountSlewFailed(this))
                        }

                        val prevIsIslewing = slewing
                        slewing = message.isBusy

                        if (slewing != prevIsIslewing) {
                            sender.fireOnEventReceived(MountSlewingChanged(this))
                        }

                        rightAscension = message["RA"]!!.value.hours
                        declination = message["DEC"]!!.value.deg

                        sender.fireOnEventReceived(MountEquatorialCoordinatesChanged(this))
                    }
                    "GEOGRAPHIC_COORD" -> {
                        latitude = message["LAT"]!!.value.deg
                        longitude = message["LONG"]!!.value.deg
                        elevation = message["ELEV"]!!.value.m

                        sender.fireOnEventReceived(MountGeographicCoordinateChanged(this))
                    }
                }
            }
            is TextVector<*> -> {
                when (message.name) {
                    "TIME_UTC" -> {
                        val utcTime = GPS.extractTime(message["UTC"]!!.value) ?: return
                        val utcOffset = message["OFFSET"]!!.value.toDoubleOrNull() ?: 0.0

                        dateTime = OffsetDateTime.of(utcTime, ZoneOffset.ofTotalSeconds((utcOffset * 3600.0).toInt()))

                        sender.fireOnEventReceived(MountTimeChanged(this))
                    }
                }
            }
            else -> Unit
        }

        guideOutput.handleMessage(message)

        super.handleMessage(message)
    }

    override fun tracking(enable: Boolean) {
        if (tracking != enable) {
            sendNewSwitch("TELESCOPE_TRACK_STATE", (if (enable) "TRACK_ON" else "TRACK_OFF") to true)
        }
    }

    override fun sync(ra: Angle, dec: Angle) {
        sendNewSwitch("ON_COORD_SET", "SYNC" to true)
        sendNewNumber("EQUATORIAL_EOD_COORD", "RA" to ra.toHours, "DEC" to dec.toDegrees)
    }

    override fun syncJ2000(ra: Angle, dec: Angle) {
        val (raNow, decNow) = ICRF.equatorial(ra, dec).equatorialAtDate()
        sync(raNow.normalized, decNow)
    }

    override fun slewTo(ra: Angle, dec: Angle) {
        sendNewSwitch("ON_COORD_SET", "SLEW" to true)
        sendNewNumber("EQUATORIAL_EOD_COORD", "RA" to ra.toHours, "DEC" to dec.toDegrees)
    }

    override fun slewToJ2000(ra: Angle, dec: Angle) {
        val (raNow, decNow) = ICRF.equatorial(ra, dec).equatorialAtDate()
        slewTo(raNow.normalized, decNow)
    }

    override fun goTo(ra: Angle, dec: Angle) {
        sendNewSwitch("ON_COORD_SET", "TRACK" to true)
        sendNewNumber("EQUATORIAL_EOD_COORD", "RA" to ra.toHours, "DEC" to dec.toDegrees)
    }

    override fun goToJ2000(ra: Angle, dec: Angle) {
        val (raNow, decNow) = ICRF.equatorial(ra, dec).equatorialAtDate()
        goTo(raNow.normalized, decNow)
    }

    override fun park() {
        if (canPark) {
            sendNewSwitch("TELESCOPE_PARK", "PARK" to true)
        }
    }

    override fun unpark() {
        if (canPark) {
            sendNewSwitch("TELESCOPE_PARK", "UNPARK" to true)
        }
    }

    override fun home() {
        if (canHome) {
            sendNewSwitch("TELESCOPE_HOME", "GO" to true)
        }
    }

    override fun abortMotion() {
        if (canAbort) {
            sendNewSwitch("TELESCOPE_ABORT_MOTION", "ABORT" to true)
        }
    }

    override fun trackMode(mode: TrackMode) {
        sendNewSwitch("TELESCOPE_TRACK_MODE", "TRACK_$mode" to true)
    }

    override fun slewRate(rate: SlewRate) {
        if (rate in slewRates) {
            sendNewSwitch("TELESCOPE_SLEW_RATE", rate.name to true)
        }
    }

    override fun guideNorth(duration: Duration) {
        guideOutput.guideNorth(duration)
    }

    override fun guideSouth(duration: Duration) {
        guideOutput.guideSouth(duration)
    }

    override fun guideEast(duration: Duration) {
        guideOutput.guideEast(duration)
    }

    override fun guideWest(duration: Duration) {
        guideOutput.guideWest(duration)
    }

    override fun moveNorth(enabled: Boolean) {
        if (enabled) sendNewSwitch("TELESCOPE_MOTION_NS", "MOTION_NORTH" to true, "MOTION_SOUTH" to false)
        else sendNewSwitch("TELESCOPE_MOTION_NS", "MOTION_NORTH" to false)
    }

    override fun moveSouth(enabled: Boolean) {
        if (enabled) sendNewSwitch("TELESCOPE_MOTION_NS", "MOTION_NORTH" to false, "MOTION_SOUTH" to true)
        else sendNewSwitch("TELESCOPE_MOTION_NS", "MOTION_SOUTH" to false)
    }

    override fun moveWest(enabled: Boolean) {
        if (enabled) sendNewSwitch("TELESCOPE_MOTION_WE", "MOTION_WEST" to true, "MOTION_EAST" to false)
        else sendNewSwitch("TELESCOPE_MOTION_WE", "MOTION_WEST" to false)
    }

    override fun moveEast(enabled: Boolean) {
        if (enabled) sendNewSwitch("TELESCOPE_MOTION_WE", "MOTION_WEST" to false, "MOTION_EAST" to true)
        else sendNewSwitch("TELESCOPE_MOTION_WE", "MOTION_EAST" to false)
    }

    override fun coordinates(longitude: Angle, latitude: Angle, elevation: Distance) {
        sendNewNumber("GEOGRAPHIC_COORD", "LAT" to latitude.toDegrees, "LONG" to longitude.toDegrees, "ELEV" to elevation.toMeters)
    }

    override fun dateTime(dateTime: OffsetDateTime) {
        val offsetHours = dateTime.offset.totalSeconds / 3600.0
        val offsetMinutes = (offsetHours - offsetHours.toInt()) * 60.0 % 60.0
        val offset = "%02d:%02d".format(offsetHours.toInt(), offsetMinutes.toInt())

        sendNewText("TIME_UTC", "UTC" to GPS.formatTime(dateTime.toLocalDateTime()), "OFFSET" to offset)
    }

    override fun close() {
        if (hasGPS) {
            hasGPS = false
            sender.unregisterGPS(this)
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
