package nebulosa.api.calibration

import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import nebulosa.api.beans.converters.database.FrameTypePropertyConverter
import nebulosa.api.beans.converters.database.PathPropertyConverter
import nebulosa.api.database.BoxEntity
import nebulosa.indi.device.camera.FrameType
import java.nio.file.Path

@Entity
data class CalibrationFrameEntity(
    @Id override var id: Long = 0L,
    @JvmField @Index @Convert(converter = FrameTypePropertyConverter::class, dbType = Int::class) var type: FrameType = FrameType.LIGHT,
    @JvmField @Index var name: String = "",
    @JvmField var filter: String? = null,
    @JvmField var exposureTime: Long = 0L,
    @JvmField var temperature: Double = 0.0,
    @JvmField var width: Int = 0,
    @JvmField var height: Int = 0,
    @JvmField var binX: Int = 0,
    @JvmField var binY: Int = 0,
    @JvmField var gain: Double = 0.0,
    @JvmField @Convert(converter = PathPropertyConverter::class, dbType = String::class) var path: Path? = null,
    @JvmField var enabled: Boolean = true,
) : BoxEntity
