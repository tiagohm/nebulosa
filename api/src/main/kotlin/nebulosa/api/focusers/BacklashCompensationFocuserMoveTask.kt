package nebulosa.api.focusers

import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserEvent
import nebulosa.job.manager.Job
import nebulosa.log.loggerFor

/**
 * This task will wrap an absolute backlash [compensation] model around the [focuser].
 * On each move an absolute backlash compensation value will be applied, if the focuser changes its moving direction
 * The returned position will then accommodate for this backlash and simulating the position without backlash.
 */
data class BacklashCompensationFocuserMoveTask(
    @JvmField val job: Job,
    override val focuser: Focuser,
    @JvmField var position: Int,
    @JvmField val compensation: BacklashCompensation,
) : FocuserTask {

    enum class OvershootDirection {
        NONE,
        IN,
        OUT,
    }

    @Volatile private var offset = 0
    @Volatile private var lastDirection = OvershootDirection.NONE

    private val task = FocuserMoveAbsoluteTask(job, focuser, 0)

    /**
     * Returns the adjusted position based on the amount of backlash compensation.
     */
    val adjustedPosition
        get() = focuser.position - offset

    override fun handleFocuserEvent(event: FocuserEvent) {
        task.handleFocuserEvent(event)
    }

    override fun run() {
        if (!job.isCancelled && focuser.connected && !focuser.moving) {
            val startPosition = focuser.position

            val newPosition = when (compensation.mode) {
                BacklashCompensationMode.ABSOLUTE -> {
                    val adjustedTargetPosition = position + offset

                    if (adjustedTargetPosition < 0) {
                        offset = 0
                        0
                    } else if (adjustedTargetPosition > focuser.maxPosition) {
                        offset = 0
                        focuser.maxPosition
                    } else {
                        val backlashCompensation = calculateAbsoluteBacklashCompensation(startPosition, adjustedTargetPosition)
                        offset += backlashCompensation
                        adjustedTargetPosition + backlashCompensation
                    }
                }
                BacklashCompensationMode.OVERSHOOT -> {
                    val backlashCompensation = calculateOvershootBacklashCompensation(startPosition, position)

                    if (backlashCompensation != 0) {
                        val overshoot = position + backlashCompensation

                        if (overshoot < 0) {
                            LOG.warn("overshooting position is below minimum 0, skipping overshoot")
                        } else if (overshoot > focuser.maxPosition) {
                            LOG.warn("overshooting position is above maximum ${focuser.maxPosition}, skipping overshoot")
                        } else {
                            LOG.info("overshooting from $startPosition to overshoot position $overshoot using a compensation of $backlashCompensation")
                            moveFocuser(overshoot)
                            LOG.info("moving back to position $position")
                        }
                    }

                    position
                }
                else -> {
                    position
                }
            }

            LOG.info("moving to position {} using {} backlash compensation", newPosition, compensation.mode)

            moveFocuser(newPosition)
        }
    }

    private fun moveFocuser(position: Int) {
        if (position > 0 && position <= focuser.maxPosition) {
            lastDirection = determineMovingDirection(focuser.position, position)
            task.position = position
            task.run()
        }
    }

    private fun determineMovingDirection(prevPosition: Int, newPosition: Int): OvershootDirection {
        return if (newPosition > prevPosition) OvershootDirection.OUT
        else if (newPosition < prevPosition) OvershootDirection.IN
        else lastDirection
    }

    private fun calculateAbsoluteBacklashCompensation(lastPosition: Int, newPosition: Int): Int {
        val direction = determineMovingDirection(lastPosition, newPosition)

        return if (direction == OvershootDirection.IN && lastDirection == OvershootDirection.OUT) {
            LOG.info("Focuser is reversing direction from outwards to inwards")
            -compensation.backlashIn
        } else if (direction == OvershootDirection.OUT && lastDirection === OvershootDirection.IN) {
            LOG.info("Focuser is reversing direction from inwards to outwards")
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

        @JvmStatic private val LOG = loggerFor<BacklashCompensationFocuserMoveTask>()
    }
}
