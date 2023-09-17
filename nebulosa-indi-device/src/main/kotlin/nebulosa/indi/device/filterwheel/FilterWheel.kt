package nebulosa.indi.device.filterwheel

import nebulosa.indi.device.Device

interface FilterWheel : Device {

    val count: Int

    val position: Int

    val moving: Boolean

    fun moveTo(position: Int)

    fun syncNames(names: Iterable<String>)

    companion object {

        @JvmStatic val DRIVERS = setOf(
            "indi_apogee_wheel",
            "indi_asi_wheel",
            "indi_atik_wheel",
            "indi_fli_wheel",
            "indi_manual_wheel",
            "indi_optec_wheel",
            "indi_qhycfw1_wheel",
            "indi_qhycfw2_wheel",
            "indi_qhycfw3_wheel",
            "indi_quantum_wheel",
            "indi_simulator_wheel",
            "indi_sx_wheel",
            "indi_trutech_wheel",
            "indi_xagyl_wheel",
        )
    }
}
