package nebulosa.guiding.internal

import nebulosa.math.Angle

interface GuideMount {

    val connected: Boolean

    val busy: Boolean

    val calibrated: Boolean

    val raParity: GuideParity

    val decParity: GuideParity

    val declination: Angle

    val guidingEnabled: Boolean

    val declinationGuideMode: DeclinationGuideMode

    val guidingRAOnly
        get() = declinationGuideMode == DeclinationGuideMode.NONE

    val xGuideAlgorithm: GuideAlgorithm

    val yGuideAlgorithm: GuideAlgorithm

    fun beginCalibration(currentLocation: Point): Boolean

    fun updateCalibrationState(currentLocation: Point): Boolean

    fun notifyGuidingStarted()

    fun notifyGuidingStopped()

    fun notifyGuidingPaused()

    fun notifyGuidingResumed()

    fun notifyGuidingDithered(dx: Double, dy: Double, mountCoords: Boolean)

    fun notifyGuidingDitherSettleDone(success: Boolean)

    fun notifyDirectMove(distance: Point)

    fun transformMountCoordinatesToCameraCoordinates(mount: Point, camera: Point): Boolean

    fun moveOffset(offset: GuiderOffset, moveOptions: List<MountMoveOption>): Boolean

    fun transformCameraCoordinatesToMountCoordinates(camera: Point, mount: Point): Boolean
}
