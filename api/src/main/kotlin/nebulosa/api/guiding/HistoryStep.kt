package nebulosa.api.guiding

import nebulosa.guiding.GuideStep

data class HistoryStep(
    val id: Long = 0L,
    val rmsRA: Double = 0.0,
    val rmsDEC: Double = 0.0,
    val rmsTotal: Double = 0.0,
    val guideStep: GuideStep? = null,
    val ditherX: Double = 0.0,
    val ditherY: Double = 0.0,
)
