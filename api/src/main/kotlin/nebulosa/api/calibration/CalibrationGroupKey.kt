package nebulosa.api.calibration

import nebulosa.indi.device.camera.FrameType

data class CalibrationGroupKey(
    val type: FrameType,
    val width: Int, val height: Int,
    val binX: Int, val binY: Int,
    val exposureTime: Long,
    val gain: Double, val filter: String?,
) {

    companion object {

        @JvmStatic
        fun from(frame: CalibrationFrameEntity) = CalibrationGroupKey(
            frame.type, frame.width, frame.height,
            frame.binX, frame.binY, frame.exposureTime, frame.gain,
            frame.filter?.ifBlank { null },
        )
    }
}
