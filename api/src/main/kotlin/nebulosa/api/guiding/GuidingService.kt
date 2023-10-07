package nebulosa.api.guiding

import jakarta.annotation.PreDestroy
import nebulosa.api.services.MessageService
import nebulosa.guiding.GuideStar
import nebulosa.guiding.GuideState
import nebulosa.guiding.GuiderListener
import nebulosa.guiding.phd2.PHD2Guider
import nebulosa.phd2.client.PHD2Client
import nebulosa.phd2.client.PHD2EventListener
import nebulosa.phd2.client.commands.PHD2Command
import nebulosa.phd2.client.events.PHD2Event
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.stereotype.Service

@Service
class GuidingService(
    private val messageService: MessageService,
    private val beanFactory: ConfigurableListableBeanFactory,
) : PHD2EventListener, GuiderListener {

    @Volatile private var client: PHD2Client? = null
    @Volatile private var guider: PHD2Guider? = null

    @Synchronized
    fun connect(host: String, port: Int) {
        if (client != null) return

        with(PHD2Client(host, port)) {
            run()
            registerListener(this@GuidingService)
            guider = PHD2Guider(this)
            guider!!.registerGuiderListener(this@GuidingService)
            client = this
            beanFactory.registerSingleton("guider", guider!!)
            messageService.sendMessage(GUIDER_CONNECTED)
        }
    }

    @PreDestroy
    @Synchronized
    fun disconnect() {
        runCatching { guider?.close() }
        guider = null
        client?.unregisterListener(this)
        client = null
        beanFactory.destroyBean("guider")
        messageService.sendMessage(GUIDER_DISCONNECTED)
    }

    fun loop(autoSelectGuideStar: Boolean = true) {
        guider?.startLooping(autoSelectGuideStar)
    }

    fun start(forceCalibration: Boolean = false) {
        guider?.startGuiding(forceCalibration)
    }

    fun stop() {
        guider?.stopGuiding()
    }

    override fun onStateChange(state: GuideState) {
        messageService.sendMessage(GUIDER_UPDATED, guider!!)
    }

    override fun onGuideStep(guideStar: GuideStar) {
        messageService.sendMessage(GUIDER_STEPPED, guideStar)
    }

    override fun onMessage(message: String) {
        messageService.sendMessage(GUIDER_MESSAGE_RECEIVED, "message" to message)
    }

    override fun onEventReceived(event: PHD2Event) {}

    override fun <T> onCommandProcessed(command: PHD2Command<T>, result: T?, error: String?) {}

    companion object {

        const val GUIDER_CONNECTED = "GUIDER_CONNECTED"
        const val GUIDER_DISCONNECTED = "GUIDER_DISCONNECTED"
        const val GUIDER_UPDATED = "GUIDER_UPDATED"
        const val GUIDER_STEPPED = "GUIDER_STEPPED"
        const val GUIDER_MESSAGE_RECEIVED = "GUIDER_MESSAGE_RECEIVED"
    }
}
