package nebulosa.api.data.responses

import nebulosa.guiding.GuideStats

data class GuidingChartResponse(
    val graph: List<GuideStats>,
    val rmsRA: Double,
    val rmsDEC: Double,
    val rmsTotal: Double,
)
