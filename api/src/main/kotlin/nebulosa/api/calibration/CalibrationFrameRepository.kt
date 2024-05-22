package nebulosa.api.calibration

import io.objectbox.Box
import io.objectbox.kotlin.equal
import io.objectbox.query.QueryBuilder.StringOrder.CASE_INSENSITIVE
import io.objectbox.query.QueryBuilder.StringOrder.CASE_SENSITIVE
import nebulosa.api.repositories.BoxRepository
import nebulosa.indi.device.camera.FrameType
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class CalibrationFrameRepository(@Qualifier("calibrationFrameBox") override val box: Box<CalibrationFrameEntity>) :
    BoxRepository<CalibrationFrameEntity>() {

    fun groups() = box.all.map { it.name }.distinct()

    fun findAll(name: String): List<CalibrationFrameEntity> {
        return box.query()
            .equal(CalibrationFrameEntity_.name, name, CASE_SENSITIVE)
            .build()
            .use { it.find() }
    }

    @Synchronized
    fun delete(name: String, path: String) {
        return box.query()
            .equal(CalibrationFrameEntity_.name, name, CASE_SENSITIVE)
            .equal(CalibrationFrameEntity_.path, path, CASE_SENSITIVE)
            .build()
            .use { it.remove() }
    }

    fun darkFrames(name: String, width: Int, height: Int, bin: Int, exposureTime: Long, gain: Double): List<CalibrationFrameEntity> {
        return box.query()
            .equal(CalibrationFrameEntity_.type, FrameType.DARK.ordinal)
            .equal(CalibrationFrameEntity_.enabled, true)
            .equal(CalibrationFrameEntity_.name, name, CASE_SENSITIVE)
            .equal(CalibrationFrameEntity_.width, width)
            .equal(CalibrationFrameEntity_.height, height)
            .equal(CalibrationFrameEntity_.binX, bin)
            .equal(CalibrationFrameEntity_.binY, bin)
            .also { if (exposureTime > 0L) it.equal(CalibrationFrameEntity_.exposureTime, exposureTime) }
            .also { if (gain > 0L) it.equal(CalibrationFrameEntity_.gain, gain, 1E-3) }
            .build()
            .use { it.find() }
    }

    fun biasFrames(name: String, width: Int, height: Int, bin: Int, gain: Double): List<CalibrationFrameEntity> {
        return box.query()
            .equal(CalibrationFrameEntity_.type, FrameType.BIAS.ordinal)
            .equal(CalibrationFrameEntity_.enabled, true)
            .equal(CalibrationFrameEntity_.name, name, CASE_SENSITIVE)
            .equal(CalibrationFrameEntity_.width, width)
            .equal(CalibrationFrameEntity_.height, height)
            .equal(CalibrationFrameEntity_.binX, bin)
            .equal(CalibrationFrameEntity_.binY, bin)
            .also { if (gain > 0L) it.equal(CalibrationFrameEntity_.gain, gain, 1E-3) }
            .build()
            .use { it.find() }
    }

    fun flatFrames(name: String, filter: String?, width: Int, height: Int, bin: Int): List<CalibrationFrameEntity> {
        return box.query()
            .equal(CalibrationFrameEntity_.type, FrameType.FLAT.ordinal)
            .equal(CalibrationFrameEntity_.enabled, true)
            .equal(CalibrationFrameEntity_.name, name, CASE_SENSITIVE)
            .also {
                if (filter.isNullOrBlank()) it.isNull(CalibrationFrameEntity_.filter)
                else it.equal(CalibrationFrameEntity_.filter, filter, CASE_INSENSITIVE)
            }
            .equal(CalibrationFrameEntity_.width, width)
            .equal(CalibrationFrameEntity_.height, height)
            .equal(CalibrationFrameEntity_.binX, bin)
            .equal(CalibrationFrameEntity_.binY, bin)
            .build()
            .use { it.find() }
    }
}
