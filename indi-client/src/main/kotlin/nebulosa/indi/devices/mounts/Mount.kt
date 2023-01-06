package nebulosa.indi.devices.mounts

import nebulosa.indi.devices.Device
import nebulosa.math.Angle

interface Mount : Device {

    val isSlewing: Boolean

    val isTracking: Boolean

    val isParking: Boolean

    val isParked: Boolean

    val canAbort: Boolean

    val canSync: Boolean

    val canPark: Boolean

    val slewRates: List<SlewRate>

    val slewRate: SlewRate?

    val mountType: MountType

    val trackModes: List<TrackMode>

    val trackMode: TrackMode

    val pierSide: PierSide

    val guideRateWE: Double

    val guideRateNS: Double

    val rightAscension: Double

    val declination: Double

    fun tracking(enable: Boolean)

    fun sync(ra: Angle, dec: Angle)

    fun syncJ2000(ra: Angle, dec: Angle)

    fun slewTo(ra: Angle, dec: Angle)

    fun slewToJ2000(ra: Angle, dec: Angle)

    fun goTo(ra: Angle, dec: Angle)

    fun goToJ2000(ra: Angle, dec: Angle)

    fun park()

    fun unpark()

    fun abortMotion()

    fun trackingMode(mode: TrackMode)

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
