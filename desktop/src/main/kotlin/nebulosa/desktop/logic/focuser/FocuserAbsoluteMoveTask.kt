package nebulosa.desktop.logic.focuser

import io.reactivex.rxjava3.disposables.Disposable
import nebulosa.desktop.core.EventBus
import nebulosa.desktop.logic.concurrency.CountUpDownLatch
import nebulosa.indi.device.focusers.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.LoggerFactory

data class FocuserAbsoluteMoveTask(
    override val focuser: Focuser,
    val position: Int,
) : FocuserTask, KoinComponent {

    private val eventBus by inject<EventBus>()
    private val latch = CountUpDownLatch()

    private fun onFocuserEvent(event: FocuserEvent) {
        when (event) {
            is FocuserMovingChanged -> if (!event.device.isMoving) {
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

                    subscriber = eventBus
                        .filterIsInstance<FocuserEvent> { it.device === focuser }
                        .subscribe(::onFocuserEvent)

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
