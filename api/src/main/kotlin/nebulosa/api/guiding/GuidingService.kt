package nebulosa.api.guiding

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import nebulosa.api.preferences.PreferenceService
import nebulosa.api.services.MessageService
import nebulosa.guiding.GuideStar
import nebulosa.guiding.GuideState
import nebulosa.guiding.Guider
import nebulosa.guiding.GuiderListener
import nebulosa.phd2.client.PHD2Client
import org.springframework.stereotype.Service
import java.time.Duration
import kotlin.math.max
import kotlin.math.min

@Service
class GuidingService(
    private val preferenceService: PreferenceService,
    private val messageService: MessageService,
    private val phd2Client: PHD2Client,
    private val guider: Guider,
) : GuiderListener {

    private val guideHistory = GuideStepHistory()

    val settleAmount
        get() = guider.settleAmount

    val settleTime
        get() = guider.settleTime

    val settleTimeout
        get() = guider.settleTimeout

    @PostConstruct
    private fun initialize() {
        settle(preferenceService.getJSON<SettleInfo>("GUIDING.SETTLE") ?: SettleInfo.EMPTY)
    }

    @Synchronized
    fun connect(host: String, port: Int) {
        check(!phd2Client.isOpen)

        phd2Client.open(host, port)
        guider.registerGuiderListener(this)
        messageService.sendMessage(GuiderMessageEvent(GUIDER_CONNECTED))
    }

    @PreDestroy
    @Synchronized
    fun disconnect() {
        runCatching { guider.close() }
        messageService.sendMessage(GuiderMessageEvent(GUIDER_DISCONNECTED))
    }

    fun status(): GuiderInfo {
        return if (!phd2Client.isOpen) GuiderInfo.DISCONNECTED
        else GuiderInfo(phd2Client.isOpen, guider.state, guider.isSettling, guider.pixelScale)
    }

    fun history(maxLength: Int = 100): List<HistoryStep> {
        val startIndex = max(0, guideHistory.size - 100)
        val endIndex = min(guideHistory.size, startIndex + maxLength)
        return guideHistory.subList(startIndex, endIndex)
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

    fun settle(settle: SettleInfo) {
        guider.settleAmount = settle.amount
        guider.settleTime = Duration.ofSeconds(settle.time)
        guider.settleTimeout = Duration.ofSeconds(settle.timeout)

        preferenceService.putJSON("GUIDING.SETTLE", settle)
    }

    fun settle(): SettleInfo {
        return SettleInfo.from(guider)
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
        val status = GuiderInfo(phd2Client.isOpen, state, guider.isSettling, pixelScale)
        messageService.sendMessage(GuiderMessageEvent(GUIDER_UPDATED, status))
    }

    override fun onGuideStepped(guideStar: GuideStar) {
        val payload = guideStar.guideStep.let(guideHistory::addGuideStep)
        messageService.sendMessage(GuiderMessageEvent(GUIDER_STEPPED, payload))
    }

    override fun onDithered(dx: Double, dy: Double) {
        val payload = guideHistory.addDither(dx, dy)
        messageService.sendMessage(GuiderMessageEvent(GUIDER_STEPPED, payload))
    }

    override fun onMessageReceived(message: String) {
        messageService.sendMessage(GuiderMessageEvent(GUIDER_MESSAGE_RECEIVED, message))
    }

    companion object {

        const val GUIDER_CONNECTED = "GUIDER_CONNECTED"
        const val GUIDER_DISCONNECTED = "GUIDER_DISCONNECTED"
        const val GUIDER_UPDATED = "GUIDER_UPDATED"
        const val GUIDER_STEPPED = "GUIDER_STEPPED"
        const val GUIDER_MESSAGE_RECEIVED = "GUIDER_MESSAGE_RECEIVED"
    }
}
