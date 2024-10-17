package nebulosa.api.focusers

import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserEvent
import nebulosa.job.manager.Job
import nebulosa.util.concurrency.cancellation.CancellationSource

/**
 * This task will wrap an absolute backlash [compensator] model around the [focuser].
 * On each move an absolute backlash compensation value will be applied, if the focuser changes its moving direction
 * The returned position will then accommodate for this backlash and simulating the position without backlash.
 */
data class BacklashCompensationFocuserMoveTask(
    @JvmField val job: Job,
    override val focuser: Focuser,
    @JvmField val position: Int,
    @JvmField val compensator: BacklashCompensator,
) : FocuserTask {

    @Volatile private var task: FocuserTask? = null

    /**
     * Returns the adjusted position based on the amount of backlash compensation.
     */
    val adjustedPosition
        get() = compensator.adjustedPosition(focuser.position)

    override fun handleFocuserEvent(event: FocuserEvent) {
        task?.handleFocuserEvent(event)
    }

    override fun onCancel(source: CancellationSource) {
        task?.onCancel(source)
    }

    override fun onPause(paused: Boolean) {
        task?.onPause(paused)
    }

    override fun run() {
        if (!job.isCancelled && focuser.connected && !focuser.moving) {
            val targetPositions = compensator.compute(position, focuser.position)

            for (position in targetPositions) {
                moveFocuser(position)
            }
        }
    }

    private fun moveFocuser(position: Int) {
        if (!job.isCancelled && position in 0..focuser.maxPosition) {
            task = FocuserMoveAbsoluteTask(job, focuser, position)
            task!!.run()
        }
    }
}
