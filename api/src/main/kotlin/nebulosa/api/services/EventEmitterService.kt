package nebulosa.api.services

import jakarta.annotation.PreDestroy
import nebulosa.log.loggerFor
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import kotlin.concurrent.timer

@Service
class EventEmitterService {

    private val eventEmitters = HashMap<String, MutableList<SseEmitter>>()
    @Volatile private var eventId = 1

    private val timer = timer(daemon = true, period = 5000L) {
        val event = SseEmitter.event().id("0").name("PING").data("")

        for (emitters in eventEmitters.values.toList()) {
            for (emitter in emitters) {
                emitter.send(event)
            }
        }
    }

    @PreDestroy
    private fun destroy() {
        timer.cancel()
        eventEmitters.forEach { it.value.forEach(SseEmitter::complete) }
        eventEmitters.clear()
    }

    fun register(key: String): SseEmitter {
        val emitter = SseEmitter(-1)

        LOG.info("SSE client connected.")

        emitter.onCompletion {
            LOG.info("SSE client disconnected.")
            eventEmitters[key]?.remove(emitter)
        }

        val emitters = eventEmitters.getOrPut(key) { ArrayList(8) }

        synchronized(emitters) {
            emitters.add(emitter)
        }

        return emitter
    }

    fun sendEvent(key: String, type: String, data: Any) {
        val emitters = eventEmitters[key] ?: return

        synchronized(emitters) {
            if (emitters.isNotEmpty()) {
                val id = eventId++
                val event = SseEmitter.event().id("$id").name(type).data(data)
                emitters.forEach { it.send(event) }
            }
        }
    }

    companion object {

        @JvmStatic private val LOG = loggerFor<EventEmitterService>()
    }
}
