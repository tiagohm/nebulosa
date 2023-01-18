package nebulosa.desktop.filterwheels

import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import nebulosa.desktop.equipments.ThreadedTask
import nebulosa.desktop.equipments.ThreadedTaskManager
import nebulosa.indi.device.filterwheels.*
import org.slf4j.LoggerFactory

class FilterWheelMoveTask(
    val filterWheel: FilterWheel,
    val position: Int,
) : ThreadedTask<Boolean>(), Consumer<FilterWheelEvent> {

    override fun accept(event: FilterWheelEvent) {
        when (event) {
            is FilterWheelPositionChanged -> release()
            is FilterWheelDetached,
            is FilterWheelMoveFailed -> finish()
        }
    }

    override fun call(): Boolean {
        var subscriber: Disposable? = null

        try {
            if (filterWheel.position != position
                && position in 1..filterWheel.slotCount
            ) {
                synchronized(filterWheel) {
                    acquire()

                    subscriber = eventBus
                        .filterIsInstance<FilterWheelEvent> { it.device === filterWheel }
                        .subscribe(this)

                    LOG.info("moving filter wheel ${filterWheel.name} to position $position")

                    filterWheel.moveTo(position)

                    await()
                }
            }
        } finally {
            subscriber?.dispose()
        }

        return true
    }

    override fun finishGracefully() {}

    companion object : ThreadedTaskManager<Boolean, FilterWheelMoveTask>() {

        @JvmStatic private val LOG = LoggerFactory.getLogger(FilterWheelMoveTask::class.java)
    }
}
