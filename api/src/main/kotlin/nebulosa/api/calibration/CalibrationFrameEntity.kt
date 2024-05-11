package nebulosa.api.calibration

import io.objectbox.annotation.*
import nebulosa.api.beans.converters.database.FrameTypePropertyConverter
import nebulosa.api.beans.converters.database.PathPropertyConverter
import nebulosa.api.database.BoxEntity
import nebulosa.indi.device.camera.FrameType
import java.nio.file.Path

@Entity
data class CalibrationFrameEntity(
    @Id override var id: Long = 0L,
    @Index @Convert(converter = FrameTypePropertyConverter::class, dbType = Int::class) var type: FrameType = FrameType.LIGHT,
    @Index var name: String = "",
    var filter: String? = null,
    var exposureTime: Long = 0L,
    var temperature: Double = 0.0,
    var width: Int = 0,
    var height: Int = 0,
    var binX: Int = 0,
    var binY: Int = 0,
    var gain: Double = 0.0,
    @Convert(converter = PathPropertyConverter::class, dbType = String::class) var path: Path? = null,
    var enabled: Boolean = true,
) : BoxEntity
