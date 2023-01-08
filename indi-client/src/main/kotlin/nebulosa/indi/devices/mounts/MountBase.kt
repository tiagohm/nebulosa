package nebulosa.indi.devices.mounts

import nebulosa.indi.INDIClient
import nebulosa.indi.devices.AbstractDevice
import nebulosa.indi.devices.DeviceProtocolHandler
import nebulosa.indi.devices.firstOnSwitch
import nebulosa.indi.devices.firstOnSwitchOrNull
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
        sendNewSwitch("TELESCOPE_ABORT_MOTION", "ABORT" to true)
    }

    override fun trackingMode(mode: TrackMode) {
        sendNewSwitch("TELESCOPE_TRACK_MODE", "TRACK_$mode" to true)
    }

    override fun close() {}
}
