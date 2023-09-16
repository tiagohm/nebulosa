package nebulosa.api.services

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class MessageService(private val simpleMessageTemplate: SimpMessagingTemplate) {

    fun sendMessage(eventName: String, payload: Any) {
        simpleMessageTemplate.convertAndSend(eventName, payload)
    }

    fun sendMessage(eventName: String, vararg attributes: Pair<String, Any>) {
        sendMessage(eventName, mapOf(*attributes))
    }

    fun sendMessage(event: MessageEvent) {
        sendMessage(event.eventName, event)
    }
}
