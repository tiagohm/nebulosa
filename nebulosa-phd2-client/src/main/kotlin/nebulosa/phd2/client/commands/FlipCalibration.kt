package nebulosa.phd2.client.commands

data object FlipCalibration : PHD2Command<Int> {

    override val methodName = "flip_calibration"

    override val params = null

    override val responseType = Int::class.java
}
