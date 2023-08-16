package nebulosa.api.repositories

import io.objectbox.BoxStore
import io.objectbox.query.QueryBuilder
import jakarta.annotation.PostConstruct
import nebulosa.api.data.entities.GuideCalibrationEntity
import nebulosa.api.data.entities.GuideCalibrationEntity_
import org.springframework.stereotype.Component

@Component
class GuideCalibrationRepository(
    boxStore: BoxStore,
) : BoxRepository<GuideCalibrationEntity>() {

    override val box = boxStore.boxFor(GuideCalibrationEntity::class.java)!!

    fun withCameraAndMountAndGuideOutput(
        camera: String, mount: String, guideOutput: String,
    ) = box.query()
        .equal(GuideCalibrationEntity_.camera, camera, QueryBuilder.StringOrder.CASE_SENSITIVE)
        .equal(GuideCalibrationEntity_.mount, mount, QueryBuilder.StringOrder.CASE_SENSITIVE)
        .equal(GuideCalibrationEntity_.guideOutput, guideOutput, QueryBuilder.StringOrder.CASE_SENSITIVE)
        .orderDesc(GuideCalibrationEntity_.savedAt)
        .build().use { it.findFirst() }

    @PostConstruct
    fun removeIfOld() {
        val currentTime = System.currentTimeMillis()

        for (calibration in this) {
            // If past 90 days.
            if (currentTime - calibration.savedAt >= NINETY_DAYS) {
                box.remove(calibration)
            }
        }
    }

    companion object {

        const val NINETY_DAYS = 1000L * 60 * 60 * 24 * 90
    }
}
