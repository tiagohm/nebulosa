package nebulosa.api.calibration

import io.objectbox.Box
import io.objectbox.kotlin.equal
import io.objectbox.query.QueryBuilder.StringOrder.CASE_INSENSITIVE
import io.objectbox.query.QueryBuilder.StringOrder.CASE_SENSITIVE
import nebulosa.api.repositories.BoxRepository
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.camera.FrameType
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class CalibrationFrameRepository(@Qualifier("calibrationFrameBox") override val box: Box<CalibrationFrameEntity>) :
    BoxRepository<CalibrationFrameEntity>() {

    fun findAll(camera: Camera): List<CalibrationFrameEntity> {
        return box.query()
            .equal(CalibrationFrameEntity_.camera, camera.name, CASE_SENSITIVE)
            .build()
            .use { it.find() }
    }

    @Synchronized
    fun delete(camera: Camera, path: String) {
        return box.query()
            .equal(CalibrationFrameEntity_.camera, camera.name, CASE_SENSITIVE)
            .equal(CalibrationFrameEntity_.path, path, CASE_SENSITIVE)
            .build()
            .use { it.remove() }
    }

    fun darkFrames(camera: Camera, width: Int, height: Int, bin: Int, exposureTime: Long, gain: Double): List<CalibrationFrameEntity> {
        return box.query()
            .equal(CalibrationFrameEntity_.type, FrameType.DARK.ordinal)
            .equal(CalibrationFrameEntity_.enabled, true)
            .equal(CalibrationFrameEntity_.camera, camera.name, CASE_SENSITIVE)
            .equal(CalibrationFrameEntity_.width, width)
            .equal(CalibrationFrameEntity_.height, height)
            .equal(CalibrationFrameEntity_.binX, bin)
            .equal(CalibrationFrameEntity_.binY, bin)
            .also { if (exposureTime > 0L) it.equal(CalibrationFrameEntity_.exposureTime, exposureTime) }
            .also { if (gain > 0L) it.equal(CalibrationFrameEntity_.gain, gain, 1E-3) }
            .build()
            .use { it.find() }
    }

    fun biasFrames(camera: Camera, width: Int, height: Int, bin: Int, gain: Double): List<CalibrationFrameEntity> {
        return box.query()
            .equal(CalibrationFrameEntity_.type, FrameType.BIAS.ordinal)
            .equal(CalibrationFrameEntity_.enabled, true)
            .equal(CalibrationFrameEntity_.camera, camera.name, CASE_SENSITIVE)
            .equal(CalibrationFrameEntity_.width, width)
            .equal(CalibrationFrameEntity_.height, height)
            .equal(CalibrationFrameEntity_.binX, bin)
            .equal(CalibrationFrameEntity_.binY, bin)
            .also { if (gain > 0L) it.equal(CalibrationFrameEntity_.gain, gain, 1E-3) }
            .build()
            .use { it.find() }
    }

    fun flatFrames(camera: Camera, filter: String, width: Int, height: Int, bin: Int): List<CalibrationFrameEntity> {
        return box.query()
            .equal(CalibrationFrameEntity_.type, FrameType.BIAS.ordinal)
            .equal(CalibrationFrameEntity_.enabled, true)
            .equal(CalibrationFrameEntity_.camera, camera.name, CASE_SENSITIVE)
            .equal(CalibrationFrameEntity_.filter, filter, CASE_INSENSITIVE)
            .equal(CalibrationFrameEntity_.width, width)
            .equal(CalibrationFrameEntity_.height, height)
            .equal(CalibrationFrameEntity_.binX, bin)
            .equal(CalibrationFrameEntity_.binY, bin)
            .build()
            .use { it.find() }
    }
}
