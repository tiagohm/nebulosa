package nebulosa.alignment.polar.point.three

import nebulosa.alignment.polar.point.three.ThreePointPolarAlignment.State.*
import nebulosa.constants.DEG2RAD
import nebulosa.fits.declination
import nebulosa.fits.rightAscension
import nebulosa.imaging.Image
import nebulosa.math.Angle
import nebulosa.math.Point2D
import nebulosa.plate.solving.PlateSolution
import nebulosa.plate.solving.PlateSolver
import nebulosa.star.detection.StarDetector
import java.nio.file.Path

data class ThreePointPolarAlignment(
    private val solver: PlateSolver,
    private val starDetector: StarDetector<Image>,
) {

    enum class State {
        FIRST_MEASURE,
        SECOND_MEASURE,
        THIRD_MEASURE,
        ADJUSTMENT_MEASURE,
    }

    private val solutions = HashMap<State, PlateSolution>(4)

    @Volatile var state = FIRST_MEASURE
        private set

    fun align(
        path: Path, image: Image,
        rightAscension: Angle = image.header.rightAscension,
        declination: Angle = image.header.declination,
        radius: Angle = DEFAULT_RADIUS,
    ): ThreePointPolarAlignmentResult {
        return when (state) {
            FIRST_MEASURE -> measure(path, image, SECOND_MEASURE, rightAscension, declination, radius)
            SECOND_MEASURE -> measure(path, image, THIRD_MEASURE, rightAscension, declination, radius)
            THIRD_MEASURE -> measure(path, image, ADJUSTMENT_MEASURE, rightAscension, declination, radius)
            ADJUSTMENT_MEASURE -> measure(path, image, ADJUSTMENT_MEASURE, rightAscension, declination, radius)
        }
    }

    private fun measure(
        path: Path?, image: Image?, nextState: State,
        rightAscension: Angle, declination: Angle, radius: Angle,
    ): ThreePointPolarAlignmentResult {
        val solution = solver.solve(path, image, rightAscension, declination, radius)

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

        const val DEFAULT_RADIUS: Angle = 4 * DEG2RAD

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
