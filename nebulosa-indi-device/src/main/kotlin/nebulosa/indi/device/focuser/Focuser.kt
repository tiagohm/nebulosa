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

        // grep -irl --include \*.h "public INDI::Focuser" .
        @JvmStatic val DRIVERS = setOf(
            "indi_aaf2_focus",
            "indi_activefocuser_focus",
            "indi_alluna_tcs2",
            "indi_armadillo_focus",
            "indi_asi_focuser",
            "indi_astrolink4",
            "indi_astrolink4mini2",
            "indi_astromechfoc",
            "indi_avalonud_focuser",
            "indi_beefocus",
            "indi_celestron_aux",
            "indi_celestron_gps",
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
            "indi_gphoto_ccd",
            "indi_hitecastrodc_focus",
            "indi_ieaf_focus",
            "indi_lacerta_mfoc_fmc_focus",
            "indi_lacerta_mfoc_focus",
            "indi_lakeside_focus",
            "indi_lx200generic",
            "indi_lx200stargo",
            "indi_lynx_focus",
            "indi_microtouch_focus",
            "indi_moonlite_focus",
            "indi_moonlitedro_focus",
            "indi_myfocuserpro2_focus",
            "indi_nfocus",
            "indi_nightcrawler_focus",
            "indi_nstep_focus",
            "indi_oasis_focuser",
            "indi_onfocus_focus",
            "indi_pegasus_focuscube",
            "indi_pegasus_focuscube3",
            "indi_pegasus_ppba",
            "indi_pegasus_prodigyMF",
            "indi_pegasus_scopsoag",
            "indi_pegasus_upb",
            "indi_perfectstar_focus",
            "indi_platypus_focus",
            "indi_qhy_focuser",
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
            "indilx200",
        )
    }
}
