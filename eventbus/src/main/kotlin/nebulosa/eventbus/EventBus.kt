package nebulosa.eventbus

import org.greenrobot.eventbus.EventBus

class EventBus {

    private val eventBus = EventBus.builder()
        .sendNoSubscriberEvent(false)
        .throwSubscriberException(false)
        .sendSubscriberExceptionEvent(false)
        .logNoSubscriberMessages(false)
        .logSubscriberExceptions(false)
        .build()

    fun register(subscriber: Any) {
        runCatching { eventBus.register(subscriber) }
    }

    fun unregister(subscriber: Any) {
        runCatching { eventBus.unregister(subscriber) }
    }

    @Synchronized
    fun post(event: Event) {
        eventBus.post(event)
    }
}
