package nebulosa.api.services

import com.fasterxml.jackson.databind.ObjectMapper
import nebulosa.log.debug
import nebulosa.log.loggerFor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class MessageService(
    private val simpleMessageTemplate: SimpMessagingTemplate,
    private val objectMapper: ObjectMapper,
) {

    fun sendMessage(eventName: String, payload: Any) {
        LOG.debug { "$eventName: $payload" }
        simpleMessageTemplate.convertAndSend(eventName, payload)
    }

    fun sendMessage(eventName: String, vararg attributes: Pair<String, Any?>) {
        val payload = objectMapper.createObjectNode()
        attributes.forEach { payload.putPOJO(it.first, it.second) }
        sendMessage(eventName, payload)
    }

    fun sendMessage(event: MessageEvent) {
        sendMessage(event.eventName, event)
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<MessageService>()
    }
}
