package nebulosa.api.data.responses

import nebulosa.guiding.internal.GuideStats

data class GuidingChartResponse(
    val chart: List<GuideStats>,
    val rmsRA: Double,
    val rmsDEC: Double,
    val rmsTotal: Double,
)
