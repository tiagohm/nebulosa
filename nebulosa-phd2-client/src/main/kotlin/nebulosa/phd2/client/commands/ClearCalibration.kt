package nebulosa.phd2.client.commands

data class ClearCalibration(val which: WhichMount = WhichMount.MOUNT) : PHD2Command<Int> {

    override val methodName = "clear_calibration"

    override val params = listOf(which)

    override val responseType = Int::class.java
}
