package nebulosa.api.data.entities

import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Index
import nebulosa.api.data.converters.GuideParityConverter
import nebulosa.guiding.GuideCalibration
import nebulosa.guiding.GuideParity
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import nebulosa.math.Angle.Companion.rad

@Entity
data class GuideCalibrationEntity(
    @Id var id: Long = 0L,
    @Index var camera: String = "",
    @Index var mount: String = "",
    @Index var guideOutput: String = "",
    var savedAt: Long = 0L,
    var xRate: Double = 0.0,
    var yRate: Double = 0.0,
    var xAngle: Double = 0.0, // rad
    var yAngle: Double = 0.0, // rad
    var declination: Double = 0.0, // rad
    var rotatorAngle: Double = 0.0, // rad
    var binning: Int = 1,
    var pierSideAtEast: Boolean = false,
    @Convert(converter = GuideParityConverter::class, dbType = String::class)
    var raGuideParity: GuideParity = GuideParity.UNKNOWN,
    @Convert(converter = GuideParityConverter::class, dbType = String::class)
    var decGuideParity: GuideParity = GuideParity.UNKNOWN,
) {

    fun toGuideCalibration() = GuideCalibration(
        xRate, yRate,
        xAngle.rad, yAngle.rad, declination.rad, rotatorAngle.rad,
        binning, pierSideAtEast, raGuideParity, decGuideParity
    )

    companion object {

        @JvmStatic
        fun from(
            camera: Camera, mount: Mount, guideOutput: GuideOutput,
            calibration: GuideCalibration,
        ) = GuideCalibrationEntity(
            0L, camera.name, mount.name, guideOutput.name, System.currentTimeMillis(),
            calibration.xRate, calibration.yRate,
            calibration.xAngle.value, calibration.yAngle.value,
            calibration.declination.value, calibration.rotatorAngle.value,
            calibration.binning, calibration.pierSideAtEast,
            calibration.raGuideParity, calibration.decGuideParity,
        )
    }
}
