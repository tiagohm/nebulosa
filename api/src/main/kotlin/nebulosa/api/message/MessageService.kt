package nebulosa.api.message

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.send
import kotlinx.coroutines.runBlocking
import nebulosa.log.d
import nebulosa.log.di
import nebulosa.log.e
import nebulosa.log.i
import nebulosa.log.loggerFor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue

class MessageService(
    app: Application,
    private val mapper: ObjectMapper,
) {

    private val sessions = ConcurrentHashMap.newKeySet<WebSocketServerSession>()
    private val messageQueue = LinkedBlockingQueue<MessageEvent>()

    init {
        with(app) {
            routing {
                webSocket("/ws") {
                    if (sessions.add(this)) {
                        val local = call.request.local

                        LOG.i("session accepted. address={}:{}", local.remoteHost, local.remotePort)

                        while (messageQueue.isNotEmpty()) {
                            send(mapper.writeValueAsString(messageQueue.take()))
                        }

                        try {
                            for (frame in incoming) {
                                LOG.di("frame received: {}", frame)
                            }

                            LOG.i("session closed. address={}:{}, reason={}", local.remoteHost, local.remotePort, closeReason.await())
                        } catch (e: Throwable) {
                            LOG.e("session closed. address={}:{}, reason={}", local.remoteHost, local.remotePort, closeReason.await(), e)
                        } finally {
                            sessions.remove(this)
                        }
                    }
                }
            }
        }
    }

    fun sendMessage(event: MessageEvent) {
        if (sessions.isNotEmpty()) {
            LOG.d("sending message. event={}", event)
            val text = mapper.writeValueAsString(event)
            runBlocking { sessions.forEach { it.send(text) } }
        } else if (event is QueueableEvent) {
            LOG.d("queueing message. event={}", event)
            messageQueue.offer(event)
        }
    }

    companion object {

        private val LOG = loggerFor<MessageService>()
    }
}
