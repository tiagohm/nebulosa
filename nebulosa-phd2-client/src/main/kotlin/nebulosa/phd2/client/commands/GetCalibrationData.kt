package nebulosa.phd2.client.commands

data class GetCalibrationData(val which: WhichMount = WhichMount.MOUNT) : PHD2Command<CalibrationData> {

    override val methodName = "get_calibration_data"

    override val params = listOf(which)

    override val responseType = CalibrationData::class.java
}
