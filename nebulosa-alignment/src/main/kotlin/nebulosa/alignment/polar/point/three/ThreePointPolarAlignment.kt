package nebulosa.alignment.polar.point.three

import nebulosa.alignment.polar.PolarAlignment
import nebulosa.alignment.polar.point.three.ThreePointPolarAlignment.State.*
import nebulosa.constants.DEG2RAD
import nebulosa.imaging.Image
import nebulosa.indi.device.mount.Mount
import nebulosa.math.Point2D
import nebulosa.plate.solving.PlateSolution
import nebulosa.plate.solving.PlateSolver
import nebulosa.star.detection.StarDetector

data class ThreePointPolarAlignment(
    private val solver: PlateSolver,
    private val starDetector: StarDetector<Image>,
    private val mount: Mount?, // null for manual mode.
) : PolarAlignment<ThreePointPolarAlignmentResult> {

    private enum class State {
        FIRST_MEASURE,
        SECOND_MEASURE,
        THIRD_MEASURE,
        ADJUSTMENT_MEASURE,
    }

    @Volatile private var state = FIRST_MEASURE
    private val solutions = HashMap<State, PlateSolution>(4)

    override fun align(image: Image): ThreePointPolarAlignmentResult {
        return when (state) {
            FIRST_MEASURE -> measure(image, SECOND_MEASURE)
            SECOND_MEASURE -> measure(image, THIRD_MEASURE)
            THIRD_MEASURE -> measure(image, ADJUSTMENT_MEASURE)
            ADJUSTMENT_MEASURE -> measure(image, ADJUSTMENT_MEASURE)
        }
    }

    private fun measure(image: Image, nextState: State): ThreePointPolarAlignmentResult {
        val radius = if (mount == null) 0.0 else DEFAULT_RADIUS
        val solution = solver.solve(null, image, mount?.rightAscension ?: 0.0, mount?.declination ?: 0.0, radius)

        return if (!solution.solved) {
            ThreePointPolarAlignmentResult.NoPlateSolution
        } else {
            solutions[state] = solution

            if (state != ADJUSTMENT_MEASURE) {
                state = nextState
                ThreePointPolarAlignmentResult.NeedMoreMeasure
            } else {
                computeAdjustment()
            }
        }
    }

    private fun computeAdjustment(): ThreePointPolarAlignmentResult {
        return ThreePointPolarAlignmentResult.NeedMoreMeasure
    }

    fun selectNewReferenceStar(image: Image, point: Point2D) {
        val referenceStar = starDetector.closestStarPosition(image, point)
    }

    companion object {

        private const val DEFAULT_RADIUS = 4 * DEG2RAD

        @JvmStatic
        internal fun StarDetector<Image>.closestStarPosition(image: Image, reference: Point2D): Point2D {
            val detectedStars = detect(image)

            var closestPoint = reference
            var minDistance = Double.MAX_VALUE

            for (star in detectedStars) {
                val distance = star.distance(reference)

                if (distance < minDistance) {
                    closestPoint = star
                    minDistance = distance
                }
            }

            return closestPoint
        }
    }
}
