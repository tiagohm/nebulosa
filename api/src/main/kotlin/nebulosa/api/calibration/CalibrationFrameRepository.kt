package nebulosa.api.calibration

import nebulosa.indi.device.camera.Camera
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CalibrationFrameRepository : JpaRepository<CalibrationFrameEntity, Long> {

    @Query(
        "SELECT frame FROM CalibrationFrameEntity frame WHERE frame.type = 'DARK' " +
                "AND frame.enabled = TRUE AND frame.camera = :#{#camera.name} " +
                "AND frame.width = :width AND frame.height = :height " +
                "AND frame.binX = :bin AND frame.binY = :bin " +
                "AND frame.exposureTime = :exposureTime"
    )
    fun darkFrames(camera: Camera, width: Int, height: Int, bin: Int, exposureTime: Long): List<CalibrationFrameEntity>

    @Query(
        "SELECT frame FROM CalibrationFrameEntity frame WHERE frame.type = 'BIAS' " +
                "AND frame.enabled = TRUE AND frame.camera = :#{#camera.name} " +
                "AND frame.width = :width AND frame.height = :height " +
                "AND frame.binX = :bin AND frame.binY = :bin "
    )
    fun biasFrames(camera: Camera, width: Int, height: Int, bin: Int): List<CalibrationFrameEntity>

    @Query(
        "SELECT frame FROM CalibrationFrameEntity frame WHERE frame.type = 'FLAT' " +
                "AND frame.enabled = TRUE AND frame.camera = :#{#camera.name} AND frame.filter = :filter " +
                "AND frame.width = :width AND frame.height = :height " +
                "AND frame.binX = :bin AND frame.binY = :bin "
    )
    fun flatFrames(camera: Camera, filter: String, width: Int, height: Int, bin: Int): List<CalibrationFrameEntity>
}
