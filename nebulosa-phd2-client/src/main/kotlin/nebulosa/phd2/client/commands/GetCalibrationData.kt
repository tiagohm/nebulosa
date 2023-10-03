package nebulosa.phd2.client.commands

data object GetCalibrationData : PHD2Command<CalibrationData> {

    override val methodName = "get_calibration_data"

    override val params = null

    override val responseType = CalibrationData::class.java
}
