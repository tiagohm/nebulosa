package nebulosa.api.guiding

import nebulosa.guiding.internal.DeclinationGuideMode
import nebulosa.guiding.internal.NoiseReductionMethod

data class GuideStartLooping(
    var searchRegion: Double = 15.0,
    var ditherMode: DitherMode = DitherMode.RANDOM,
    var ditherAmount: Double = 5.0,
    var ditherRAOnly: Boolean = false,
    var calibrationFlipRequiresDecFlip: Boolean = false,
    var assumeDECOrthogonalToRA: Boolean = false,
    var calibrationStep: Int = 1000,
    var calibrationDistance: Int = 25,
    var useDECCompensation: Boolean = true,
    var declinationGuideMode: DeclinationGuideMode = DeclinationGuideMode.AUTO,
    var maxDECDuration: Int = 2500,
    var maxRADuration: Int = 2500,
    var noiseReductionMethod: NoiseReductionMethod = NoiseReductionMethod.NONE,
    var xGuideAlgorithm: GuideAlgorithmType = GuideAlgorithmType.HYSTERESIS,
    var yGuideAlgorithm: GuideAlgorithmType = GuideAlgorithmType.HYSTERESIS,
    // min: 0.1, max: 10 (px)
    var minimumStarHFD: Double = 1.5,
    // min: 0.1, max: 10 (px)
    var maximumStarHFD: Double = 1.5,
)
