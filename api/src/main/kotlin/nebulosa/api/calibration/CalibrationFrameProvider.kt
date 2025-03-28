package nebulosa.api.calibration

interface CalibrationFrameProvider {

    fun findBestDarkFrames(
        name: String, temperature: Double, width: Int, height: Int,
        binX: Int, binY: Int = binX, exposureTimeInMicroseconds: Long = 0L,
        gain: Double = 0.0,
    ): List<CalibrationFrameEntity>

    fun findBestFlatFrames(
        name: String, width: Int, height: Int,
        binX: Int, binY: Int = binX, filter: String? = null,
    ): List<CalibrationFrameEntity>

    fun findBestBiasFrames(
        name: String, width: Int, height: Int,
        binX: Int, binY: Int = binX, gain: Double = 0.0,
    ): List<CalibrationFrameEntity>
}
