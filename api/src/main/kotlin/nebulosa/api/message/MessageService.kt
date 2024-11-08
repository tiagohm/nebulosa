package nebulosa.api.message

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.runBlocking
import nebulosa.log.d
import nebulosa.log.di
import nebulosa.log.e
import nebulosa.log.i
import nebulosa.log.loggerFor
import nebulosa.log.w
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicReference

class MessageService(
    app: Application,
    private val mapper: ObjectMapper,
) {

    private val session = AtomicReference<WebSocketServerSession>()
    private val messageQueue = LinkedBlockingQueue<MessageEvent>()

    init {
        with(app) {
            routing {
                webSocket("/ws") {
                    if (session.compareAndSet(null, this)) {
                        val local = call.request.local

                        LOG.i("session accepted. address={}:{}", local.remoteHost, local.remotePort)

                        while (messageQueue.isNotEmpty()) {
                            sendMessage(messageQueue.take())
                        }

                        try {
                            for (frame in incoming) {
                                LOG.di("frame received: {}", frame)
                            }

                            LOG.i("session closed. address={}:{}, reason={}", local.remoteHost, local.remotePort, closeReason.await())
                        } catch (e: Throwable) {
                            LOG.e("session closed. address={}:{}, reason={}", local.remoteHost, local.remotePort, closeReason.await(), e)
                        } finally {
                            session.set(null)
                        }
                    } else {
                        LOG.w("session rejected. address={}", this)

                        // Accepts only one connection.
                        close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Too many connections"))
                    }
                }
            }
        }
    }

    fun sendMessage(event: MessageEvent) {
        val context = session.get()

        if (context != null) {
            LOG.d("sending message. event={}", event)
            val text = mapper.writeValueAsString(event)
            runBlocking { context.send(Frame.Text(text)) }
        } else if (event is QueueableEvent) {
            LOG.d("queueing message. event={}", event)
            messageQueue.offer(event)
        }
    }

    companion object {

        private val LOG = loggerFor<MessageService>()
    }
}
