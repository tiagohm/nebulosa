package nebulosa.guiding.internal

import nebulosa.math.Angle

interface GuidingMount {

    val isBusy: Boolean

    val rightAscension: Angle

    val declination: Angle

    val rightAscensionGuideRate: Double

    val declinationGuideRate: Double

    val isPierSideAtEast: Boolean
}
