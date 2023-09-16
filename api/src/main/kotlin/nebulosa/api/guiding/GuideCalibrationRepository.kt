package nebulosa.api.guiding

import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface GuideCalibrationRepository : JpaRepository<GuideCalibrationEntity, Long> {

    @Query(
        "SELECT gc.* FROM guide_calibrations gc WHERE" +
                " gc.camera = :#{#camera.name} AND" +
                " gc.mount = :#{#mount.name} AND" +
                " gc.guide_output = :#{#guideOutput.name}" +
                " ORDER BY gc.saved_at DESC" +
                " LIMIT 1",
        nativeQuery = true
    )
    fun get(camera: Camera, mount: Mount, guideOutput: GuideOutput): GuideCalibrationEntity?
}
