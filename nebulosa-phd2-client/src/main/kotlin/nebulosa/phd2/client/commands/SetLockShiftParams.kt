package nebulosa.phd2.client.commands

data class SetLockShiftParams(
    val xRate: Double, val yRate: Double,
    val shiftAxes: ShiftAxesType,
    val shiftUnit: RateUnit = shiftAxes.rateUnit,
) : PHD2Command<Int> {

    override val methodName = "set_lock_shift_params"

    override val params = mapOf("rate" to doubleArrayOf(xRate, yRate), "units" to shiftUnit, "axes" to shiftAxes)

    override val responseType = Int::class.java
}
