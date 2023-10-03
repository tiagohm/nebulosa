package nebulosa.phd2.client.commands

data object GetCalibrated : PHD2Command<Boolean> {

    override val methodName = "get_calibrated"

    override val params = null

    override val responseType = Boolean::class.java
}
