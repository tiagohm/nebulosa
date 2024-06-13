package nebulosa.api.calibration

import io.objectbox.Box
import io.objectbox.kotlin.equal
import nebulosa.api.repositories.BoxRepository
import nebulosa.indi.device.camera.FrameType
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class CalibrationFrameRepository(@Qualifier("calibrationFrameBox") override val box: Box<CalibrationFrameEntity>) :
    BoxRepository<CalibrationFrameEntity>() {

    fun groups() = box.all.map { it.name }.distinct()

    fun findAll(name: String): List<CalibrationFrameEntity> {
        return box.query(CalibrationFrameEntity_.name equal name)
            .build().use { it.find() }
    }

    @Synchronized
    fun delete(name: String, path: String) {
        val condition = and(CalibrationFrameEntity_.name equal name, CalibrationFrameEntity_.path equal path)
        return box.query(condition).build().use { it.remove() }
    }

    fun darkFrames(name: String, width: Int, height: Int, bin: Int, exposureTime: Long, gain: Double): List<CalibrationFrameEntity> {
        val condition = and(
            CalibrationFrameEntity_.type equal FrameType.DARK.ordinal,
            CalibrationFrameEntity_.enabled.isTrue,
            CalibrationFrameEntity_.name equal name,
            CalibrationFrameEntity_.width equal width,
            CalibrationFrameEntity_.height equal height,
            CalibrationFrameEntity_.binX equal bin,
            CalibrationFrameEntity_.binY equal bin,
            if (exposureTime > 0L) CalibrationFrameEntity_.exposureTime equal exposureTime else null,
            if (gain > 0L) CalibrationFrameEntity_.gain.equal(gain, 1E-3) else null,
        )

        return box.query(condition).build().use { it.find() }
    }

    fun biasFrames(name: String, width: Int, height: Int, bin: Int, gain: Double): List<CalibrationFrameEntity> {
        val condition = and(
            CalibrationFrameEntity_.type equal FrameType.BIAS.ordinal,
            CalibrationFrameEntity_.enabled.isTrue,
            CalibrationFrameEntity_.name equal name,
            CalibrationFrameEntity_.width equal width,
            CalibrationFrameEntity_.height equal height,
            CalibrationFrameEntity_.binX equal bin,
            CalibrationFrameEntity_.binY equal bin,
            if (gain > 0L) CalibrationFrameEntity_.gain.equal(gain, 1E-3) else null,
        )

        return box.query(condition).build().use { it.find() }
    }

    fun flatFrames(name: String, filter: String?, width: Int, height: Int, bin: Int): List<CalibrationFrameEntity> {
        val condition = and(
            CalibrationFrameEntity_.type equal FrameType.FLAT.ordinal,
            CalibrationFrameEntity_.enabled.isTrue,
            CalibrationFrameEntity_.name equal name,
            CalibrationFrameEntity_.width equal width,
            CalibrationFrameEntity_.height equal height,
            CalibrationFrameEntity_.binX equal bin,
            CalibrationFrameEntity_.binY equal bin,
            if (filter.isNullOrBlank()) CalibrationFrameEntity_.filter.isNull
            else CalibrationFrameEntity_.filter equalInsensitive filter,
        )

        return box.query(condition).build().use { it.find() }
    }
}
