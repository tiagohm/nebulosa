package nebulosa.indi.device.focuser

import nebulosa.indi.device.Device
import nebulosa.indi.device.thermometer.Thermometer

interface Focuser : Device, Thermometer {

    val moving: Boolean

    val position: Int

    val canAbsoluteMove: Boolean

    val canRelativeMove: Boolean

    val canAbort: Boolean

    val canReverse: Boolean

    val reversed: Boolean

    val canSync: Boolean

    val hasBacklash: Boolean

    val maxPosition: Int

    fun moveFocusIn(steps: Int)

    fun moveFocusOut(steps: Int)

    fun moveFocusTo(steps: Int)

    fun abortFocus()

    fun reverseFocus(enable: Boolean)

    fun syncFocusTo(steps: Int)

    companion object {

        @JvmStatic val DRIVERS = setOf(
            "indi_aaf2_focus",
            "indi_activefocuser_focus",
            "indi_armadillo_focus",
            "indi_asi_focuser",
            "indi_celestron_sct_focus",
            "indi_deepskydad_af1_focus",
            "indi_deepskydad_af2_focus",
            "indi_deepskydad_af3_focus",
            "indi_dmfc_focus",
            "indi_dreamfocuser_focus",
            "indi_efa_focus",
            "indi_esatto_focus",
            "indi_esattoarco_focus",
            "indi_fcusb_focus",
            "indi_fli_focus",
            "indi_gemini_focus",
            "indi_hitecastrodc_focus",
            "indi_integra_focus",
            "indi_lacerta_mfoc_fmc_focus",
            "indi_lacerta_mfoc_focus",
            "indi_lakeside_focus",
            "indi_lynx_focus",
            "indi_microtouch_focus",
            "indi_moonlite_focus",
            "indi_moonlitedro_focus",
            "indi_myfocuserpro2_focus",
            "indi_nightcrawler_focus",
            "indi_nstep_focus",
            "indi_onfocus_focus",
            "indi_pegasus_focuscube",
            "indi_perfectstar_focus",
            "indi_platypus_focus",
            "indi_rainbowrsf_focus",
            "indi_rbfocus_focus",
            "indi_robo_focus",
            "indi_sestosenso2_focus",
            "indi_sestosenso_focus",
            "indi_siefs_focus",
            "indi_simulator_focus",
            "indi_smartfocus_focus",
            "indi_steeldrive2_focus",
            "indi_steeldrive_focus",
            "indi_tcfs3_focus",
            "indi_tcfs_focus",
            "indi_teenastro_focus",
            "indi_usbfocusv3_focus",
        )
    }
}
