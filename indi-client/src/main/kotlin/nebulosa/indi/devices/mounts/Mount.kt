package nebulosa.indi.devices.mounts

import nebulosa.indi.INDIClient
import nebulosa.indi.devices.Device
import nebulosa.indi.devices.DeviceProtocolHandler
import nebulosa.indi.devices.firstOnSwitch
import nebulosa.indi.devices.firstOnSwitchOrNull
import nebulosa.indi.protocol.*
import nebulosa.math.Angle

open class Mount(
    client: INDIClient,
    handler: DeviceProtocolHandler,
    name: String,
) : Device(client, handler, name) {

    @Volatile @JvmField var isTracking = false
    @Volatile @JvmField var isParking = false
    @Volatile @JvmField var isParked = false
    @Volatile @JvmField var canAbort = false
    @Volatile @JvmField var canSync = false
    @Volatile @JvmField var slewRates = emptyList<SlewRate>()
    @Volatile @JvmField var slewRate: SlewRate? = null
    @Volatile @JvmField var mountType = MountType.EQ_GEM
    @Volatile @JvmField var trackModes = emptyList<TrackMode>()
    @Volatile @JvmField var trackMode = TrackMode.SIDEREAL
    @Volatile @JvmField var pierSide = PierSide.NEITHER
    @Volatile @JvmField var guideRateWE = 0.0
    @Volatile @JvmField var guideRateNS = 0.0

    override fun handleMessage(message: INDIProtocol) {
        when (message) {
            is SwitchVector<*> -> {
                when (message.name) {
                    "TELESCOPE_SLEW_RATE" -> {
                        if (message is DefSwitchVector) {
                            slewRates = message.map { SlewRate(it.name, it.label) }

                            handler.fireOnEventReceived(MountSlewRatesChanged(this))
                        }

                        slewRate = slewRates.first { it.name == message.firstOnSwitch().name }

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
                        isParking = message.state == PropertyState.BUSY
                        isParked = message.firstOnSwitch().name == "PARK"

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
                }
            }
            else -> Unit
        }

        super.handleMessage(message)
    }

    fun tracking(enable: Boolean) {
        if (isTracking != enable) {
            sendNewSwitch("TELESCOPE_TRACK_STATE", (if (enable) "TRACK_ON" else "TRACK_OFF") to true)
        }
    }

    fun sync(ra: Angle, dec: Angle) {
        sendNewSwitch("ON_COORD_SET", "SYNC" to true)
        sendNewNumber("EQUATORIAL_EOD_COORD", "RA" to ra.hours, "DEC" to dec.degrees)
    }

    fun slewTo(ra: Angle, dec: Angle) {
        sendNewSwitch("ON_COORD_SET", "SLEW" to true)
        sendNewNumber("EQUATORIAL_EOD_COORD", "RA" to ra.hours, "DEC" to dec.degrees)
    }

    fun goTo(ra: Angle, dec: Angle) {
        sendNewSwitch("ON_COORD_SET", "TRACK" to true)
        sendNewNumber("EQUATORIAL_EOD_COORD", "RA" to ra.hours, "DEC" to dec.degrees)
    }

    fun park() {
        sendNewSwitch("TELESCOPE_PARK", "PARK" to true)
    }

    fun unpark() {
        sendNewSwitch("TELESCOPE_PARK", "UNPARK" to true)
    }

    fun abortMotion() {
        sendNewSwitch("TELESCOPE_ABORT_MOTION", "ABORT" to true)
    }

    override fun toString() = name

    companion object {

        @JvmStatic val DRIVERS = setOf(
            "indi_astrotrac_telescope",
            "indi_azgti_telescope",
            "indi_bresserexos2",
            "indi_celestron_aux",
            "indi_celestron_gps",
            "indi_crux_mount",
            "indi_dsc_telescope",
            "indi_eq500x_telescope",
            "indi_eqmod_telescope",
            "indi_ieq_telescope",
            "indi_ieqlegacy_telescope",
            "indi_ioptronHC8406",
            "indi_ioptronv3_telescope",
            "indi_lx200_10micron",
            "indi_lx200_16",
            "indi_lx200_OnStep",
            "indi_lx200_TeenAstro",
            "indi_lx200am5",
            "indi_lx200aok",
            "indi_lx200ap_gtocp2",
            "indi_lx200ap_v2",
            "indi_lx200ap",
            "indi_lx200autostar",
            "indi_lx200basic",
            "indi_lx200classic",
            "indi_lx200fs2",
            "indi_lx200gemini",
            "indi_lx200gotonova",
            "indi_lx200gps",
            "indi_lx200_OpenAstroTech",
            "indi_lx200pulsar2",
            "indi_lx200ss2000pc",
            "indi_lx200stargo",
            "indi_lx200zeq25",
            "indi_paramount_telescope",
            "indi_pmc8_telescope",
            "indi_rainbow_telescope",
            "indi_script_telescope",
            "indi_simulator_telescope",
            "indi_skycommander_telescope",
            "indi_skywatcherAltAzMount",
            "indi_staradventurer2i_telescope",
            "indi_starbook_telescope",
            "indi_starbook_ten",
            "indi_synscan_telescope",
            "indi_synscanlegacy_telescope",
            "indi_temma_telescope",
        )
    }
}
