package nebulosa.api.calibration

data class CalibrationFrameGroup(
    val id: Int,
    val key: CalibrationGroupKey,
    val frames: List<CalibrationFrameEntity>,
)
