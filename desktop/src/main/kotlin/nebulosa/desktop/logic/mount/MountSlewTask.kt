package nebulosa.desktop.logic.mount

import nebulosa.desktop.logic.concurrency.CountUpDownLatch
import nebulosa.desktop.logic.task.TaskFinished
import nebulosa.desktop.logic.task.TaskStarted
import nebulosa.indi.device.mount.*
import nebulosa.math.Angle
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

data class MountSlewTask(
    override val mount: Mount,
    val rightAscension: Angle,
    val declination: Angle,
    val isJ2000: Boolean = false,
    val slewType: MountSlewType = MountSlewType.GOTO,
) : MountTask {

    @Autowired private lateinit var eventBus: EventBus

    private val latch = CountUpDownLatch()

    @Subscribe
    fun onEvent(event: MountEvent) {
        if (event.device !== mount) return

        when (event) {
            is MountSlewingChanged -> if (!event.device.slewing) latch.countDown()
            is MountDetached,
            is MountSlewFailed -> latch.reset()
        }
    }

    override fun call() {
        try {
            eventBus.post(TaskStarted(this))

            synchronized(mount) {
                eventBus.register(this)

                latch.countUp()

                LOG.info("slewing mount ${mount.name} to position ra={}, dec={}", rightAscension.hours, declination.degrees)

                if (isJ2000) if (slewType == MountSlewType.SLEW) mount.slewToJ2000(rightAscension, declination)
                else mount.goToJ2000(rightAscension, declination)
                else if (slewType == MountSlewType.SLEW) mount.slewTo(rightAscension, declination)
                else mount.goTo(rightAscension, declination)

                latch.await()
            }
        } catch (e: Throwable) {
            LOG.error("mount slew failed.", e)
            throw e
        } finally {
            eventBus.unregister(this)
            eventBus.post(TaskFinished(this))
        }
    }

    companion object {

        @JvmStatic private val LOG = LoggerFactory.getLogger(MountSlewTask::class.java)
    }
}
