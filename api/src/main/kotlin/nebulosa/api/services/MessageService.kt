package nebulosa.api.services

import com.fasterxml.jackson.databind.ObjectMapper
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
    private val objectMapper: ObjectMapper,
) {

    private val connected = AtomicBoolean()
    private val messageQueue = LinkedBlockingQueue<Pair<String, Any>>()

    @EventListener
    private fun handleSessionSubscribe(event: SessionSubscribeEvent) {
        val destination = SimpMessageHeaderAccessor.wrap(event.message).destination ?: return

        if (destination == "END" && connected.compareAndSet(false, true)) {
            while (messageQueue.isNotEmpty()) {
                val (eventName, payload) = messageQueue.take()
                sendMessage(eventName, payload)
            }
        }
    }

    @EventListener
    private fun handleSessionDisconnect(event: SessionDisconnectEvent) {
        connected.set(false)
    }

    fun sendMessage(eventName: String, payload: Any) {
        if (connected.get()) {
            LOG.debug { "$eventName: $payload" }
            simpleMessageTemplate.convertAndSend(eventName, payload)
        } else {
            LOG.info("queueing message. eventName={}", eventName)
            messageQueue.offer(eventName to payload)
        }
    }

    fun sendMessage(eventName: String, vararg attributes: Pair<String, Any?>) {
        if (attributes.isNotEmpty()) {
            val payload = objectMapper.createObjectNode()
            attributes.forEach { payload.putPOJO(it.first, it.second) }
            sendMessage(eventName, payload)
        } else {
            sendMessage(eventName, EMPTY_PAYLOAD)
        }
    }

    fun sendMessage(event: MessageEvent) {
        sendMessage(event.eventName, event)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<MessageService>()
        @JvmStatic private val EMPTY_PAYLOAD = Any()
    }
}
