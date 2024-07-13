package nebulosa.api.calibration

data class CalibrationFrameGroup(
    @JvmField val id: Int,
    @JvmField val name: String,
    @JvmField val key: CalibrationGroupKey,
    @JvmField val frames: List<CalibrationFrameEntity>,
)
