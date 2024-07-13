package nebulosa.api.guiding

import nebulosa.guiding.GuideStep

data class HistoryStep(
    @JvmField val id: Long = 0L,
    @JvmField val rmsRA: Double = 0.0,
    @JvmField val rmsDEC: Double = 0.0,
    @JvmField val rmsTotal: Double = 0.0,
    @JvmField val guideStep: GuideStep? = null,
    @JvmField val ditherX: Double = 0.0,
    @JvmField val ditherY: Double = 0.0,
)
