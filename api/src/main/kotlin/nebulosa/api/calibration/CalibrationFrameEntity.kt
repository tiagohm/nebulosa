package nebulosa.api.calibration

import jakarta.persistence.*
import nebulosa.api.beans.converters.PathAttributeConverter
import nebulosa.indi.device.camera.FrameType
import java.nio.file.Path

@Entity
@Table(name = "calibration_frames")
data class CalibrationFrameEntity(
    @Id @Column(name = "id", columnDefinition = "INT8") var id: Long = 0L,
    @Enumerated(EnumType.ORDINAL) @Column(name = "type", columnDefinition = "INT1") var type: FrameType = FrameType.LIGHT,
    @Column(name = "camera", columnDefinition = "TEXT") var camera: String = "",
    @Column(name = "filter", columnDefinition = "TEXT") var filter: String = "",
    @Column(name = "exposure_time", columnDefinition = "INT8") var exposureTime: Long = 0L,
    @Column(name = "temperature", columnDefinition = "REAL") var temperature: Double = 0.0,
    @Column(name = "width", columnDefinition = "INT4") var width: Int = 0,
    @Column(name = "height", columnDefinition = "INT4") var height: Int = 0,
    @Column(name = "bin_x", columnDefinition = "INT1") var binX: Int = 0,
    @Column(name = "bin_y", columnDefinition = "INT1") var binY: Int = 0,
    @Convert(converter = PathAttributeConverter::class)
    @Column(name = "path", columnDefinition = "TEXT") var path: Path? = null,
    @Column(name = "enabled", columnDefinition = "INT1") var enabled: Boolean = true,
)
