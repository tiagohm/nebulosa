package nebulosa.api.calibration

data class CalibrationFrameGroup(
    val id: Int,
    val name: String,
    val key: CalibrationGroupKey,
    val frames: List<CalibrationFrameEntity>,
)
