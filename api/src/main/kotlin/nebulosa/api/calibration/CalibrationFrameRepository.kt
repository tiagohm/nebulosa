package nebulosa.api.calibration

import nebulosa.indi.device.camera.Camera
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
interface CalibrationFrameRepository : JpaRepository<CalibrationFrameEntity, Long> {

    @Query(
        "SELECT frame FROM CalibrationFrameEntity frame WHERE frame.type = 1 " +
                "AND frame.enabled = TRUE AND frame.camera = :#{#camera.name} " +
                "AND frame.width = :width AND frame.height = :height " +
                "AND frame.binX = :bin AND frame.binY = :bin " +
                "AND frame.exposureTime = :exposureTime"
    )
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    fun darkFrames(camera: Camera, width: Int, height: Int, bin: Int, exposureTime: Long): List<CalibrationFrameEntity>

    @Query(
        "SELECT frame FROM CalibrationFrameEntity frame WHERE frame.type = 3 " +
                "AND frame.enabled = TRUE AND frame.camera = :#{#camera.name} " +
                "AND frame.width = :width AND frame.height = :height " +
                "AND frame.binX = :bin AND frame.binY = :bin "
    )
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    fun biasFrames(camera: Camera, width: Int, height: Int, bin: Int): List<CalibrationFrameEntity>

    @Query(
        "SELECT frame FROM CalibrationFrameEntity frame WHERE frame.type = 2 " +
                "AND frame.enabled = TRUE AND frame.camera = :#{#camera.name} AND frame.filter = :filter " +
                "AND frame.width = :width AND frame.height = :height " +
                "AND frame.binX = :bin AND frame.binY = :bin "
    )
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    fun flatFrames(camera: Camera, filter: String, width: Int, height: Int, bin: Int): List<CalibrationFrameEntity>
}