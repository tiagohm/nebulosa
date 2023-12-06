package nebulosa.guiding

data class GuideStar(
    val lockPosition: GuidePoint = GuidePoint.ZERO,
    val starPosition: GuidePoint = GuidePoint.ZERO,
    val image: String = "",
    val guideStep: GuideStep,
)
