package nebulosa.indi.devices.mounts

import nebulosa.indi.INDIClient
import nebulosa.indi.devices.AbstractDevice
import nebulosa.indi.devices.DeviceProtocolHandler
import nebulosa.indi.devices.firstOnSwitch
import nebulosa.indi.devices.firstOnSwitchOrNull
import nebulosa.indi.devices.guiders.GuiderAttached
import nebulosa.indi.devices.guiders.GuiderDetached
import nebulosa.indi.devices.guiders.GuiderPulsingChanged
import nebulosa.indi.protocol.*
import nebulosa.math.Angle
import nebulosa.math.Angle.Companion.rad
import nebulosa.nova.astrometry.ICRF

internal open class MountBase(
    client: INDIClient,
    handler: DeviceProtocolHandler,
    name: String,
) : AbstractDevice(client, handler, name), Mount {

    override var isSlewing = false
    override var isTracking = false
    override var isParking = false
    override var isParked = false
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
    override var rightAscension = 0.0
    override var declination = 0.0

    override var canPulseGuide = false
    override var isPulseGuiding = false

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
                        isTracking = message.firstOnSwitch().name == "TRACK_ON"

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
                            canPark = message.perm != PropertyPermission.RO

                            handler.fireOnEventReceived(MountCanParkChanged(this))
                        }

                        isParking = message.state == PropertyState.BUSY
                        isParked = message.firstOnSwitchOrNull()?.name == "PARK"

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
                        val prevIsIslewing = isSlewing
                        isSlewing = message.state == PropertyState.BUSY

                        if (isSlewing != prevIsIslewing) {
                            handler.fireOnEventReceived(MountSlewingChanged(this))
                        }

                        rightAscension = message["RA"]!!.value
                        declination = message["DEC"]!!.value

                        handler.fireOnEventReceived(MountEquatorialCoordinatesChanged(this))
                    }
                    "TELESCOPE_TIMED_GUIDE_NS" -> {
                        if (!canPulseGuide && message is DefNumberVector) {
                            canPulseGuide = true

                            handler.fireOnEventReceived(GuiderAttached(this))
                        } else {
                            val prevIsPulseGuiding = isPulseGuiding
                            isPulseGuiding = message.state == PropertyState.BUSY

                            if (isPulseGuiding != prevIsPulseGuiding) {
                                handler.fireOnEventReceived(GuiderPulsingChanged(this))
                            }
                        }
                    }
                    "TELESCOPE_TIMED_GUIDE_WE" -> {
                        if (!canPulseGuide && message is DefNumberVector) {
                            canPulseGuide = true

                            handler.fireOnEventReceived(GuiderAttached(this))
                        } else {
                            val prevIsPulseGuiding = isPulseGuiding
                            isPulseGuiding = message.state == PropertyState.BUSY

                            if (isPulseGuiding != prevIsPulseGuiding) {
                                handler.fireOnEventReceived(GuiderPulsingChanged(this))
                            }
                        }
                    }
                }
            }
            else -> Unit
        }

        super.handleMessage(message)
    }

    override fun tracking(enable: Boolean) {
        if (isTracking != enable) {
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

    override fun close() {
        if (canPulseGuide) {
            canPulseGuide = false
            handler.fireOnEventReceived(GuiderDetached(this))
        }
    }

    override fun toString(): String {
        return "Mount(name=$name, isSlewing=$isSlewing, isTracking=$isTracking," +
                " isParking=$isParking, isParked=$isParked, canAbort=$canAbort," +
                " canSync=$canSync, canPark=$canPark, slewRates=$slewRates," +
                " slewRate=$slewRate, mountType=$mountType, trackModes=$trackModes," +
                " trackMode=$trackMode, pierSide=$pierSide, guideRateWE=$guideRateWE," +
                " guideRateNS=$guideRateNS, rightAscension=$rightAscension," +
                " declination=$declination, canPulseGuide=$canPulseGuide," +
                " isPulseGuiding=$isPulseGuiding)"
    }
}
