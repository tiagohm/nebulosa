package nebulosa.api.message

import nebulosa.log.loggerFor
import org.springframework.stereotype.Service
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

@Service
class MessageService {

    private val connected = AtomicBoolean()
    private val messageQueue = LinkedBlockingQueue<MessageEvent>()

//    @EventListener
//    private fun handleSessionSubscribe(event: SessionSubscribeEvent) {
//        val destination = SimpMessageHeaderAccessor.wrap(event.message).destination ?: return
//
//        if (destination == DESTINATION && connected.compareAndSet(false, true)) {
//            while (messageQueue.isNotEmpty()) {
//                sendMessage(messageQueue.take())
//            }
//        }
//    }

//    @EventListener
//    private fun handleSessionDisconnect(event: SessionDisconnectEvent) {
//        connected.set(false)
//    }

    fun sendMessage(event: MessageEvent) {
        if (connected.get()) {
            LOG.debug("sending message. event={}", event)
            // simpleMessageTemplate.convertAndSend(DESTINATION, event)
        } else if (event is QueueableEvent) {
            LOG.debug("queueing message. event={}", event)
            messageQueue.offer(event)
        }
    }

    companion object {

        const val DESTINATION = "NEBULOSA.EVENT"

        @JvmStatic private val LOG = loggerFor<MessageService>()
    }
}
