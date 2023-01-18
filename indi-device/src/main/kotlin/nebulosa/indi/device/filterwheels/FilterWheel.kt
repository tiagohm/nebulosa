package nebulosa.indi.device.filterwheels

import nebulosa.indi.device.Device

interface FilterWheel : Device {

    val slotCount: Int

    val position: Int

    val isMoving: Boolean

    fun moveTo(position: Int)

    fun filterNames(names: Iterable<String>)

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
