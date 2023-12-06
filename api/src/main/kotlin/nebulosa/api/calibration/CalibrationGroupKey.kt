package nebulosa.api.calibration

import nebulosa.indi.device.camera.FrameType
import kotlin.math.roundToInt

data class CalibrationGroupKey(
    val type: FrameType, val filter: String?,
    val width: Int, val height: Int,
    val binX: Int, val binY: Int,
    val exposureTime: Long,
    val temperature: Int, val gain: Double,
) {

    companion object {

        @JvmStatic
        fun from(frame: CalibrationFrameEntity) = CalibrationGroupKey(
            frame.type, frame.filter?.ifBlank { null },
            frame.width, frame.height,
            frame.binX, frame.binY, frame.exposureTime,
            frame.temperature.roundToInt(), frame.gain,
        )
    }
}
