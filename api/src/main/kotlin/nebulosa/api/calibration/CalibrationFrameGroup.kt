package nebulosa.api.calibration

data class CalibrationFrameGroup(
    val key: CalibrationGroupKey,
    val frames: List<CalibrationFrameEntity>,
)
