package nebulosa.api.guiding

import jakarta.persistence.*
import nebulosa.guiding.internal.GuideCalibration
import nebulosa.guiding.internal.GuideParity
import nebulosa.indi.device.camera.Camera
import nebulosa.indi.device.guide.GuideOutput
import nebulosa.indi.device.mount.Mount
import nebulosa.math.rad

@Entity
@Table(name = "guide_calibrations")
data class GuideCalibrationEntity(
    @Id @Column(name = "id", columnDefinition = "INT8") var id: Long = 0L,
    @Column(name = "camera", columnDefinition = "TEXT") var camera: String = "",
    @Column(name = "mount", columnDefinition = "TEXT") var mount: String = "",
    @Column(name = "guide_output", columnDefinition = "TEXT") var guideOutput: String = "",
    @Column(name = "saved_at", columnDefinition = "INT8") var savedAt: Long = 0L,
    @Column(name = "x_rate", columnDefinition = "REAL") var xRate: Double = 0.0,
    @Column(name = "y_rate", columnDefinition = "REAL") var yRate: Double = 0.0,
    @Column(name = "x_angle", columnDefinition = "REAL") var xAngle: Double = 0.0, // rad
    @Column(name = "y_angle", columnDefinition = "REAL") var yAngle: Double = 0.0, // rad
    @Column(name = "declination", columnDefinition = "REAL") var declination: Double = 0.0, // rad
    @Column(name = "rotator_angle", columnDefinition = "REAL") var rotatorAngle: Double = 0.0, // rad
    @Column(name = "binning", columnDefinition = "INT1") var binning: Int = 1,
    @Column(name = "pier_side_at_east", columnDefinition = "INT1") var pierSideAtEast: Boolean = false,
    @Column(name = "ra_guide_parity", columnDefinition = "INT1") @Enumerated(EnumType.ORDINAL) var raGuideParity: GuideParity = GuideParity.UNKNOWN,
    @Column(name = "dec_guide_parity", columnDefinition = "INT1") @Enumerated(EnumType.ORDINAL) var decGuideParity: GuideParity = GuideParity.UNKNOWN,
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
            calibration.xAngle, calibration.yAngle,
            calibration.declination, calibration.rotatorAngle,
            calibration.binning, calibration.pierSideAtEast,
            calibration.raGuideParity, calibration.decGuideParity,
        )
    }
}
