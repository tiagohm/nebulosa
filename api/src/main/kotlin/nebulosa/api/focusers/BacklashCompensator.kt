package nebulosa.api.focusers

import nebulosa.log.d
import nebulosa.log.loggerFor
import kotlin.math.max
import kotlin.math.min

// https://bitbucket.org/Isbeorn/nina

data class BacklashCompensator(
    @JvmField val compensation: BacklashCompensation,
    @JvmField val maxPosition: Int,
) {

    enum class OvershootDirection {
        NONE,
        IN,
        OUT,
    }

    @Volatile var lastDirection = OvershootDirection.NONE
        private set

    @Volatile var offset = 0
        private set

    fun compute(targetPosition: Int, currentPosition: Int): IntArray {
        var newPosition = targetPosition

        when (compensation.mode) {
            BacklashCompensationMode.ABSOLUTE -> {
                val adjustedTargetPosition = targetPosition + offset

                if (adjustedTargetPosition < 0) {
                    offset = 0
                    newPosition = 0
                } else if (adjustedTargetPosition > maxPosition) {
                    offset = 0
                    newPosition = maxPosition
                } else {
                    val backlashCompensation = calculateAbsoluteBacklashCompensation(currentPosition, adjustedTargetPosition)
                    offset += backlashCompensation
                    newPosition = max(0, min(adjustedTargetPosition + backlashCompensation, maxPosition))
                }
            }
            BacklashCompensationMode.OVERSHOOT -> {
                val backlashCompensation = calculateOvershootBacklashCompensation(currentPosition, targetPosition)

                if (backlashCompensation != 0) {
                    val overshoot = targetPosition + backlashCompensation

                    if (overshoot < 0) {
                        LOG.warn("overshooting position is below minimum 0, skipping overshoot")
                    } else if (overshoot > maxPosition) {
                        LOG.warn("overshooting position is above maximum {}, skipping overshoot", maxPosition)
                    } else {
                        LOG.d { debug("overshooting from {} to overshoot position {} using a compensation of {}. Moving back to position {}", currentPosition, overshoot, backlashCompensation, newPosition) }

                        move(currentPosition, overshoot)
                        move(overshoot, newPosition)

                        return intArrayOf(overshoot, newPosition)
                    }
                }
            }
            BacklashCompensationMode.NONE -> Unit
        }

        move(currentPosition, newPosition)

        return intArrayOf(newPosition)
    }

    fun adjustedPosition(currentPosition: Int): Int {
        return currentPosition - offset
    }


    @Suppress("NOTHING_TO_INLINE")
    private inline fun move(currentPosition: Int, newPosition: Int) {
        lastDirection = determineMovingDirection(currentPosition, newPosition)
        LOG.d { debug("moving to position {} using {} backlash compensation", newPosition, compensation.mode) }
    }

    private fun determineMovingDirection(prevPosition: Int, newPosition: Int): OvershootDirection {
        return if (newPosition > prevPosition) OvershootDirection.OUT
        else if (newPosition < prevPosition) OvershootDirection.IN
        else lastDirection
    }

    private fun calculateAbsoluteBacklashCompensation(lastPosition: Int, newPosition: Int): Int {
        val direction = determineMovingDirection(lastPosition, newPosition)

        return if (direction == OvershootDirection.IN && lastDirection == OvershootDirection.OUT) {
            LOG.debug("focuser is reversing direction from outwards to inwards")
            -compensation.backlashIn
        } else if (direction == OvershootDirection.OUT && lastDirection === OvershootDirection.IN) {
            LOG.debug("focuser is reversing direction from inwards to outwards")
            compensation.backlashOut
        } else {
            0
        }
    }

    private fun calculateOvershootBacklashCompensation(lastPosition: Int, newPosition: Int): Int {
        val direction = determineMovingDirection(lastPosition, newPosition)

        return if (direction == OvershootDirection.IN && compensation.backlashIn != 0) -compensation.backlashIn
        else if (direction == OvershootDirection.OUT && compensation.backlashOut != 0) compensation.backlashOut
        else 0
    }

    companion object {

        private val LOG = loggerFor<BacklashCompensator>()
    }
}
