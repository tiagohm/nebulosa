package nebulosa.api.calibration

import nebulosa.indi.device.camera.Camera
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Repository
@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
interface CalibrationFrameRepository : JpaRepository<CalibrationFrameEntity, Long> {

    @Transactional(readOnly = true)
    @Query("SELECT frame FROM CalibrationFrameEntity frame WHERE frame.camera = :#{#camera.name}")
    fun findAll(camera: Camera): List<CalibrationFrameEntity>

    @Modifying
    @Query("DELETE FROM CalibrationFrameEntity frame WHERE frame.camera = :#{#camera.name} and frame.path = :path")
    fun delete(camera: Camera, path: String)

    @Query(
        "SELECT frame FROM CalibrationFrameEntity frame WHERE frame.type = 1 " +
                "AND frame.enabled = TRUE AND frame.camera = :#{#camera.name} " +
                "AND frame.width = :width " +
                "AND frame.height = :height " +
                "AND frame.binX = :bin AND frame.binY = :bin " +
                "AND (:exposureTime <= 0 OR frame.exposureTime = :exposureTime) " +
                "AND (:gain < 0.0 OR frame.gain = :gain)"
    )
    @Transactional(readOnly = true)
    fun darkFrames(camera: Camera, width: Int, height: Int, bin: Int, exposureTime: Long, gain: Double): List<CalibrationFrameEntity>

    @Query(
        "SELECT frame FROM CalibrationFrameEntity frame WHERE frame.type = 3 " +
                "AND frame.enabled = TRUE AND frame.camera = :#{#camera.name} " +
                "AND frame.width = :width AND frame.height = :height " +
                "AND frame.binX = :bin AND frame.binY = :bin " +
                "AND (:gain < 0.0 OR frame.gain = :gain)"
    )
    @Transactional(readOnly = true)
    fun biasFrames(camera: Camera, width: Int, height: Int, bin: Int, gain: Double): List<CalibrationFrameEntity>

    @Query(
        "SELECT frame FROM CalibrationFrameEntity frame WHERE frame.type = 2 " +
                "AND frame.enabled = TRUE AND frame.camera = :#{#camera.name} AND frame.filter = :filter " +
                "AND frame.width = :width AND frame.height = :height " +
                "AND frame.binX = :bin AND frame.binY = :bin"
    )
    @Transactional(readOnly = true)
    fun flatFrames(camera: Camera, filter: String, width: Int, height: Int, bin: Int): List<CalibrationFrameEntity>
}
