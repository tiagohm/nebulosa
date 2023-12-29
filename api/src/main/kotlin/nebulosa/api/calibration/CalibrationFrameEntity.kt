package nebulosa.api.calibration

import io.objectbox.annotation.*
import nebulosa.api.entities.BoxEntity
import nebulosa.api.beans.converters.FrameTypePropertyConverter
import nebulosa.indi.device.camera.FrameType

@Entity
data class CalibrationFrameEntity(
    @Id override var id: Long = 0L,
    @Index @Convert(converter = FrameTypePropertyConverter::class, dbType = Int::class) var type: FrameType = FrameType.LIGHT,
    @Index var camera: String? = null,
    @Index var filter: String? = null,
    var exposureTime: Long = 0L,
    var temperature: Double = 0.0,
    var width: Int = 0,
    var height: Int = 0,
    var binX: Int = 0,
    var binY: Int = 0,
    var gain: Double = 0.0,
    @Unique var path: String? = null,
    var enabled: Boolean = true,
) : BoxEntity
