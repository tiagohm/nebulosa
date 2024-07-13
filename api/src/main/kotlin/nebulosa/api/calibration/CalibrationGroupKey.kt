package nebulosa.api.calibration

import nebulosa.indi.device.camera.FrameType
import kotlin.math.roundToInt

data class CalibrationGroupKey(
    @JvmField val type: FrameType,
    @JvmField val filter: String?,
    @JvmField val width: Int,
    @JvmField val height: Int,
    @JvmField val binX: Int,
    @JvmField val binY: Int,
    @JvmField val exposureTime: Long,
    @JvmField val temperature: Int,
    @JvmField val gain: Double,
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
