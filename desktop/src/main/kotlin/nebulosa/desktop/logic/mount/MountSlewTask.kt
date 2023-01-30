package nebulosa.desktop.logic.mount

import io.reactivex.rxjava3.disposables.Disposable
import nebulosa.desktop.logic.EventBus
import nebulosa.desktop.logic.concurrency.CountUpDownLatch
import nebulosa.desktop.logic.task.TaskFinished
import nebulosa.desktop.logic.task.TaskStarted
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.focuser.FocuserMovingChanged
import nebulosa.indi.device.mount.Mount
import nebulosa.indi.device.mount.MountDetached
import nebulosa.indi.device.mount.MountSlewFailed
import nebulosa.math.Angle
import org.slf4j.LoggerFactory

data class MountSlewTask(
    override val mount: Mount,
    val rightAscension: Angle,
    val declination: Angle,
    val isJ2000: Boolean,
    val slewType: MountSlewType,
) : MountTask {

    private val latch = CountUpDownLatch()

    private fun onEvent(event: DeviceEvent<*>) {
        when (event) {
            is FocuserMovingChanged -> if (!event.device.moving) {
                latch.countDown()
            }
            is MountDetached,
            is MountSlewFailed -> latch.reset()
        }
    }

    override fun call() {
        var subscriber: Disposable? = null

        try {
            EventBus.TASK.post(TaskStarted(this))

            synchronized(mount) {
                latch.countUp()

                subscriber = EventBus.DEVICE
                    .subscribe(filter = { it.device === mount }, next = ::onEvent)

                LOG.info("slewing mount ${mount.name} to position ra={}, dec={}", rightAscension.hours, declination.degrees)

                if (isJ2000) if (slewType == MountSlewType.SLEW) mount.slewToJ2000(rightAscension, declination)
                else mount.goToJ2000(rightAscension, declination)
                else if (slewType == MountSlewType.SLEW) mount.slewTo(rightAscension, declination)
                else mount.goTo(rightAscension, declination)

                latch.await()
            }
        } finally {
            subscriber?.dispose()

            EventBus.TASK.post(TaskFinished(this))
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(MountSlewTask::class.java)
    }
}
