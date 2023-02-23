package nebulosa.desktop.logic.focuser

import io.reactivex.rxjava3.disposables.Disposable
import nebulosa.desktop.App
import nebulosa.desktop.logic.DeviceEventBus
import nebulosa.desktop.logic.TaskEventBus
import nebulosa.desktop.logic.concurrency.CountUpDownLatch
import nebulosa.desktop.logic.task.TaskFinished
import nebulosa.desktop.logic.task.TaskStarted
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserDetached
import nebulosa.indi.device.focuser.FocuserMoveFailed
import nebulosa.indi.device.focuser.FocuserMovingChanged
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

data class FocuserAbsoluteMoveTask(
    override val focuser: Focuser,
    val position: Int,
) : FocuserTask {

    @Autowired private lateinit var deviceEventBus: DeviceEventBus
    @Autowired private lateinit var taskEventBus: TaskEventBus

    private val latch = CountUpDownLatch()

    init {
        App.autowireBean(this)
    }

    private fun onEvent(event: DeviceEvent<*>) {
        when (event) {
            is FocuserMovingChanged -> if (!event.device.moving) latch.countDown()
            is FocuserDetached,
            is FocuserMoveFailed -> latch.reset()
        }
    }

    override fun call() {
        var subscriber: Disposable? = null

        try {
            taskEventBus.onNext(TaskStarted(this))

            if (position >= 0 && position <= focuser.maxPosition) {
                synchronized(focuser) {
                    latch.countUp()

                    subscriber = deviceEventBus
                        .filter { it.device === focuser }
                        .subscribe(::onEvent)

                    LOG.info("moving focuser ${focuser.name} to absolute position $position")

                    focuser.moveFocusTo(position)

                    latch.await()
                }
            }
        } finally {
            subscriber?.dispose()

            taskEventBus.onNext(TaskFinished(this))
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(FocuserAbsoluteMoveTask::class.java)
    }
}
