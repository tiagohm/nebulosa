package nebulosa.api.message

import io.javalin.Javalin
import io.javalin.websocket.WsConfig
import io.javalin.websocket.WsContext
import nebulosa.log.loggerFor
import org.eclipse.jetty.websocket.api.Session
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

class MessageService(app: Javalin) : Consumer<WsConfig> {

    private val connected = AtomicReference<Session>()
    private val context = AtomicReference<WsContext>()
    private val messageQueue = LinkedBlockingQueue<MessageEvent>()

    init {
        app.ws("/ws", this)
    }

    override fun accept(ws: WsConfig) {
        ws.onConnect {
            if (connected.compareAndSet(null, it.session)) {
                LOG.info("web socket session accepted. address={}", it.session.remoteAddress)

                context.set(it)
                it.enableAutomaticPings()

                while (messageQueue.isNotEmpty()) {
                    sendMessage(messageQueue.take())
                }
            } else {
                LOG.warn("web socket session rejected. address={}", it.session.remoteAddress)

                // Accepts only one connection.
                it.closeSession()
            }
        }

        ws.onClose {
            if (connected.compareAndSet(it.session, null)) {
                it.disableAutomaticPings()
                context.set(null)
                LOG.info("web socket session closed. address={}, status={}, reason={}", it.session.remoteAddress, it.status(), it.reason())
            }
        }
    }

    fun sendMessage(event: MessageEvent) {
        val context = context.get()

        if (context != null) {
            LOG.debug("sending message. event={}", event)
            context.send(event)
        } else if (event is QueueableEvent) {
            LOG.debug("queueing message. event={}", event)
            messageQueue.offer(event)
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<MessageService>()
    }
}
