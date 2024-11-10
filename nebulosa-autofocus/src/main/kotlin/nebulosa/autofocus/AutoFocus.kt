package nebulosa.autofocus

import nebulosa.curve.fitting.CurvePoint
import nebulosa.curve.fitting.CurvePoint.Companion.midPoint
import nebulosa.curve.fitting.HyperbolicFitting
import nebulosa.curve.fitting.QuadraticFitting
import nebulosa.curve.fitting.TrendLineFitting
import nebulosa.log.d
import nebulosa.log.i
import nebulosa.log.loggerFor
import nebulosa.log.w
import nebulosa.stardetector.StarDetector
import nebulosa.stardetector.StarPoint
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

data class AutoFocus(
    private val starDetector: StarDetector<Path>,
    private val exposureAmount: Int = 5,
    private val initialOffsetSteps: Int = 4,
    private val stepSize: Int = 50,
    private val fittingMode: AutoFocusFittingMode = AutoFocusFittingMode.TREND_HYPERBOLIC,
    private val rSquaredThreshold: Double = 0.0,
    private val reverse: Boolean = false,
    private val focusMaxPosition: Int = Int.MAX_VALUE,
) {

    private sealed interface State {
        data object Idle : State
        data object FocusPoints : State
        data object EvaluateTrendLinePoints : State
        data class MorePointsToTheLeft(@JvmField var moveFocuser: Boolean) : State
        data class MorePointsToTheRight(@JvmField var moveFocuser: Boolean) : State
        data object VerifyDataIsEnougth : State
        data object DeterminateFinalFocusPoint : State
    }

    private enum class SubState {
        IDLE,
        MOVE_FOCUSER,
        TAKE_EXPOSURE,
    }

    private val direction = if (reverse) -1 else 1
    private val numberOfSteps = initialOffsetSteps + 1
    private val measurements = ArrayList<MeasuredStars>(exposureAmount)
    private val maximumFocusPoints = exposureAmount * initialOffsetSteps * 10
    private val focusPoints = ArrayList<CurvePoint>(maximumFocusPoints)
    private val listeners = ConcurrentHashMap.newKeySet<AutoFocusListener>(1)

    @Volatile private var subState = SubState.IDLE
    @Volatile private var state: State = State.Idle

    @Volatile private var measurement = MeasuredStars.EMPTY
    @Volatile private var exposureCount = 0
    @Volatile private var remainingSteps = 0
    @Volatile private var initialFocusPosition = 0
    @Volatile private var currentFocusPosition = 0

    @Volatile private var trendLineCurve: TrendLineFitting.Curve? = null
    @Volatile private var parabolicCurve: QuadraticFitting.Curve? = null
    @Volatile private var hyperbolicCurve: HyperbolicFitting.Curve? = null
    @Volatile private var leftCount = 0
    @Volatile private var rightCount = 0

    @Volatile var determinedFocusPoint: CurvePoint? = null
        private set

    private val isDataPointsEnough
        get() = trendLineCurve != null && (rightCount + focusPoints.count { it.x > trendLineCurve!!.minimum.x && it.y == 0.0 } >= initialOffsetSteps && leftCount + focusPoints.count { it.x < trendLineCurve!!.minimum.x && it.y == 0.0 } >= initialOffsetSteps)

    fun registerAutoFocusListener(listener: AutoFocusListener) {
        listeners.add(listener)
    }

    fun unregisterAutoFocusListener(listener: AutoFocusListener) {
        listeners.remove(listener)
    }

    fun add(path: Path) {
        if (exposureCount > 0) {
            exposureCount--
            LOG.d("added. path={}, exposureCount={}", path, exposureCount)
            measurement = measureStars(path)

            if (exposureCount == 0) {
                measurement = evaluateAllMeasurements()
            }
        }
    }

    fun reset() {
        subState = SubState.IDLE
        state = State.Idle

        measurement = MeasuredStars.EMPTY
        exposureCount = 0
        remainingSteps = 0
        initialFocusPosition = 0
        currentFocusPosition = 0

        trendLineCurve = null
        parabolicCurve = null
        hyperbolicCurve = null
        leftCount = 0
        rightCount = 0
    }

    fun determinate(focusPosition: Int): AutoFocusResult {
        val currentState = state

        LOG.d("determinate. position={}, state={}, subState={}", focusPosition, currentState, subState)

        when (currentState) {
            // Estado inicial.
            State.Idle -> {
                // Salva a posição inicial do focalizador, para que possa voltar caso algo dê errado.
                initialFocusPosition = focusPosition

                // Iniciar a coleta dos pontos de foco.
                state = State.FocusPoints
                remainingSteps = numberOfSteps
                // Próxima passo é mover o focalizador para a posição mais distante.
                return moveFocuser(direction * initialOffsetSteps * stepSize, true)
            }
            // Estado para obter os pontos de foco e calcular a curva.
            State.FocusPoints -> {
                // Ainda não terminou de capturar todos os pontos de foco necessário.
                if (remainingSteps > 0) {
                    // Terminou de mover o focalizador.
                    if (subState == SubState.MOVE_FOCUSER) {
                        currentFocusPosition = focusPosition
                        return takeExposure()
                    }
                    // Ainda não terminou a captura.
                    else if (exposureCount > 0) {
                        return takeExposure()
                    }
                    // Fim da captura.
                    else if (exposureCount == 0) {
                        LOG.d("HFD measured after exposures. hfd={}, stdDev={}", measurement.hfd, measurement.stdDev)

                        computeCurvePoint()

                        // Continua a mover o focalizador.
                        if (remainingSteps-- > 1) {
                            return moveFocuser(direction * -stepSize, true)
                        }

                        state = State.EvaluateTrendLinePoints
                        subState = SubState.IDLE

                        return AutoFocusResult.Determinate
                    }
                }
            }
            State.EvaluateTrendLinePoints -> {
                leftCount = trendLineCurve?.left?.points?.size ?: 0
                rightCount = trendLineCurve?.right?.points?.size ?: 0

                LOG.d("trend line evaluated. left={}, right={}", leftCount, rightCount)

                if (leftCount == 0 && rightCount == 0) {
                    LOG.w("not enought spreaded points")
                    return AutoFocusResult.Failed(initialFocusPosition)
                }

                // Let's keep moving in, one step at a time, until we have enough left trend points.
                // Then we can think about moving out to fill in the right trend points.
                if (trendLineCurve!!.left.points.size < initialOffsetSteps
                    && focusPoints.count { it.x < trendLineCurve!!.minimum.x && it.y == 0.0 } < initialOffsetSteps
                ) {
                    LOG.d("more data points needed to the left of the minimum")

                    val firstX = focusPoints.first().x.roundToInt()

                    // Move to the leftmost point - this should never be necessary since we're already there, but just in case
                    if (focusPosition != firstX) {
                        state = State.MorePointsToTheLeft(true)
                        return moveFocuser(firstX, false)
                    } else {
                        state = State.MorePointsToTheLeft(false)
                    }

                    // More points needed to the left.
                    return moveFocuser(direction * -stepSize, true)
                } else if (trendLineCurve!!.right.points.size < initialOffsetSteps
                    && focusPoints.count { it.x > trendLineCurve!!.minimum.x && it.y == 0.0 } < initialOffsetSteps
                ) {
                    // Now we can go to the right, if necessary.
                    LOG.d("more data points needed to the right of the minimum")

                    val lastX = focusPoints.last().x.roundToInt()

                    // More points needed to the right. Let's get to the rightmost point, and keep going right one point at a time.
                    if (focusPosition != lastX) {
                        state = State.MorePointsToTheRight(true)
                        return moveFocuser(lastX, false)
                    } else {
                        state = State.MorePointsToTheRight(false)
                    }

                    // More points needed to the right.
                    return moveFocuser(direction * stepSize, true)
                }

                state = State.DeterminateFinalFocusPoint
                return AutoFocusResult.Determinate
            }
            is State.MorePointsToTheLeft -> {
                // Terminou de mover o focalizador.
                if (currentState.moveFocuser) {
                    currentState.moveFocuser = false
                    // More points needed to the left.
                    return moveFocuser(direction * -stepSize, true)
                } else if (subState == SubState.MOVE_FOCUSER) {
                    currentFocusPosition = focusPosition
                    return takeExposure()
                }
                // Ainda não terminou a captura.
                else if (exposureCount > 0) {
                    return takeExposure()
                }
                // Fim da captura.
                else if (exposureCount == 0) {
                    state = State.VerifyDataIsEnougth
                    return AutoFocusResult.Determinate
                }
            }
            is State.MorePointsToTheRight -> {
                // Terminou de mover o focalizador.
                if (currentState.moveFocuser) {
                    currentState.moveFocuser = false
                    // More points needed to the right.
                    return moveFocuser(direction * stepSize, true)
                } else if (subState == SubState.MOVE_FOCUSER) {
                    currentFocusPosition = focusPosition
                    return takeExposure()
                }
                // Ainda não terminou a captura.
                else if (exposureCount > 0) {
                    return takeExposure()
                }
                // Fim da captura.
                else if (exposureCount == 0) {
                    state = State.VerifyDataIsEnougth
                    return AutoFocusResult.Determinate
                }
            }
            State.VerifyDataIsEnougth -> {
                LOG.d("HFD measured after exposures. hfd={}, stdDev={}", measurement.hfd, measurement.stdDev)

                computeCurvePoint()

                if (maximumFocusPoints < focusPoints.size) {
                    // Break out when the maximum limit of focus points is reached
                    LOG.w("failed to complete. Maximum number of focus points exceeded ({}).", maximumFocusPoints)
                    return AutoFocusResult.Failed(initialFocusPosition)
                }

                if (focusPosition <= 0 || focusPosition >= focusMaxPosition) {
                    // Break out when the focuser hits the min/max position. It can't continue from there.
                    LOG.w("failed to complete. position reached to min/max")
                    return AutoFocusResult.Failed(initialFocusPosition)
                }

                state = if (isDataPointsEnough) {
                    State.DeterminateFinalFocusPoint
                } else {
                    subState = SubState.IDLE
                    State.EvaluateTrendLinePoints
                }

                return AutoFocusResult.Determinate
            }
            State.DeterminateFinalFocusPoint -> {
                val finalFocusPoint = determineFinalFocusPoint()

                return if (finalFocusPoint == null || !validateCalculatedFocusPosition(finalFocusPoint)) {
                    LOG.w("potentially bad auto-focus. Restoring original focus position")
                    AutoFocusResult.Failed(initialFocusPosition)
                } else {
                    determinedFocusPoint = finalFocusPoint
                    LOG.i("Auto Focus completed. x={}, y={}", finalFocusPoint.x, finalFocusPoint.y)
                    AutoFocusResult.Completed(finalFocusPoint)
                }
            }
        }

        LOG.w("invalid state. state={}, subState={}, exposureCount={}", state, subState, exposureCount)

        throw IllegalStateException("auto focus has reached an invalid state")
    }

    private fun moveFocuser(position: Int, relative: Boolean): AutoFocusResult {
        subState = SubState.MOVE_FOCUSER
        LOG.d("moving focuser. position={}, relative={}", position, relative)
        return AutoFocusResult.MoveFocuser(position, relative)
    }

    private fun takeExposure(): AutoFocusResult {
        // Inicia-se a sequência de capturas.
        if (exposureCount <= 0) {
            measurements.clear()
            exposureCount = exposureAmount
        }

        subState = SubState.TAKE_EXPOSURE
        LOG.d("taking exposure. exposureCount={}", exposureCount)
        return AutoFocusResult.TakeExposure
    }

    private fun measureStars(path: Path): MeasuredStars {
        val detectedStars = starDetector.detect(path)
        LOG.d("detected {} stars", detectedStars.size)
        val measurement = detectedStars.measureDetectedStars()
        LOG.d("HFD measured. hfd={}, stdDev={}", measurement.hfd, measurement.stdDev)
        measurements.add(measurement)
        listeners.forEach { it.onStarDetected(detectedStars.size, measurement.hfd, measurement.stdDev, false) }
        return measurement
    }

    private fun evaluateAllMeasurements(): MeasuredStars {
        if (measurements.isEmpty()) MeasuredStars.EMPTY
        if (measurements.size == 1) return measurements[0]
        val descriptiveStatistics = DescriptiveStatistics(measurements.size)
        measurements.forEach { descriptiveStatistics.addValue(it.hfd) }
        val stdDev = descriptiveStatistics.standardDeviation
        val starCount = measurements.sumOf { it.count } / measurements.size
        listeners.forEach { it.onStarDetected(starCount, descriptiveStatistics.mean, stdDev, true) }
        return MeasuredStars(starCount, descriptiveStatistics.mean, if (stdDev > 0.0) stdDev else 1.0)
    }

    private fun computeCurvePoint(): CurvePoint? {
        return if (measurement.hfd == 0.0) {
            LOG.w("no stars detected in step")
            null
        } else {
            val focusPoint = CurvePoint(currentFocusPosition.toDouble(), measurement.hfd, measurement.stdDev)
            focusPoints.add(focusPoint)
            focusPoints.sortBy { it.x }

            LOG.d("focus point added. remainingSteps={}, point={}", remainingSteps, focusPoint)

            computeCurveFittings()

            focusPoint
        }
    }

    private fun computeCurveFittings() {
        with(focusPoints) {
            trendLineCurve = TrendLineFitting.calculate(this)

            if (size >= 3) {
                if (fittingMode == AutoFocusFittingMode.PARABOLIC || fittingMode == AutoFocusFittingMode.TREND_PARABOLIC) {
                    parabolicCurve = QuadraticFitting.calculate(this)
                } else if (fittingMode == AutoFocusFittingMode.HYPERBOLIC || fittingMode == AutoFocusFittingMode.TREND_HYPERBOLIC) {
                    hyperbolicCurve = HyperbolicFitting.calculate(this)
                }
            }

            val predictedFocusPoint = determinedFocusPoint ?: determineFinalFocusPoint()
            val (minX, minY) = if (isEmpty()) CurvePoint.ZERO else first()
            val (maxX, maxY) = if (isEmpty()) CurvePoint.ZERO else last()

            listeners.forEach { it.onCurveFitted(predictedFocusPoint, minX, minY, maxX, maxY, trendLineCurve, parabolicCurve, hyperbolicCurve) }
        }
    }

    private fun determineFinalFocusPoint(): CurvePoint? {
        return when (fittingMode) {
            AutoFocusFittingMode.TRENDLINES -> trendLineCurve?.intersection
            AutoFocusFittingMode.PARABOLIC -> parabolicCurve?.minimum
            AutoFocusFittingMode.TREND_PARABOLIC -> parabolicCurve?.minimum?.midPoint(trendLineCurve!!.intersection)
            AutoFocusFittingMode.HYPERBOLIC -> hyperbolicCurve?.minimum
            AutoFocusFittingMode.TREND_HYPERBOLIC -> hyperbolicCurve?.minimum?.midPoint(trendLineCurve!!.intersection)
        }
    }

    private fun validateCalculatedFocusPosition(focusPoint: CurvePoint): Boolean {
        LOG.d("validating calculated focus position. threshold={}, x={}, y={}", rSquaredThreshold, focusPoint.x, focusPoint.y)

        if (rSquaredThreshold > 0.0) {
            fun isTrendLineBad() = trendLineCurve?.let { it.left.rSquared < rSquaredThreshold || it.right.rSquared < rSquaredThreshold } != false
            fun isParabolicBad() = parabolicCurve?.let { it.rSquared < rSquaredThreshold } != false
            fun isHyperbolicBad() = hyperbolicCurve?.let { it.rSquared < rSquaredThreshold } != false

            val isBad = when (fittingMode) {
                AutoFocusFittingMode.TRENDLINES -> isTrendLineBad()
                AutoFocusFittingMode.PARABOLIC -> isParabolicBad()
                AutoFocusFittingMode.TREND_PARABOLIC -> isParabolicBad() || isTrendLineBad()
                AutoFocusFittingMode.HYPERBOLIC -> isHyperbolicBad()
                AutoFocusFittingMode.TREND_HYPERBOLIC -> isHyperbolicBad() || isTrendLineBad()
            }

            if (isBad) {
                LOG.w("coefficient of determination is below threshold")
                return false
            }
        }

        val min = focusPoints.first().x
        val max = focusPoints.last().x

        if (focusPoint.x < min || focusPoint.x > max) {
            LOG.w("determined focus point position is outside of the overall measurement points of the curve")
            return false
        }

        return true
    }

    companion object {

        private val LOG = loggerFor<AutoFocus>()

        private fun List<StarPoint>.measureDetectedStars(): MeasuredStars {
            if (isEmpty()) return MeasuredStars.EMPTY
            val descriptiveStatistics = DescriptiveStatistics(size)
            forEach { descriptiveStatistics.addValue(it.hfd) }
            val stdDev = descriptiveStatistics.standardDeviation
            return MeasuredStars(size, descriptiveStatistics.mean, if (stdDev > 0.0) stdDev else 1.0)
        }
    }
}
