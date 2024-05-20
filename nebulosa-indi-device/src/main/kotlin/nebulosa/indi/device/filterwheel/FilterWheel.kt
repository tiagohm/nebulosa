package nebulosa.indi.device.filterwheel

import nebulosa.indi.device.Device

interface FilterWheel : Device {

    val count: Int

    val position: Int

    val moving: Boolean

    val names: List<String>

    fun moveTo(position: Int)

    fun names(names: Iterable<String>)

    companion object {

        @JvmStatic val DRIVERS = setOf(
            "indi_altair_wheel",
            "indi_apogee_wheel",
            "indi_asi_wheel",
            "indi_atik_wheel",
            "indi_bressercam_wheel",
            "indi_fli_wheel",
            "indi_mallincam_wheel",
            "indi_manual_wheel",
            "indi_mi_sfw_eth",
            "indi_mi_sfw_usb",
            "indi_nncam_ccd",
            "indi_oasis_filter_wheel",
            "indi_ogmacam_wheel",
            "indi_omegonprocam_wheel",
            "indi_optec_wheel",
            "indi_pegasusindigo_wheel",
            "indi_playerone_wheel",
            "indi_qhycfw1_wheel",
            "indi_qhycfw2_wheel",
            "indi_qhycfw3_wheel",
            "indi_quantum_wheel",
            "indi_simulator_wheel",
            "indi_starshootg_wheel",
            "indi_sx_wheel",
            "indi_toupcam_wheel",
            "indi_trutech_wheel",
            "indi_tscam_wheel",
            "indi_xagyl_wheel",
        )
    }
}
