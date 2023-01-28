package nebulosa.desktop.logic.focuser

import io.reactivex.rxjava3.disposables.Disposable
import nebulosa.desktop.logic.EventBus
import nebulosa.desktop.logic.concurrency.CountUpDownLatch
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.focuser.Focuser
import nebulosa.indi.device.focuser.FocuserDetached
import nebulosa.indi.device.focuser.FocuserMoveFailed
import nebulosa.indi.device.focuser.FocuserMovingChanged
import org.koin.core.component.KoinComponent
import org.slf4j.LoggerFactory

data class FocuserAbsoluteMoveTask(
    override val focuser: Focuser,
    val position: Int,
) : FocuserTask, KoinComponent {

    private val latch = CountUpDownLatch()

    private fun onEvent(event: DeviceEvent<*>) {
        when (event) {
            is FocuserMovingChanged -> if (!event.device.moving) {
                latch.countDown()
            }
            is FocuserDetached,
            is FocuserMoveFailed -> {
                latch.reset()
                closeGracefully()
            }
        }
    }

    override fun call(): Boolean {
        var subscriber: Disposable? = null

        try {
            if (position >= 0 && position <= focuser.maxPosition) {
                synchronized(focuser) {
                    latch.countUp()

                    subscriber = EventBus.DEVICE
                        .subscribe(filter = { it.device === focuser }, next = ::onEvent)

                    LOG.info("moving focuser ${focuser.name} to absolute position $position")

                    focuser.moveFocusTo(position)

                    latch.await()
                }
            }
        } finally {
            subscriber?.dispose()
        }

        return true
    }

    override fun closeGracefully() {}

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(FocuserAbsoluteMoveTask::class.java)
    }
}
