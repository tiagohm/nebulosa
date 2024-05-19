package nebulosa.indi.device.rotator

import nebulosa.indi.device.Device

interface Rotator : Device {

    val canAbort: Boolean

    val canHome: Boolean

    val canSync: Boolean

    val canReverse: Boolean

    val hasBacklashCompensation: Boolean

    val backslash: Int

    val angle: Double

    val minAngle: Double

    val maxAngle: Double

    fun moveRotator(angle: Double)

    fun syncRotator(angle: Double)

    fun homeRotator()

    fun reverseRotator(enable: Boolean)

    fun abortRotator()

    companion object {

        // grep -rl --include \*.h "public INDI::Rotator" .
        @JvmStatic val DRIVERS = setOf(
            "indi_deepskydad_fr1",
            "indi_esattoarco_focus",
            "indi_falcon_rotator",
            "indi_gemini_focus",
            "indi_integra_focus",
            "indi_lx200generic",
            "indi_nframe_rotator",
            "indi_nightcrawler_focus",
            "indi_nstep_rotator",
            "indi_pyxis_rotator",
            "indi_seletek_rotator",
            "indi_simulator_rotator",
            "indi_wanderer_lite_rotator",
            "indi_wanderer_rotator_lite_v2",
            "indi_wanderer_rotator_mini",
        )
    }
}
