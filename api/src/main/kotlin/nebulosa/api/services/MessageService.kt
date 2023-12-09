package nebulosa.api.services

import nebulosa.log.debug
import nebulosa.log.loggerFor
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import org.springframework.web.socket.messaging.SessionSubscribeEvent
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

@Service
class MessageService(
    private val simpleMessageTemplate: SimpMessagingTemplate,
) {

    private val connected = AtomicBoolean()
    private val messageQueue = LinkedBlockingQueue<MessageEvent>()

    @EventListener
    private fun handleSessionSubscribe(event: SessionSubscribeEvent) {
        val destination = SimpMessageHeaderAccessor.wrap(event.message).destination ?: return

        if (destination == EVENT_NAME && connected.compareAndSet(false, true)) {
            while (messageQueue.isNotEmpty()) {
                sendMessage(messageQueue.take())
            }
        }
    }

    @EventListener
    private fun handleSessionDisconnect(event: SessionDisconnectEvent) {
        connected.set(false)
    }

    fun sendMessage(event: MessageEvent) {
        if (connected.get()) {
            simpleMessageTemplate.convertAndSend(EVENT_NAME, event)
        } else {
            LOG.debug { "queueing message. event=$event" }
            messageQueue.offer(event)
        }
    }

    companion object {

        const val EVENT_NAME = "NEBULOSA_EVENT"

        @JvmStatic private val LOG = loggerFor<MessageService>()
    }
}
