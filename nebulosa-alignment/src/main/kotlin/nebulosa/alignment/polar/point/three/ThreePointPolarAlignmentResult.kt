package nebulosa.alignment.polar.point.three

sealed interface ThreePointPolarAlignmentResult {

    data object NeedMoreMeasure : ThreePointPolarAlignmentResult

    data object NoPlateSolution : ThreePointPolarAlignmentResult
}
