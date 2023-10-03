package nebulosa.phd2.client.commands

data class ClearCalibration(val mount: Boolean = true, val ao: Boolean = !mount) : PHD2Command<Int> {

    init {
        require(mount || ao)
    }

    override val methodName = "clear_calibration"

    override val params = listOf(if (mount && ao) "both" else if (ao) "ao" else "mount")

    override val responseType = Int::class.java
}
