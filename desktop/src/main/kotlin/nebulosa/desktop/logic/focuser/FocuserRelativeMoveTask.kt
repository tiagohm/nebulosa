package nebulosa.desktop.logic.focuser

import io.reactivex.rxjava3.disposables.Disposable
import nebulosa.desktop.logic.EventBus
import nebulosa.desktop.logic.concurrency.CountUpDownLatch
import nebulosa.indi.device.DeviceEvent
import nebulosa.indi.device.focusers.Focuser
import nebulosa.indi.device.focusers.FocuserDetached
import nebulosa.indi.device.focusers.FocuserMoveFailed
import nebulosa.indi.device.focusers.FocuserMovingChanged
import org.koin.core.component.KoinComponent
import org.slf4j.LoggerFactory

data class FocuserRelativeMoveTask(
    override val focuser: Focuser,
    val increment: Int,
    val isOut: Boolean,
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
            if (increment >= 0 && increment <= focuser.maxPosition) {
                synchronized(focuser) {
                    latch.countUp()

                    subscriber = EventBus.DEVICE
                        .subscribe(filter = { it.device === focuser }, next = ::onEvent)

                    LOG.info("moving focuser ${focuser.name} to position by $increment [{}]", if (isOut) "OUT" else "IN")

                    if (isOut) focuser.moveFocusOut(increment)
                    else focuser.moveFocusIn(increment)

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

        @JvmStatic private val LOG = LoggerFactory.getLogger(FocuserRelativeMoveTask::class.java)
    }
}
