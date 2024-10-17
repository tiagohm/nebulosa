package nebulosa.api.calibration

import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import nebulosa.api.converters.database.FrameTypePropertyConverter
import nebulosa.api.converters.database.PathPropertyConverter
import nebulosa.api.database.BoxEntity
import nebulosa.api.validators.Validatable
import nebulosa.api.validators.positive
import nebulosa.api.validators.positiveOrZero
import nebulosa.fits.INVALID_TEMPERATURE
import nebulosa.indi.device.camera.FrameType
import org.jetbrains.exposed.sql.ResultRow
import java.nio.file.Path
import kotlin.io.path.Path

@Entity
data class CalibrationFrameEntity(
    @Id override var id: Long = 0L,
    @JvmField @Index @Convert(converter = FrameTypePropertyConverter::class, dbType = Int::class) var type: FrameType = FrameType.LIGHT,
    @JvmField @Index var group: String = "",
    @JvmField var filter: String? = null,
    @JvmField var exposureTime: Long = 0L,
    @JvmField var temperature: Double = INVALID_TEMPERATURE,
    @JvmField var width: Int = 0,
    @JvmField var height: Int = 0,
    @JvmField var binX: Int = 0,
    @JvmField var binY: Int = 0,
    @JvmField var gain: Double = 0.0,
    @JvmField @Convert(converter = PathPropertyConverter::class, dbType = String::class) var path: Path? = null,
    @JvmField var enabled: Boolean = true,
) : BoxEntity, Comparable<CalibrationFrameEntity>, Validatable {

    override fun validate() {
        id.positive()
        exposureTime.positive()
        width.positive()
        height.positive()
        binX.positive()
        binY.positive()
        gain.positiveOrZero()
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
