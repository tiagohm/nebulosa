package nebulosa.api.guiding

import jakarta.annotation.PreDestroy
import nebulosa.api.services.MessageService
import nebulosa.guiding.GuideStar
import nebulosa.guiding.GuideState
import nebulosa.guiding.Guider
import nebulosa.guiding.GuiderListener
import nebulosa.phd2.client.PHD2Client
import nebulosa.phd2.client.PHD2EventListener
import nebulosa.phd2.client.commands.PHD2Command
import nebulosa.phd2.client.events.PHD2Event
import org.springframework.stereotype.Service
import kotlin.time.Duration

@Service
class GuidingService(
    private val messageService: MessageService,
    private val phd2Client: PHD2Client,
    private val guider: Guider,
) : PHD2EventListener, GuiderListener {

    private val guideHistory = GuideStepHistory()

    @Synchronized
    fun connect(host: String, port: Int) {
        check(!phd2Client.isOpen)

        phd2Client.open(host, port)
        phd2Client.registerListener(this)
        guider.registerGuiderListener(this)
        messageService.sendMessage(GUIDER_CONNECTED)
    }

    @PreDestroy
    @Synchronized
    fun disconnect() {
        runCatching { guider.close() }
        phd2Client.unregisterListener(this)
        messageService.sendMessage(GUIDER_DISCONNECTED)
    }

    fun status(): GuiderStatus {
        return if (!phd2Client.isOpen) GuiderStatus.DISCONNECTED
        else GuiderStatus(phd2Client.isOpen, guider.state, guider.isSettling, guider.pixelScale)
    }

    fun history(): List<HistoryStep> {
        return guideHistory
    }

    fun latestHistory(): HistoryStep? {
        return guideHistory.lastOrNull()
    }

    fun clearHistory() {
        return guideHistory.clear()
    }

    fun loop(autoSelectGuideStar: Boolean = true) {
        if (phd2Client.isOpen) {
            guider.startLooping(autoSelectGuideStar)
        }
    }

    fun start(forceCalibration: Boolean = false) {
        if (phd2Client.isOpen) {
            guider.startGuiding(forceCalibration)
        }
    }

    fun settle(settleAmount: Double?, settleTime: Duration?, settleTimeout: Duration?) {
        if (settleAmount != null) guider.settleAmount = settleAmount
        if (settleTime != null) guider.settleTime = settleTime
        if (settleTimeout != null) guider.settleTimeout = settleTimeout
    }

    fun dither(amount: Double, raOnly: Boolean = false) {
        if (phd2Client.isOpen) {
            guider.dither(amount, raOnly)
        }
    }

    fun stop() {
        if (phd2Client.isOpen) {
            guider.stopGuiding(true)
        }
    }

    override fun onStateChanged(state: GuideState, pixelScale: Double) {
        val status = GuiderStatus(phd2Client.isOpen, state, guider.isSettling, pixelScale)
        messageService.sendMessage(GUIDER_UPDATED, status)
    }

    override fun onGuideStepped(guideStar: GuideStar) {
        val payload = guideStar.guideStep?.let(guideHistory::addGuideStep) ?: guideStar
        messageService.sendMessage(GUIDER_STEPPED, payload)
    }

    override fun onDithered(dx: Double, dy: Double) {
        guideHistory.addDither(dx, dy)
    }

    override fun onMessageReceived(message: String) {
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
