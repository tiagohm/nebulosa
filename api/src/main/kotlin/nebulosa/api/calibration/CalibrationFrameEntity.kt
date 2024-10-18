package nebulosa.api.calibration

import nebulosa.api.validators.Validatable
import nebulosa.api.validators.positive
import nebulosa.api.validators.positiveOrZero
import nebulosa.fits.INVALID_TEMPERATURE
import nebulosa.indi.device.camera.FrameType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import java.nio.file.Path
import kotlin.io.path.Path

data class CalibrationFrameEntity(
    @JvmField var id: Long = 0L,
    @JvmField var type: FrameType = FrameType.LIGHT,
    @JvmField var group: String = "",
    @JvmField var filter: String? = null,
    @JvmField var exposureTime: Long = 0L,
    @JvmField var temperature: Double = INVALID_TEMPERATURE,
    @JvmField var width: Int = 0,
    @JvmField var height: Int = 0,
    @JvmField var binX: Int = 0,
    @JvmField var binY: Int = 0,
    @JvmField var gain: Double = 0.0,
    @JvmField var path: Path? = null,
    @JvmField var enabled: Boolean = true,
) : Comparable<CalibrationFrameEntity>, Validatable {

    override fun validate() {
        id.positive()
        exposureTime.positive()
        width.positive()
        height.positive()
        binX.positive()
        binY.positive()
        gain.positiveOrZero()
    }

    fun mapTo(builder: UpdateBuilder<Int>) {
        builder[CalibrationFrameTable.type] = type
        builder[CalibrationFrameTable.group] = group
        builder[CalibrationFrameTable.filter] = filter
        builder[CalibrationFrameTable.exposureTime] = exposureTime
        builder[CalibrationFrameTable.temperature] = temperature
        builder[CalibrationFrameTable.width] = width
        builder[CalibrationFrameTable.height] = height
        builder[CalibrationFrameTable.binX] = binX
        builder[CalibrationFrameTable.binY] = binY
        builder[CalibrationFrameTable.gain] = gain
        builder[CalibrationFrameTable.path] = "$path"
        builder[CalibrationFrameTable.enabled] = enabled
    }

    override fun compareTo(other: CalibrationFrameEntity): Int {
        return if (type.ordinal > other.type.ordinal) 1
        else if (type.ordinal < other.type.ordinal) -1
        else if (exposureTime > other.exposureTime) 1
        else if (exposureTime < other.exposureTime) -1
        else if (width > other.width) 1
        else if (width < other.width) -1
        else if (height > other.height) 1
        else if (height < other.height) -1
        else if (binX > other.binX) 1
        else if (binX < other.binX) -1
        else if (binY > other.binY) 1
        else if (binY < other.binY) -1
        else if (gain > other.gain) 1
        else if (gain < other.gain) -1
        else if (temperature > other.temperature) 1
        else if (temperature < other.temperature) -1
        else if (filter != null && other.filter != null) filter!!.compareTo(other.filter!!)
        else if (filter == null) -1
        else 1
    }

    companion object {

        fun from(row: ResultRow): CalibrationFrameEntity {
            return CalibrationFrameEntity(
                row[CalibrationFrameTable.id],
                row[CalibrationFrameTable.type],
                row[CalibrationFrameTable.group],
                row[CalibrationFrameTable.filter],
                row[CalibrationFrameTable.exposureTime],
                row[CalibrationFrameTable.temperature],
                row[CalibrationFrameTable.width],
                row[CalibrationFrameTable.height],
                row[CalibrationFrameTable.binX],
                row[CalibrationFrameTable.binY],
                row[CalibrationFrameTable.gain],
                Path(row[CalibrationFrameTable.path]),
                row[CalibrationFrameTable.enabled],
            )
        }
    }
}
